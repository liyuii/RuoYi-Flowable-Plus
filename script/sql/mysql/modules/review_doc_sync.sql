-- ============================================================
-- 投标方案库：人工审核文档表新增“转图片 / 同步”相关字段
-- 说明：
--   1. 脱敏完成后进入转图片阶段，process_status 新增三个取值（varchar 值，不需要 DDL）：
--      WAIT_IMAGE 待转图片 / IMAGE_RUNNING 转图中 / IMAGE_DONE 图片已生成等待同步；
--   2. 页面图片转好后登记到 sys_file（biz_type = review_page，biz_id = review_doc.id，
--      batch_id = review_page_{docId}），定时任务按这个批次读出来同步到文档库；
--   3. sync_status 取值：WAIT_SYNC 待同步 / SYNC_DONE 已同步 / SYNC_FAIL 同步失败。
-- ============================================================

ALTER TABLE `review_doc`
    ADD COLUMN `sync_status` VARCHAR(20) NOT NULL DEFAULT 'WAIT_SYNC'
        COMMENT '同步状态：WAIT_SYNC待同步 / SYNC_DONE已同步 / SYNC_FAIL同步失败' AFTER `retry_count`,
    ADD COLUMN `sync_time` DATETIME NULL COMMENT '最近一次同步成功时间' AFTER `sync_status`,
    ADD COLUMN `sync_error` VARCHAR(500) NULL COMMENT '最近一次同步失败原因，成功后清空' AFTER `sync_time`,
    ADD COLUMN `sync_retry_count` INT NOT NULL DEFAULT 0 COMMENT '同步重试次数' AFTER `sync_error`,
    ADD COLUMN `image_retry_count` INT NOT NULL DEFAULT 0 COMMENT '转图片重试次数' AFTER `sync_retry_count`;

-- ------------------------------------------------------------
-- （可选）历史数据：已经脱敏完成、但还没转图片的记录，直接推进到“待转图”，
-- 下一轮兜底任务会自动补图片，不用重新走一遍审核。
-- 注意：如果你的记录当前是 WAIT_REVIEW / WAIT_MASK，不要执行这条，正常走“审核完成”即可。
-- ------------------------------------------------------------
UPDATE `review_doc`
SET `process_status` = 'WAIT_IMAGE',
    `sync_status` = 'WAIT_SYNC',
    `image_retry_count` = 0,
    `sync_error` = NULL
WHERE `del_flag` = '0'
  AND `status` = '2'
  AND `process_status` = 'MASK_DONE';

-- ------------------------------------------------------------
-- （可选）定时任务按 process_status + sync_status 捞记录，
-- 数据量大了可以加上这个索引；不需要就跳过这条。
-- ------------------------------------------------------------
ALTER TABLE `review_doc` ADD KEY `idx_review_sync` (`process_status`, `sync_status`);
