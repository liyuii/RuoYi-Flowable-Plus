-- ============================================================
-- 用户上传模板：发布目标纠正为文档库（投标方案列表）
-- 背景：之前错误地把审核通过的用户模板写进了方案库表 review_doc，
--       实际上用户模板和平台脱敏文件是两条独立链路，只在“投标方案列表”汇合。
--       新逻辑：审核通过 → 本地文件上传 OSS + sys_file → 直接写文档库 doc_info（source_type=USER_TEMPLATE）
-- ============================================================

-- 1) demo_upload_file：发布关联字段语义调整
--    publish_doc_id 原本存的是 review_doc.id，改成存文档库 doc_info.id
ALTER TABLE `demo_upload_file`
    CHANGE COLUMN `publish_doc_id` `doc_info_id` BIGINT NULL COMMENT '审核通过后写入文档库的 doc_info.id';

--    再补一个 sys_file.id，用于删除文件时连带清理 OSS 对象
ALTER TABLE `demo_upload_file`
    ADD COLUMN `publish_file_id` BIGINT NULL COMMENT '审核通过后上传到 OSS 的 sys_file.id' AFTER `doc_info_id`;

-- 2) 清理之前误插到方案库表 review_doc 的用户模板记录
DELETE FROM `review_doc` WHERE `source_type` = 'USER_UPLOAD';

-- 2.1) 如果之前已经被误同步到文档库，也一并清掉（REVIEW_MASK 的平台记录不受影响）
DELETE FROM `doc_file` WHERE `doc_id` IN (SELECT `id` FROM `doc_info` WHERE `source_type` = 'USER_TEMPLATE');
DELETE FROM `doc_info` WHERE `source_type` = 'USER_TEMPLATE';

-- 2.2) 先看看有哪几条是旧逻辑审核通过的
SELECT `id`, `project_name`, `file_name`, `audit_status`, `doc_info_id`
FROM `demo_upload_file`
WHERE `audit_status` = '1' AND `deleted` = 0;

--      确认后把它们退回“待审核”，重新点一次“审核 → 通过”即可按新逻辑发布到投标方案列表
UPDATE `demo_upload_file`
SET `audit_status` = '0',
    `audit_reason` = NULL,
    `audit_by` = NULL,
    `audit_time` = NULL,
    `doc_info_id` = NULL,
    `publish_file_id` = NULL,
    `update_time` = NOW()
WHERE `audit_status` = '1' AND `deleted` = 0;

-- 3)（可选）方案库表不再需要 source_type 列，确认没有其他用途后可执行；
--    不执行也没影响（实体里已经不再映射这一列）
-- ALTER TABLE `review_doc` DROP COLUMN `source_type`;
