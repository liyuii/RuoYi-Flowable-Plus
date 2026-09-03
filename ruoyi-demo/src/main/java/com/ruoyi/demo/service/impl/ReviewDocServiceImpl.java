package com.ruoyi.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.demo.domain.ReviewDoc;
import com.ruoyi.demo.domain.ReviewSpan;
import com.ruoyi.demo.domain.vo.ReviewContentVO;
import com.ruoyi.demo.domain.vo.ReviewNodeVO;
import com.ruoyi.demo.mapper.ReviewDocMapper;
import com.ruoyi.demo.mapper.ReviewSpanMapper;
import com.ruoyi.demo.service.IReviewDocService;
import com.ruoyi.demo.utils.ReviewDocxParser;
import com.ruoyi.demo.utils.ReviewMaskApplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * 人工审核Service业务层处理
 */
@RequiredArgsConstructor
@Service
public class ReviewDocServiceImpl implements IReviewDocService {

    private final ReviewDocMapper docMapper;

    private final ReviewSpanMapper spanMapper;

    @Override
    public TableDataInfo<ReviewDoc> pageDocs(PageQuery pageQuery) {
        LambdaQueryWrapper<ReviewDoc> wrapper = Wrappers.lambdaQuery();
        wrapper.orderByDesc(ReviewDoc::getId);
        Page<ReviewDoc> page = docMapper.selectPage(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    @Override
    public ReviewContentVO content(Long docId) {
        ReviewDoc doc = getDocOrThrow(docId);
        checkFileExists(doc.getFilePath());
        try {
            List<ReviewNodeVO> nodes = ReviewDocxParser.parse(doc.getFilePath());
            List<ReviewSpan> spans = spanMapper.selectList(Wrappers.<ReviewSpan>lambdaQuery()
                .eq(ReviewSpan::getDocId, docId)
                .orderByAsc(ReviewSpan::getId));
            ReviewContentVO contentVO = new ReviewContentVO();
            contentVO.setDoc(doc);
            contentVO.setNodes(nodes);
            contentVO.setSpans(spans);
            return contentVO;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("解析文档失败: " + e.getMessage());
        }
    }

    @Override
    public Boolean confirmSpan(Long id) {
        ReviewSpan span = getSpanOrThrow(id);
        span.setStatus("1");
        span.setUpdateTime(new Date());
        return spanMapper.updateById(span) > 0;
    }

    @Override
    public Boolean ignoreSpan(Long id) {
        ReviewSpan span = getSpanOrThrow(id);
        span.setStatus("2");
        span.setUpdateTime(new Date());
        return spanMapper.updateById(span) > 0;
    }

    @Override
    public ReviewSpan addSpan(ReviewSpan span) {
        if (span.getDocId() == null) {
            throw new ServiceException("docId不能为空");
        }
        getDocOrThrow(span.getDocId());
        validateSpan(span);
        Date now = new Date();
        span.setId(null);
        span.setSource(StringUtils.isBlank(span.getSource()) ? "A" : span.getSource());
        span.setStatus(StringUtils.isBlank(span.getStatus()) ? "0" : span.getStatus());
        span.setCreateTime(now);
        span.setUpdateTime(now);
        spanMapper.insert(span);
        return spanMapper.selectById(span.getId());
    }

    @Override
    public ReviewSpan updateSpan(ReviewSpan span) {
        ReviewSpan db = getSpanOrThrow(span.getId());
        validateSpan(span);
        db.setBlockId(span.getBlockId());
        db.setStartChar(span.getStartChar());
        db.setEndChar(span.getEndChar());
        db.setSpanText(span.getSpanText());
        db.setSpanType(span.getSpanType());
        db.setUpdateTime(new Date());
        spanMapper.updateById(db);
        return spanMapper.selectById(db.getId());
    }

    @Override
    public String applyMask(Long docId) {
        ReviewDoc doc = getDocOrThrow(docId);
        checkFileExists(doc.getFilePath());
        List<ReviewSpan> confirmed = spanMapper.selectList(Wrappers.<ReviewSpan>lambdaQuery()
            .eq(ReviewSpan::getDocId, docId)
            .eq(ReviewSpan::getStatus, "1"));
        if (confirmed.isEmpty()) {
            throw new ServiceException("没有已确认的敏感记录");
        }
        String outputPath = ReviewMaskApplier.apply(doc, confirmed);
        doc.setStatus("1");
        doc.setUpdateTime(new Date());
        docMapper.updateById(doc);
        return outputPath;
    }

    private ReviewDoc getDocOrThrow(Long docId) {
        ReviewDoc doc = docMapper.selectById(docId);
        if (doc == null) {
            throw new ServiceException("文档不存在");
        }
        return doc;
    }

    private ReviewSpan getSpanOrThrow(Long id) {
        ReviewSpan span = spanMapper.selectById(id);
        if (span == null) {
            throw new ServiceException("识别记录不存在");
        }
        return span;
    }

    private void validateSpan(ReviewSpan span) {
        if (span.getBlockId() == null || span.getStartChar() == null || span.getEndChar() == null) {
            throw new ServiceException("blockId或起止位置不能为空");
        }
        if (span.getEndChar() <= span.getStartChar()) {
            throw new ServiceException("结束位置必须大于起始位置");
        }
        if (StringUtils.isBlank(span.getSpanType())) {
            throw new ServiceException("敏感类型不能为空");
        }
    }

    private void checkFileExists(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new ServiceException("文档文件不存在: " + filePath);
        }
    }

}
