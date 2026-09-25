package com.ruoyi.doc.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.doc.domain.DocFile;
import com.ruoyi.doc.domain.DocInfo;
import com.ruoyi.doc.domain.dto.DocPageDTO;
import com.ruoyi.doc.domain.dto.DocSyncDTO;
import com.ruoyi.doc.mapper.DocFileMapper;
import com.ruoyi.doc.mapper.DocInfoMapper;
import com.ruoyi.doc.service.IDocSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 文档同步服务实现。
 * <p>
 * 只做“接收数据 + 幂等落库”，不再下载文件、不再转图片、也不复制 OSS 对象：
 * 文档文件和页面图片都直接复用源系统的 OSS 地址。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocSyncServiceImpl implements IDocSyncService {

    private final DocInfoMapper docInfoMapper;

    private final DocFileMapper docFileMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long sync(DocSyncDTO dto) {
        validate(dto);
        long start = System.currentTimeMillis();
        List<DocPageDTO> pages = dto.getPages() == null ? Collections.emptyList() : dto.getPages();
        Date now = new Date();
        DocInfo exists = docInfoMapper.selectOne(Wrappers.<DocInfo>lambdaQuery()
            .eq(DocInfo::getSourceType, dto.getSourceType())
            .eq(DocInfo::getSourceBizId, dto.getSourceBizId()));
        Long docId;
        if (exists == null) {
            DocInfo info = new DocInfo();
            copy(dto, info);
            info.setPageCount(pages.size());
            info.setDelFlag("0");
            info.setCreateTime(now);
            info.setUpdateTime(now);
            docInfoMapper.insert(info);
            docId = info.getId();
        } else {
            docId = exists.getId();
            DocInfo update = new DocInfo();
            update.setId(docId);
            copy(dto, update);
            update.setPageCount(pages.size());
            update.setUpdateTime(now);
            docInfoMapper.updateById(update);
            // 幂等更新：先清掉旧的页面明细，再写新的
            docFileMapper.delete(Wrappers.<DocFile>lambdaQuery().eq(DocFile::getDocId, docId));
        }
        int index = 0;
        for (DocPageDTO page : pages) {
            DocFile file = new DocFile();
            file.setDocId(docId);
            file.setFileType("PAGE");
            file.setPageNo(page.getPageNo() != null ? page.getPageNo() : index + 1);
            file.setFileUrl(page.getFileUrl());
            file.setFileSize(page.getFileSize());
            file.setWidth(page.getWidth());
            file.setHeight(page.getHeight());
            file.setCreateTime(now);
            docFileMapper.insert(file);
            index++;
        }
        log.info("文档同步完成 docId={} sourceType={} sourceBizId={} 页数={} 动作={} cost={}ms",
            docId, dto.getSourceType(), dto.getSourceBizId(), pages.size(),
            exists == null ? "新增" : "更新", System.currentTimeMillis() - start);
        return docId;
    }

    private void validate(DocSyncDTO dto) {
        if (dto == null || StringUtils.isBlank(dto.getSourceType()) || dto.getSourceBizId() == null) {
            throw new ServiceException("同步参数不完整：sourceType / sourceBizId 不能为空");
        }
        if (StringUtils.isBlank(dto.getFileUrl())) {
            throw new ServiceException("同步参数不完整：fileUrl 不能为空");
        }
    }

    private void copy(DocSyncDTO dto, DocInfo info) {
        info.setDocName(dto.getDocName());
        info.setProjectName(dto.getProjectName());
        info.setProvinceCode(dto.getProvinceCode());
        info.setAmount(dto.getAmount() == null ? BigDecimal.ZERO : dto.getAmount());
        info.setSourceType(dto.getSourceType());
        info.setSourceBizId(dto.getSourceBizId());
        info.setFileUrl(dto.getFileUrl());
        info.setFileSize(dto.getFileSize());
    }

}
