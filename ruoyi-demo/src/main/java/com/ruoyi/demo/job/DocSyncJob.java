package com.ruoyi.demo.job;

import com.ruoyi.demo.service.DocImageService;
import com.ruoyi.demo.service.ReviewDocSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 投标方案库定时任务：转图片兜底 + 同步到文档库。
 * <p>
 * 用 Spring 的 @Scheduled 跑，将来要上 XXL-Job 只需在方法上加 @XxlJob 注解。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocSyncJob {

    private final DocImageService docImageService;

    private final ReviewDocSyncService reviewDocSyncService;

    @Value("${ruoyi.review.image.enabled:true}")
    private boolean imageEnabled;

    @Value("${ruoyi.review.sync.enabled:true}")
    private boolean syncEnabled;

    /**
     * 转图片兜底任务（默认每分钟）：
     * 正常情况审核完成时已经异步转过了，这里只补偿“进程重启导致异步任务丢失”和“转图失败”的场景。
     */
//    @Scheduled(cron = "${ruoyi.review.image.cron:0 * * * * ?}")
    public void imageJob() {
        if (!imageEnabled) {
            return;
        }
        List<Long> docIds = docImageService.findPendingImageDocIds();
        if (docIds.isEmpty()) {
            return;
        }
        log.info("转图片兜底任务提交 docId={} 数量={}", docIds, docIds.size());
        docIds.forEach(docImageService::convertAsync);
    }

    /**
     * 同步任务（默认每小时整点）：只同步“审核完成 + 图片转换完成”的文档
     */
//    @Scheduled(cron = "${ruoyi.review.sync.cron:0 0 * * * ?}")
    public void syncJob() {
        log.info("开始执行投标方案同步任务");
        if (!syncEnabled) {
            return;
        }
        reviewDocSyncService.syncPendingDocs();
    }

}
