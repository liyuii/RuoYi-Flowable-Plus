package com.ruoyi.demo.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.demo.domain.ReviewDoc;
import com.ruoyi.demo.mapper.ReviewDocMapper;
import com.ruoyi.doc.domain.dto.DocPageDTO;
import com.ruoyi.doc.domain.dto.DocSyncDTO;
import com.ruoyi.doc.service.IDocSyncService;
import com.ruoyi.system.domain.SysFile;
import com.ruoyi.system.service.ISysFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 投标方案库 → 文档库 的同步编排。
 * <p>
 * 只同步“审核完成 + 图片转换完成”的文档；图片地址直接复用源侧 sys_file 里的 OSS 地址，不复制文件。
 * 目前是进程内调用 IDocSyncService（本地模拟），将来拆成两个项目时把这里换成 HTTP 调用即可。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewDocSyncService {

    /**
     * 来源业务类型
     */
    public static final String SOURCE_TYPE_REVIEW_MASK = "REVIEW_MASK";

    private static final String STATUS_AUDITED = "2";

    private static final String PROCESS_IMAGE_DONE = "IMAGE_DONE";

    private static final String SYNC_WAIT = "WAIT_SYNC";

    private static final String SYNC_DONE = "SYNC_DONE";

    private static final String SYNC_FAIL = "SYNC_FAIL";

    private final ReviewDocMapper docMapper;

    private final ISysFileService sysFileService;

    private final IDocSyncService docSyncService;

    @Value("${ruoyi.review.sync.batch-size:50}")
    private int batchSize;

    @Value("${ruoyi.review.sync.max-retry:3}")
    private int maxRetry;

    /**
     * 扫描待同步文档并逐条同步（定时任务调用）
     *
     * @return 成功条数
     */
    public int syncPendingDocs() {
        long start = System.currentTimeMillis();
        Page<ReviewDoc> page = docMapper.selectPage(new Page<>(1, batchSize),
            Wrappers.<ReviewDoc>lambdaQuery()
                .eq(ReviewDoc::getStatus, STATUS_AUDITED)
                .eq(ReviewDoc::getProcessStatus, PROCESS_IMAGE_DONE)
                .and(w -> w.eq(ReviewDoc::getSyncStatus, SYNC_WAIT)
                    .or(x -> x.eq(ReviewDoc::getSyncStatus, SYNC_FAIL)
                        .lt(ReviewDoc::getSyncRetryCount, maxRetry)))
                .orderByAsc(ReviewDoc::getUpdateTime));
        List<ReviewDoc> docs = page.getRecords();
        if (docs.isEmpty()) {
            return 0;
        }
        int success = 0;
        for (ReviewDoc doc : docs) {
            if (syncDoc(doc)) {
                success++;
            }
        }
        log.info("文档同步任务结束 待同步={} 成功={} 失败={} cost={}ms",
            docs.size(), success, docs.size() - success, System.currentTimeMillis() - start);
        return success;
    }

    /**
     * 手动立即同步单条（页面按钮调用）
     *
     * @return 是否成功
     */
    public boolean syncNow(Long docId) {
        ReviewDoc doc = docMapper.selectById(docId);
        if (doc == null) {
            throw new ServiceException("文档不存在");
        }
        return syncDoc(doc);
    }

    private boolean syncDoc(ReviewDoc doc) {
        long start = System.currentTimeMillis();
        try {
            if (!STATUS_AUDITED.equals(doc.getStatus()) || !PROCESS_IMAGE_DONE.equals(doc.getProcessStatus())) {
                throw new ServiceException("文档还未完成审核与图片转换，不能同步");
            }
            if (StringUtils.isBlank(doc.getMaskFilePath())) {
                throw new ServiceException("脱敏文件不存在，请重新脱敏");
            }
            List<SysFile> pageFiles = new ArrayList<>();
            for (SysFile file : sysFileService.listByBatch(DocImageService.BATCH_PAGE_PREFIX + doc.getId())) {
                if (DocImageService.BIZ_TYPE_PAGE.equals(file.getBizType())) {
                    pageFiles.add(file);
                }
            }
            if (pageFiles.isEmpty()) {
                throw new ServiceException("页面图片不存在，请重新转图片");
            }
            SysFile maskFile = doc.getMaskFileId() == null ? null : sysFileService.getById(doc.getMaskFileId());
            DocSyncDTO dto = new DocSyncDTO();
            dto.setSourceType(SOURCE_TYPE_REVIEW_MASK);
            dto.setSourceBizId(doc.getId());
            dto.setDocName(buildDocName(doc.getDocName()));
            dto.setProjectName(doc.getProjectName());
            dto.setProvinceCode(doc.getProvinceCode());
            dto.setAmount(doc.getAmount());
            dto.setFileUrl(doc.getMaskFilePath());
            dto.setFileSize(maskFile == null ? null : maskFile.getFileSize());
            List<DocPageDTO> pages = new ArrayList<>(pageFiles.size());
            int pageNo = 0;
            for (SysFile file : pageFiles) {
                DocPageDTO page = new DocPageDTO();
                page.setPageNo(++pageNo);
                page.setFileUrl(file.getOssUrl());
                page.setFileSize(file.getFileSize());
                pages.add(page);
            }
            dto.setPages(pages);
            Long docInfoId = docSyncService.sync(dto);
            docMapper.update(null, Wrappers.<ReviewDoc>lambdaUpdate()
                .eq(ReviewDoc::getId, doc.getId())
                .set(ReviewDoc::getSyncStatus, SYNC_DONE)
                .set(ReviewDoc::getSyncTime, new Date())
                .set(ReviewDoc::getSyncError, null)
                .set(ReviewDoc::getSyncRetryCount, 0)
                .set(ReviewDoc::getUpdateTime, new Date()));
            log.info("文档同步成功 docId={} docInfoId={} 页数={} cost={}ms",
                doc.getId(), docInfoId, pages.size(), System.currentTimeMillis() - start);
            return true;
        } catch (Exception e) {
            markFailed(doc, e.getMessage());
            log.warn("文档同步失败 docId={} docName={} reason={} cost={}ms",
                doc.getId(), doc.getDocName(), e.getMessage(), System.currentTimeMillis() - start);
            return false;
        }
    }

    private void markFailed(ReviewDoc doc, String message) {
        String error = StringUtils.substring(message, 0, 500);
        int retry = (doc.getSyncRetryCount() == null ? 0 : doc.getSyncRetryCount()) + 1;
        docMapper.update(null, Wrappers.<ReviewDoc>lambdaUpdate()
            .eq(ReviewDoc::getId, doc.getId())
            .set(ReviewDoc::getSyncStatus, SYNC_FAIL)
            .set(ReviewDoc::getSyncError, error)
            .set(ReviewDoc::getSyncRetryCount, retry)
            .set(ReviewDoc::getUpdateTime, new Date()));
    }

    /**
     * 文档库里的名称用脱敏文件的名字，跟下载下来的文件名保持一致
     */
    private String buildDocName(String docName) {
        String name = StringUtils.isBlank(docName) ? "投标文件" : docName;
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            name = name.substring(0, dot);
        }
        return name + "-脱敏.docx";
    }

}
