-- 测试表 - 验证开发规范
DROP TABLE IF EXISTS `lims_test_demo`;
CREATE TABLE `lims_test_demo` (
  `id`          bigint(20)   NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `demo_no`     varchar(64)  NOT NULL                 COMMENT '编号',
  `demo_name`   varchar(200) DEFAULT NULL             COMMENT '名称',
  `demo_type`   varchar(32)  DEFAULT NULL             COMMENT '类型',
  `content`     text         DEFAULT NULL             COMMENT '内容',
  `status`      char(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
  `remark`      varchar(500) DEFAULT NULL             COMMENT '备注',
  `del_flag`    char(1)      DEFAULT '0'              COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by`   varchar(64)  DEFAULT NULL             COMMENT '创建者',
  `create_time` datetime     DEFAULT NULL             COMMENT '创建时间',
  `update_by`   varchar(64)  DEFAULT NULL             COMMENT '更新者',
  `update_time` datetime     DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试记录表';