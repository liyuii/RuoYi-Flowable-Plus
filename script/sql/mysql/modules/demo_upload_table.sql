-- 文件管理：分片上传与断点续传演示表
CREATE TABLE IF NOT EXISTS `demo_upload_file` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `upload_id`    VARCHAR(64)  NOT NULL COMMENT '上传会话唯一编号',
  `file_name`    VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `file_size`    BIGINT       NOT NULL COMMENT '文件总大小，字节',
  `file_hash`    VARCHAR(128) NULL COMMENT '整个文件 SHA-256',
  `chunk_size`   INT          NOT NULL COMMENT '分片大小，字节',
  `total_chunks` INT          NOT NULL COMMENT '总片数',
  `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '0 上传中，1 已完成，2 已取消',
  `storage_path` VARCHAR(500) NULL COMMENT '合并后的正式文件路径',
  `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 否，1 是',
  `create_time`  DATETIME     NULL COMMENT '创建时间',
  `update_time`  DATETIME     NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_upload_id` (`upload_id`),
  KEY `idx_hash_status` (`file_hash`, `status`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文件管理-上传文件';

CREATE TABLE IF NOT EXISTS `demo_upload_chunk` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `upload_id`   VARCHAR(64) NOT NULL COMMENT '关联 demo_upload_file.upload_id',
  `chunk_index` INT         NOT NULL COMMENT '分片序号，从 0 开始',
  `chunk_size`  BIGINT      NOT NULL COMMENT '该分片实际字节数',
  `status`      TINYINT     NOT NULL DEFAULT 1 COMMENT '1 已上传',
  `create_time` DATETIME    NULL COMMENT '创建时间',
  `update_time` DATETIME    NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_upload_chunk` (`upload_id`, `chunk_index`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文件管理-分片记录';
