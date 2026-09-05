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
        boolean updated = spanMapper.updateById(span) > 0;
        if (updated) {
            // 第一次确认后，文档从待审核进入审核中
            ReviewDoc doc = docMapper.selectById(span.getDocId());
            if (doc != null && "0".equals(doc.getStatus())) {
                doc.setStatus("1");
                doc.setUpdateTime(new Date());
                docMapper.updateById(doc);
            }
        }
        return updated;
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
        span.setSpanText(span.getSpanText().trim());
        validateWordSpan(span);
        checkUniqueSpan(span.getDocId(), null, span.getSpanText());
        Date now = new Date();
        span.setId(null);
        span.setBlockId(null);
        span.setStartChar(null);
        span.setEndChar(null);
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
        span.setSpanText(span.getSpanText().trim());
        validateWordSpan(span);
        checkUniqueSpan(db.getDocId(), db.getId(), span.getSpanText());
        db.setSpanText(span.getSpanText());
        db.setSpanType(span.getSpanType());
        db.setUpdateTime(new Date());
        spanMapper.updateById(db);
        return spanMapper.selectById(db.getId());
    }

    @Override
    public Boolean completeReview(Long docId) {
        ReviewDoc doc = getDocOrThrow(docId);
        Long pendingCount = spanMapper.selectCount(Wrappers.<ReviewSpan>lambdaQuery()
            .eq(ReviewSpan::getDocId, docId)
            .eq(ReviewSpan::getStatus, "0"));
        if (pendingCount != null && pendingCount > 0) {
            throw new ServiceException("还有 " + pendingCount + " 条待确认记录未处理");
        }
        doc.setStatus("2");
        doc.setUpdateTime(new Date());
        return docMapper.updateById(doc) > 0;
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

    private void validateWordSpan(ReviewSpan span) {
        if (StringUtils.isBlank(span.getSpanText())) {
            throw new ServiceException("敏感词不能为空");
        }
        if (StringUtils.isBlank(span.getSpanType())) {
            throw new ServiceException("敏感类型不能为空");
        }
    }

    private void checkUniqueSpan(Long docId, Long excludeId, String spanText) {
        Long count = spanMapper.selectCount(Wrappers.<ReviewSpan>lambdaQuery()
            .eq(ReviewSpan::getDocId, docId)
            .eq(ReviewSpan::getSpanText, spanText)
            .ne(excludeId != null, ReviewSpan::getId, excludeId));
        if (count != null && count > 0) {
            throw new ServiceException("该敏感词在当前文档中已存在");
        }
    }

    private void checkFileExists(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new ServiceException("文档文件不存在: " + filePath);
        }
    }

}
