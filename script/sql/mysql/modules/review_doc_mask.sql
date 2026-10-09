-- ============================================================
-- 投标方案库：人工审核文档表新增“脱敏文件”相关字段
-- 说明：
--   1. 脱敏（把已确认的敏感词替换成类型化掩码，如【机构名】）在“审核完成”时自动执行；
--   2. 脱敏文件是流程的最终产物：按约定上传 OSS（biz_type=review_masked），
--      本地工作目录只留一份 masked.docx 副本；
--   3. 流程状态字段 process_status 是 varchar，新增的 WAIT_MASK/待脱敏、MASK_DONE/脱敏完成
--      两个取值直接复用，不需要再改表结构。
-- ============================================================

-- 1) 脱敏文件 OSS 地址（已执行过可跳过）
ALTER TABLE `review_doc`
    ADD COLUMN `mask_file_path` VARCHAR(500) NULL COMMENT '脱敏后文件 OSS 地址' AFTER `extract_file_path`;

-- 2) 本次新增：脱敏文件在 sys_file 表中的 id，用于重新脱敏时删除上一版 OSS 对象
ALTER TABLE `review_doc`
    ADD COLUMN `mask_file_id` BIGINT NULL COMMENT '脱敏文件在 sys_file 表中的 id（文件存 OSS）' AFTER `mask_file_path`;

-- ------------------------------------------------------------
-- 上一阶段遗留（已执行过可跳过）：
-- 识别来源要存 AI / REGEX，原 char(1) 会报 Data truncation，需扩成 VARCHAR(16)
-- ------------------------------------------------------------
ALTER TABLE `review_span`
    MODIFY COLUMN `source` VARCHAR(16) NULL COMMENT '来源：AI 模型识别，REGEX 正则识别，A 人工补充';
