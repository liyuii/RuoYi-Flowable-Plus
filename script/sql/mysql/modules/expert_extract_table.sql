-- ============================================================
-- 专家抽取：表结构
-- 说明：
--   1. 一共 6 张表：专家库、专家专业、抽取申请、抽取范围、回避名单、抽取结果；
--   2. 标段只做记录，不参与任何抽取判断，直接存在申请表上，不单独建表；
--   3. 评标日期冗余在结果表上，用于「一个专家同一天只能算一次占用」的判断，
--      避免每次查候选都要回连申请表；
--   4. 这一版不加唯一约束，并发窗口内的重复占用由人工发现和处理，
--      表结构保持不变，将来要加唯一索引只需要补一个索引。
-- 执行后请在系统管理中确认字典是否已存在，避免重复插入。
-- ============================================================

-- ------------------------------------------------------------
-- 1. 专家库
-- ------------------------------------------------------------
CREATE TABLE `expert_info` (
  `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `expert_name`  varchar(64)  NOT NULL                COMMENT '专家姓名',
  `id_card`      varchar(32)  NOT NULL                COMMENT '身份证号，回避名单按姓名+身份证号匹配，页面展示需脱敏',
  `phone`        varchar(20)  DEFAULT NULL            COMMENT '联系电话，管理员线下联系专家用',
  `company_name` varchar(255) DEFAULT NULL            COMMENT '工作单位名称，回避名单按单位名称匹配',
  `region_code`  varchar(12)  NOT NULL                COMMENT '所属地区编码，对应字典 expert_region',
  `status`       char(1)      NOT NULL DEFAULT '0'    COMMENT '状态（0正常 1停用），停用的专家不参与抽取',
  `del_flag`     char(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by`    varchar(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`  datetime     DEFAULT NULL            COMMENT '创建时间',
  `update_by`    varchar(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`  datetime     DEFAULT NULL            COMMENT '更新时间',
  `remark`       varchar(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_expert_id_card` (`id_card`),
  KEY `idx_region_status` (`region_code`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评标专家信息表';

-- ------------------------------------------------------------
-- 2. 专家专业
--    一个专家可以有多个专业，所以单独一张表
--    idx_major_expert 是抽取时查候选用的索引，必须建
-- ------------------------------------------------------------
CREATE TABLE `expert_major` (
  `id`         bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `expert_id`  bigint      NOT NULL                COMMENT '专家ID，关联 expert_info.id',
  `major_code` varchar(64) NOT NULL                COMMENT '专业编码，对应字典 expert_major',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_expert_major` (`expert_id`, `major_code`),
  KEY `idx_major_expert` (`major_code`, `expert_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家专业关联表';

-- ------------------------------------------------------------
-- 3. 抽取申请
--    status 取值：
--      DRAFT           草稿
--      WAIT_AUDIT      待审核
--      WAIT_EXTRACT    待抽取（审核通过）
--      EXTRACTED       已抽取
--      WAIT_SUPPLEMENT 补充抽取（有人不参加或请假）
--      REJECTED        审核驳回
-- ------------------------------------------------------------
CREATE TABLE `expert_extract` (
  `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `project_name`  varchar(255) NOT NULL                COMMENT '项目名称',
  `section_names` varchar(500) DEFAULT NULL            COMMENT '标段名称，多个用逗号分隔，只做记录不参与抽取判断',
  `region_code`   varchar(12)  NOT NULL                COMMENT '抽取地区编码，一个申请只能有一个地区',
  `evaluate_time` datetime     NOT NULL                COMMENT '评标时间，判断占用时取其中的日期部分',
  `status`        varchar(20)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT草稿/WAIT_AUDIT待审核/WAIT_EXTRACT待抽取/EXTRACTED已抽取/WAIT_SUPPLEMENT补充抽取/REJECTED审核驳回',
  `apply_by`      varchar(64)  DEFAULT NULL            COMMENT '申请人',
  `apply_time`    datetime     DEFAULT NULL            COMMENT '提交申请时间',
  `audit_by`      varchar(64)  DEFAULT NULL            COMMENT '审核人',
  `audit_time`    datetime     DEFAULT NULL            COMMENT '审核时间',
  `audit_opinion` varchar(500) DEFAULT NULL            COMMENT '审核意见',
  `del_flag`      char(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by`     varchar(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`   datetime     DEFAULT NULL            COMMENT '创建时间',
  `update_by`     varchar(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`   datetime     DEFAULT NULL            COMMENT '更新时间',
  `remark`        varchar(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_evaluate_time` (`evaluate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家抽取申请表';

-- ------------------------------------------------------------
-- 4. 抽取范围
--    一条范围 = 一个专业 + 需要人数，地区从申请表上取
-- ------------------------------------------------------------
CREATE TABLE `expert_extract_scope` (
  `id`         bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `extract_id` bigint      NOT NULL                COMMENT '抽取申请ID，关联 expert_extract.id',
  `major_code` varchar(64) NOT NULL                COMMENT '专业编码，对应字典 expert_major',
  `need_num`   int         NOT NULL DEFAULT '0'    COMMENT '需要人数',
  `order_num`  int         NOT NULL DEFAULT '0'    COMMENT '抽取顺序，前一条范围抽中的人会被后一条排除',
  PRIMARY KEY (`id`),
  KEY `idx_extract` (`extract_id`, `order_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家抽取范围表';

-- ------------------------------------------------------------
-- 5. 抽取回避名单
--    avoid_type = EXPERT  专家姓名 + 身份证号，两者都对上才排除
--    avoid_type = COMPANY 单位名称，对上就排除
-- ------------------------------------------------------------
CREATE TABLE `expert_extract_avoid` (
  `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `extract_id`   bigint       NOT NULL                COMMENT '抽取申请ID，关联 expert_extract.id',
  `avoid_type`   varchar(16)  NOT NULL                COMMENT '回避类型：EXPERT专家 / COMPANY单位',
  `expert_name`  varchar(64)  DEFAULT NULL            COMMENT '专家姓名，avoid_type=EXPERT 时使用',
  `id_card`      varchar(32)  DEFAULT NULL            COMMENT '身份证号，avoid_type=EXPERT 时使用',
  `company_name` varchar(255) DEFAULT NULL            COMMENT '单位名称，avoid_type=COMPANY 时使用',
  PRIMARY KEY (`id`),
  KEY `idx_extract_type` (`extract_id`, `avoid_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家抽取回避名单表';

-- ------------------------------------------------------------
-- 6. 抽取结果
--    status 取值：
--      WAIT_CONFIRM 待确认（占用）
--      JOIN         参加（占用）
--      QUIT         不参加（不占用）
--      LEAVE        请假（不占用）
--    idx_expert_date 是查候选时判断占用的索引，必须建
-- ------------------------------------------------------------
CREATE TABLE `expert_extract_result` (
  `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `extract_id`    bigint       NOT NULL                COMMENT '抽取申请ID，关联 expert_extract.id',
  `scope_id`      bigint       NOT NULL                COMMENT '抽取范围ID，关联 expert_extract_scope.id',
  `expert_id`     bigint       NOT NULL                COMMENT '专家ID，关联 expert_info.id',
  `evaluate_date` date         NOT NULL                COMMENT '评标日期，从申请的评标时间取值，冗余用于占用判断',
  `status`        varchar(16)  NOT NULL DEFAULT 'WAIT_CONFIRM' COMMENT '专家状态：WAIT_CONFIRM待确认/JOIN参加/QUIT不参加/LEAVE请假',
  `reason`        varchar(500) DEFAULT NULL            COMMENT '不参加或请假的理由，请假必填',
  `round_no`      int          NOT NULL DEFAULT '1'    COMMENT '抽取轮次，首次抽取是1，每补抽一次加一',
  `extract_type`  varchar(16)  NOT NULL DEFAULT 'FIRST' COMMENT '抽取类型：FIRST首次抽取 / SUPPLEMENT补充抽取',
  `extract_by`    varchar(64)  DEFAULT NULL            COMMENT '抽取操作人',
  `extract_time`  datetime     DEFAULT NULL            COMMENT '抽取时间',
  `status_by`     varchar(64)  DEFAULT NULL            COMMENT '状态变更人',
  `status_time`   datetime     DEFAULT NULL            COMMENT '状态变更时间',
  `create_time`   datetime     DEFAULT NULL            COMMENT '创建时间',
  `update_time`   datetime     DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_extract` (`extract_id`),
  KEY `idx_expert_date` (`expert_id`, `evaluate_date`, `status`),
  KEY `idx_date_status` (`evaluate_date`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家抽取结果表';


-- ============================================================
-- 字典数据（可选，也可以直接在「系统管理 - 字典管理」里手工加）
-- 如果下面这些 ID 已经被占用，把 ID 换成空闲的即可
-- ============================================================

INSERT INTO `sys_dict_type` VALUES (9001, '专家专业', 'expert_major', '0', 'admin', sysdate(), '', NULL, '评标专家专业分类');
INSERT INTO `sys_dict_type` VALUES (9002, '专家地区', 'expert_region', '0', 'admin', sysdate(), '', NULL, '评标专家所属地区');

-- 专业示例数据，按实际情况替换
INSERT INTO `sys_dict_data` VALUES (9001, 1, '建筑工程', 'BUILD',    'expert_major', '', 'default', 'Y', '0', 'admin', sysdate(), '', NULL, '建筑工程');
INSERT INTO `sys_dict_data` VALUES (9002, 2, '市政工程', 'MUNICIPAL','expert_major', '', 'default', 'N', '0', 'admin', sysdate(), '', NULL, '市政工程');
INSERT INTO `sys_dict_data` VALUES (9003, 3, '水利工程', 'WATER',    'expert_major', '', 'default', 'N', '0', 'admin', sysdate(), '', NULL, '水利工程');
INSERT INTO `sys_dict_data` VALUES (9004, 4, '公路工程', 'ROAD',     'expert_major', '', 'default', 'N', '0', 'admin', sysdate(), '', NULL, '公路工程');

-- 地区示例数据，按实际情况补齐
INSERT INTO `sys_dict_data` VALUES (9011, 1, '石家庄市', '130100', 'expert_region', '', 'default', 'Y', '0', 'admin', sysdate(), '', NULL, '石家庄市');
INSERT INTO `sys_dict_data` VALUES (9012, 2, '保定市',   '130600', 'expert_region', '', 'default', 'N', '0', 'admin', sysdate(), '', NULL, '保定市');
INSERT INTO `sys_dict_data` VALUES (9013, 3, '唐山市',   '130200', 'expert_region', '', 'default', 'N', '0', 'admin', sysdate(), '', NULL, '唐山市');


-- ============================================================
-- 造数据的参考语句（可选，方便先把流程跑通）
-- 手工造几条专家和一个申请，抽取才有的可抽
-- ============================================================

-- INSERT INTO `expert_info` (`expert_name`,`id_card`,`phone`,`company_name`,`region_code`,`status`)
-- VALUES ('张三','130100199001011234','13800000001','XX建筑设计院','130100','0');
-- INSERT INTO `expert_major` (`expert_id`,`major_code`) VALUES (1,'BUILD'), (1,'MUNICIPAL');
