package com.ruoyi.doc.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileDownloadUtils;
import com.ruoyi.doc.domain.DocFile;
import com.ruoyi.doc.domain.DocInfo;
import com.ruoyi.doc.domain.bo.DocInfoBo;
import com.ruoyi.doc.domain.vo.DocDetailVo;
import com.ruoyi.doc.domain.vo.DocInfoVo;
import com.ruoyi.doc.domain.vo.DocPageVo;
import com.ruoyi.doc.mapper.DocFileMapper;
import com.ruoyi.doc.mapper.DocInfoMapper;
import com.ruoyi.doc.service.IDocInfoService;
import com.ruoyi.oss.service.FileStorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文档管理Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocInfoServiceImpl implements IDocInfoService {

    /**
     * 页图片类型
     */
    private static final String FILE_TYPE_PAGE = "PAGE";

    private final DocInfoMapper docInfoMapper;

    private final DocFileMapper docFileMapper;

    private final FileStorageStrategy storageStrategy;

    @Override
    public TableDataInfo<DocInfoVo> pageList(DocInfoBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<DocInfo> wrapper = Wrappers.<DocInfo>lambdaQuery()
            .like(StringUtils.isNotBlank(bo.getDocName()), DocInfo::getDocName, bo.getDocName())
            .like(StringUtils.isNotBlank(bo.getProjectName()), DocInfo::getProjectName, bo.getProjectName())
            .eq(StringUtils.isNotBlank(bo.getProvinceCode()), DocInfo::getProvinceCode, bo.getProvinceCode())
            .orderByDesc(DocInfo::getId);
        Page<DocInfoVo> page = docInfoMapper.selectVoPage(pageQuery.build(), wrapper);
        fillCoverUrl(page.getRecords());
        return TableDataInfo.build(page);
    }

    @Override
    public DocDetailVo detail(Long id) {
        DocInfoVo doc = docInfoMapper.selectVoById(id);
        if (doc == null) {
            throw new ServiceException("文档不存在");
        }
        List<DocFile> files = docFileMapper.selectList(Wrappers.<DocFile>lambdaQuery()
            .eq(DocFile::getDocId, id)
            .orderByAsc(DocFile::getPageNo));
        List<DocPageVo> pages = new ArrayList<>(files.size());
        for (DocFile file : files) {
            DocPageVo page = new DocPageVo();
            page.setPageNo(file.getPageNo());
            page.setFileUrl(file.getFileUrl());
            // 私有桶返回签名地址，前端 <img src> 直接用
            page.setImageUrl(storageStrategy.getAccessUrl(file.getFileUrl()));
            page.setFileSize(file.getFileSize());
            page.setWidth(file.getWidth());
            page.setHeight(file.getHeight());
            pages.add(page);
        }
        DocDetailVo detail = new DocDetailVo();
        detail.setDoc(doc);
        detail.setPages(pages);
        return detail;
    }

    @Override
    public void download(Long id, HttpServletResponse response) {
        DocInfo info = docInfoMapper.selectById(id);
        if (info == null) {
            throw new ServiceException("文档不存在");
        }
        if (StringUtils.isBlank(info.getFileUrl())) {
            throw new ServiceException("文档文件不存在，无法下载");
        }
        String fileName = buildFileName(info.getDocName());
        long start = System.currentTimeMillis();
        log.info("开始下载文档 docId={} fileName={} size={}B", id, fileName, info.getFileSize());
        try {
            FileDownloadUtils.writeResponse(fileName, info.getFileSize(),
                () -> storageStrategy.getContent(info.getFileUrl()), response);
            log.info("文档下载完成 docId={} fileName={} cost={}ms", id, fileName, System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("文档下载失败 docId={} fileName={}", id, fileName, e);
            throw new ServiceException("文档下载失败: " + e.getMessage());
        }
    }

    @Override
    public void remove(Long id) {
        DocInfo info = docInfoMapper.selectById(id);
        if (info == null) {
            throw new ServiceException("文档不存在");
        }
        // 只删文档库的记录：OSS 对象的归属在源系统 sys_file，这里不动它
        docInfoMapper.deleteById(id);
        docFileMapper.delete(Wrappers.<DocFile>lambdaQuery().eq(DocFile::getDocId, id));
        log.info("文档删除完成 docId={} docName={}（只删文档库记录，不动源侧 OSS 对象）", id, info.getDocName());
    }

    /**
     * 列表页取每份文档的第一页作为缩略图
     */
    private void fillCoverUrl(List<DocInfoVo> rows) {
        if (CollUtil.isEmpty(rows)) {
            return;
        }
        List<Long> docIds = rows.stream().map(DocInfoVo::getId).collect(Collectors.toList());
        List<DocFile> files = docFileMapper.selectList(Wrappers.<DocFile>lambdaQuery()
            .in(DocFile::getDocId, docIds)
            .eq(DocFile::getFileType, FILE_TYPE_PAGE)
            .eq(DocFile::getPageNo, 1));
        Map<Long, String> coverMap = new HashMap<>(files.size());
        for (DocFile file : files) {
            coverMap.put(file.getDocId(), storageStrategy.getAccessUrl(file.getFileUrl()));
        }
        rows.forEach(row -> row.setCoverUrl(coverMap.get(row.getId())));
    }

    private String buildFileName(String docName) {
        String name = StringUtils.isBlank(docName) ? "文档" : docName;
        return name.toLowerCase().endsWith(".docx") ? name : name + ".docx";
    }

}
