-- 功能级数据范围表结构
CREATE TABLE IF NOT EXISTS `sys_data_scope_define` (
  `scope_key`   varchar(100) NOT NULL COMMENT '功能范围标识，如 contract:statistics',
  `scope_name`  varchar(100) NOT NULL COMMENT '范围名称，如 合同统计',
  `status`      char(1)      NOT NULL DEFAULT '0' COMMENT '状态：0 启用，1 停用',
  `remark`      varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by`   varchar(64)  DEFAULT '' COMMENT '创建者',
  `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`   varchar(64)  DEFAULT '' COMMENT '更新者',
  `update_time` datetime     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`scope_key`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '数据范围功能标识';

CREATE TABLE IF NOT EXISTS `sys_role_data_scope` (
  `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id`     bigint       NOT NULL COMMENT '角色ID',
  `scope_key`   varchar(100) NOT NULL COMMENT '功能范围标识，关联 sys_data_scope_define.scope_key',
  `data_scope`  char(1)      NOT NULL COMMENT '1全部/2自定义/3本部门/4本部门及以下/5仅本人',
  `create_by`   varchar(64)  DEFAULT '' COMMENT '创建者',
  `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`   varchar(64)  DEFAULT '' COMMENT '更新者',
  `update_time` datetime     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_scope` (`role_id`, `scope_key`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色-功能数据范围';

CREATE TABLE IF NOT EXISTS `sys_role_data_scope_dept` (
  `id`          bigint   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `scope_id`    bigint   NOT NULL COMMENT '关联 sys_role_data_scope.id',
  `dept_id`     bigint   NOT NULL COMMENT '部门ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scope_dept` (`scope_id`, `dept_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色功能数据范围-自定义部门明细';

INSERT INTO `sys_data_scope_define`
(`scope_key`, `scope_name`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES
('contract:list',       '合同列表', '0', '合同列表数据范围', 'admin', sysdate(), '', NULL),
('contract:statistics', '合同统计', '0', '合同统计数据范围', 'admin', sysdate(), '', NULL),
('contract:template',   '合同模板', '0', '合同模板数据范围', 'admin', sysdate(), '', NULL),
('contract:approve',    '合同审批', '0', '合同审批数据范围', 'admin', sysdate(), '', NULL);
