package com.ruoyi.demo.service;

import cn.hutool.core.io.IoUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.demo.domain.ReviewDoc;
import com.ruoyi.demo.mapper.ReviewDocMapper;
import com.ruoyi.demo.utils.ByteArrayMultipartFile;
import com.ruoyi.demo.utils.DocImageConverter;
import com.ruoyi.oss.service.FileStorageStrategy;
import com.ruoyi.system.domain.SysFile;
import com.ruoyi.system.service.ISysFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 页面图片生成服务。
 * <p>
 * 脱敏文件逐页渲染成 PNG，每页上传 OSS 并登记到 sys_file
 * （biz_type=review_page，biz_id=docId，batch_id=review_page_{docId}），
 * 不落本地图片、不额外建表；后续同步任务按批次读出来写到文档库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocImageService {

    /**
     * 页面图片在 sys_file 中的业务类型
     */
    public static final String BIZ_TYPE_PAGE = "review_page";

    /**
     * 页面图片批次前缀
     */
    public static final String BATCH_PAGE_PREFIX = "review_page_";

    /**
     * 流程状态
     */
    private static final String PROCESS_WAIT_IMAGE = "WAIT_IMAGE";

    private static final String PROCESS_IMAGE_RUNNING = "IMAGE_RUNNING";

    private static final String PROCESS_IMAGE_DONE = "IMAGE_DONE";

    /**
     * 系统生成文件的创建人（异步线程没有登录上下文）
     */
    private static final String CREATE_BY_SYSTEM = "system";

    /**
     * 转图片中的记录超过该分钟数视为卡死，允许兜底任务重跑
     */
    private static final int RUNNING_TIMEOUT_MINUTES = 10;

    /**
     * 本地脱敏文件副本名
     */
    private static final String MASK_FILE_NAME = "masked.docx";

    private final ReviewDocMapper docMapper;

    private final ISysFileService sysFileService;

    private final FileStorageStrategy storageStrategy;

    @Value("${ruoyi.review.work-dir}")
    private String workDir;

    @Value("${ruoyi.review.image.max-pages:200}")
    private int maxPages;

    @Value("${ruoyi.review.image.resolution:150}")
    private float resolution;

    @Value("${ruoyi.review.image.pending-limit:5}")
    private int pendingLimit;

    @Value("${ruoyi.review.image.max-retry:3}")
    private int maxRetry;

    /**
     * 异步转图片：审核完成、重新转图片时提交，不阻塞用户请求
     */
    @Async
    public void convertAsync(Long docId) {
        convert(docId);
    }

    /**
     * 同步执行转图片（兜底任务、手动重试都走这里）
     *
     * @return 是否转换成功
     */
    public boolean convert(Long docId) {
        ReviewDoc doc = docMapper.selectById(docId);
        if (doc == null) {
            log.warn("转图片跳过，文档不存在 docId={}", docId);
            return false;
        }
        if (!claim(docId)) {
            log.info("转图片跳过，当前状态不是待转图片 docId={} processStatus={}", docId, doc.getProcessStatus());
            return false;
        }
        long start = System.currentTimeMillis();
        log.info("开始转图片 docId={} docName={} 源文件={} 分辨率={}dpi 页数上限={}",
            docId, doc.getDocName(), doc.getMaskFilePath(), resolution, maxPages);
        try {
            File source = ensureLocalMask(doc);
            // 重新转图片时先清掉上一批图片（记录 + OSS 对象）
            deletePageImages(docId);
            String batchId = BATCH_PAGE_PREFIX + docId;
            String baseName = baseName(doc.getDocName());
            int pages = DocImageConverter.convert(source.getAbsolutePath(), maxPages, resolution, (pageNo, png) -> {
                String fileName = baseName + "-第" + pageNo + "页.png";
                sysFileService.upload(new ByteArrayMultipartFile(png, fileName, "image/png"),
                    BIZ_TYPE_PAGE, batchId, CREATE_BY_SYSTEM);
            });
            if (pages <= 0) {
                throw new ServiceException("转图片结果为空，文档可能没有内容");
            }
            docMapper.update(null, Wrappers.<ReviewDoc>lambdaUpdate()
                .eq(ReviewDoc::getId, docId)
                .set(ReviewDoc::getProcessStatus, PROCESS_IMAGE_DONE)
                .set(ReviewDoc::getSyncStatus, "WAIT_SYNC")
                .set(ReviewDoc::getSyncError, null)
                .set(ReviewDoc::getProcessError, null)
                .set(ReviewDoc::getImageRetryCount, 0)
                .set(ReviewDoc::getUpdateTime, new Date()));
            log.info("转图片完成 docId={} 页数={} cost={}ms", docId, pages, System.currentTimeMillis() - start);
            return true;
        } catch (ServiceException e) {
            markFailed(docId, e.getMessage());
            log.warn("转图片未成功 docId={} reason={} cost={}ms",
                docId, e.getMessage(), System.currentTimeMillis() - start);
            return false;
        } catch (Exception e) {
            markFailed(docId, e.getMessage());
            log.error("转图片异常 docId={} docName={} cost={}ms",
                docId, doc.getDocName(), System.currentTimeMillis() - start, e);
            return false;
        }
    }

    /**
     * 兜底任务扫描：待转图片、以及转图中超时（进程重启等）的文档
     */
    public List<Long> findPendingImageDocIds() {
        Date timeout = new Date(System.currentTimeMillis() - RUNNING_TIMEOUT_MINUTES * 60_000L);
        return docMapper.selectList(Wrappers.<ReviewDoc>lambdaQuery()
                .select(ReviewDoc::getId)
                // 超过重试上限后不再自动跑，由页面“重新转图片”手动触发
                .lt(ReviewDoc::getImageRetryCount, maxRetry)
                .and(w -> w.eq(ReviewDoc::getProcessStatus, PROCESS_WAIT_IMAGE)
                    .or(x -> x.eq(ReviewDoc::getProcessStatus, PROCESS_IMAGE_RUNNING)
                        .lt(ReviewDoc::getUpdateTime, timeout)))
                .orderByAsc(ReviewDoc::getUpdateTime)
                .last("limit " + pendingLimit))
            .stream()
            .map(ReviewDoc::getId)
            .collect(Collectors.toList());
    }

    /**
     * 删除某个文档的所有页面图片（sys_file 记录 + OSS 对象）
     *
     * @return 删除的图片数量
     */
    public int deletePageImages(Long docId) {
        List<SysFile> files = sysFileService.listByBatch(BATCH_PAGE_PREFIX + docId);
        int deleted = 0;
        for (SysFile file : files) {
            if (!BIZ_TYPE_PAGE.equals(file.getBizType())) {
                continue;
            }
            try {
                sysFileService.deleteById(file.getId());
                deleted++;
            } catch (Exception e) {
                log.warn("删除页面图片失败 docId={} fileId={} reason={}", docId, file.getId(), e.getMessage());
            }
        }
        return deleted;
    }

    /**
     * 抢占：只有“待转图片”才允许转，避免异步任务和兜底任务重复渲染同一份文档
     */
    private boolean claim(Long docId) {
        return docMapper.update(null, Wrappers.<ReviewDoc>lambdaUpdate()
            .eq(ReviewDoc::getId, docId)
            .eq(ReviewDoc::getProcessStatus, PROCESS_WAIT_IMAGE)
            .set(ReviewDoc::getProcessStatus, PROCESS_IMAGE_RUNNING)
            .set(ReviewDoc::getUpdateTime, new Date())) > 0;
    }

    private void markFailed(Long docId, String message) {
        ReviewDoc doc = docMapper.selectById(docId);
        int retry = (doc == null || doc.getImageRetryCount() == null ? 0 : doc.getImageRetryCount()) + 1;
        docMapper.update(null, Wrappers.<ReviewDoc>lambdaUpdate()
            .eq(ReviewDoc::getId, docId)
            .set(ReviewDoc::getProcessStatus, PROCESS_WAIT_IMAGE)
            .set(ReviewDoc::getProcessError, StringUtils.substring(message, 0, 500))
            .set(ReviewDoc::getImageRetryCount, retry)
            .set(ReviewDoc::getUpdateTime, new Date()));
    }

    /**
     * 确保本地存在脱敏文件副本，没有就从 OSS 拉一份（转换需要本地文件）
     */
    private File ensureLocalMask(ReviewDoc doc) {
        File dir = new File(workDir, String.valueOf(doc.getId()));
        if ((!dir.exists() && !dir.mkdirs()) || !dir.isDirectory()) {
            throw new ServiceException("创建本地工作目录失败: " + dir.getAbsolutePath());
        }
        File local = new File(dir, MASK_FILE_NAME);
        if (local.exists() && local.length() > 0) {
            return local;
        }
        if (StringUtils.isBlank(doc.getMaskFilePath())) {
            throw new ServiceException("脱敏文件不存在，请先完成审核");
        }
        long start = System.currentTimeMillis();
        try (InputStream in = storageStrategy.getContent(doc.getMaskFilePath());
             OutputStream out = Files.newOutputStream(local.toPath())) {
            IoUtil.copy(in, out);
        } catch (Exception e) {
            throw new ServiceException("下载脱敏文件失败: " + e.getMessage());
        }
        log.info("脱敏文件下载完成 docId={} ossUrl={} size={}B cost={}ms",
            doc.getId(), doc.getMaskFilePath(), local.length(), System.currentTimeMillis() - start);
        return local;
    }

    private String baseName(String docName) {
        String name = StringUtils.isBlank(docName) ? "投标文件" : docName;
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

}
