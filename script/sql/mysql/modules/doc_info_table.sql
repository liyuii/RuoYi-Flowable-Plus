-- ============================================================
-- 文档库（模拟“另一个项目 / 另一个数据库”）：文档主表 + 页面图片表
-- 说明：
--   1. 这两张表按“独立系统”设计：不引用 review_doc / sys_file 的实体，只通过同步 DTO 传数据，
--      将来拆库时把表和数据一起搬走即可；
--   2. doc_info 的 source_type + source_biz_id 是幂等键，定时任务重复同步时走更新分支，不会重复插入；
--   3. doc_file 存的是页面图片（只用于详情页预览）；图片 OSS 对象的“户口”在源侧 sys_file，
--      所以删除文档时只删这两张表的记录，不动 OSS 对象。
-- ============================================================

CREATE TABLE `doc_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `doc_name` varchar(255) NOT NULL COMMENT '文档名称',
  `project_name` varchar(255) DEFAULT NULL COMMENT '项目名称',
  `province_code` varchar(6) DEFAULT NULL COMMENT '项目所在省份行政区划代码，如 130000',
  `amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '项目金额（元）',
  `source_type` varchar(32) NOT NULL COMMENT '来源业务类型：REVIEW_MASK 人工审核脱敏文件',
  `source_biz_id` bigint NOT NULL COMMENT '来源业务主键（review_doc.id）',
  `file_url` varchar(500) NOT NULL COMMENT '文档文件 OSS 地址（脱敏后的 docx，下载用）',
  `file_size` bigint DEFAULT NULL COMMENT '文档文件大小（字节）',
  `page_count` int NOT NULL DEFAULT '0' COMMENT '页面图片数量',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_source` (`source_type`,`source_biz_id`),
  KEY `idx_project_name` (`project_name`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档信息表';

CREATE TABLE `doc_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `doc_id` bigint NOT NULL COMMENT '文档信息ID，关联 doc_info.id',
  `file_type` varchar(16) NOT NULL DEFAULT 'PAGE' COMMENT '文件类型：PAGE 页图片',
  `page_no` int NOT NULL DEFAULT '0' COMMENT '页码，从1开始',
  `file_url` varchar(500) NOT NULL COMMENT '图片 OSS 地址',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `width` int DEFAULT NULL COMMENT '图片宽度（像素）',
  `height` int DEFAULT NULL COMMENT '图片高度（像素）',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_doc_page` (`doc_id`,`page_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档文件表（页面图片）';
