-- ==============================================================
-- 检测流程业务模块（LIMS 样品检测）
-- 场景说明：样品报检 → 按规格书展开检测项目 → 按检测组分发
--            → 并行多实例结果录入 → 审核 → 报告签发
-- 前缀约定：lims_* 表示 Laboratory Information Management System
-- 设计版本：v1.0
-- 适用于：RuoYi-Flowable-Plus + Flowable 6.8.0
-- ==============================================================

-- ========================
-- 1. 检测组
-- ========================
DROP TABLE IF EXISTS lims_test_group;
CREATE TABLE lims_test_group (
  id            bigint(20)   NOT NULL AUTO_INCREMENT  COMMENT '主键',
  group_name    varchar(100) NOT NULL                 COMMENT '检测组名称，如：物理检测组、化学分析组',
  group_code    varchar(32)  NOT NULL                 COMMENT '检测组编码，如：PHY、CHEM、INST',
  status        char(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
  
remark        varchar(500) DEFAULT NULL             COMMENT '备注',
  del_flag      char(1)      DEFAULT '0'              COMMENT '删除标志（0代表存在 2代表删除）',
  create_by     varchar(64)  DEFAULT NULL             COMMENT '创建者',
  create_time   datetime     DEFAULT NULL             COMMENT '创建时间',
  update_by     varchar(64)  DEFAULT NULL             COMMENT '更新者',
  update_time   datetime     DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_group_code (group_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测组';

-- ========================
-- 2. 检测组成员
-- ========================
DROP TABLE IF EXISTS lims_test_group_member;
CREATE TABLE lims_test_group_member (
  id            bigint(20)   NOT NULL AUTO_INCREMENT  COMMENT '主键',
  group_id      bigint(20)   NOT NULL                 COMMENT '检测组ID（关联 lims_test_group.id）',
  user_id       bigint(20)   NOT NULL                 COMMENT '用户ID（关联 sys_user.user_id）',
  user_name     varchar(64)  NOT NULL                 COMMENT '用户名称（冗余，方便展示）',
  sort_order    int(11)      DEFAULT '0'              COMMENT '排序号（影响签收优先级）',
  del_flag      char(1)      DEFAULT '0'              COMMENT '删除标志（0代表存在 2代表删除）',
  create_time   datetime     DEFAULT NULL             COMMENT '创建时间',
  update_time   datetime     DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_group_id (group_id),
  UNIQUE KEY uk_group_user (group_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测组成员';

-- ========================
-- 3. 规格书
-- ========================
DROP TABLE IF EXISTS lims_specification;
CREATE TABLE lims_specification (
  id              bigint(20)   NOT NULL AUTO_INCREMENT  COMMENT '主键',
  spec_name       varchar(200) NOT NULL                 COMMENT '规格书名称，如：工业硫酸98% 规格书',
  material_name   varchar(200) NOT NULL                 COMMENT '物料名称',
  version         varchar(32)  DEFAULT NULL             COMMENT '版本号',
  status          char(1)      DEFAULT '0'              COMMENT '状态（0启用 1停用）',
  
remark          varchar(500) DEFAULT NULL             COMMENT '备注',
  del_flag        char(1)      DEFAULT '0'              COMMENT '删除标志（0代表存在 2代表删除）',
  create_by       varchar(64)  DEFAULT NULL             COMMENT '创建者',
  create_time     datetime     DEFAULT NULL             COMMENT '创建时间',
  update_by       varchar(64)  DEFAULT NULL             COMMENT '更新者',
  update_time     datetime     DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规格书';

-- ========================
-- 4. 规格书检测项目定义
-- ========================
DROP TABLE IF EXISTS lims_spec_item;
CREATE TABLE lims_spec_item (
  id              bigint(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
  spec_id         bigint(20)    NOT NULL                 COMMENT '规格书ID（关联 lims_specification.id）',
  item_name       varchar(200)  NOT NULL                 COMMENT '检测项目名称，如：纯度、铁含量、pH值',
  item_method     varchar(200)  DEFAULT NULL             COMMENT '检测方法',
  spec_lower      decimal(10,4) DEFAULT NULL             COMMENT '规格下限',
  spec_upper      decimal(10,4) DEFAULT NULL             COMMENT '规格上限',
  unit            varchar(32)   DEFAULT NULL             COMMENT '单位，如：%、mg/L',
  	test_group_id   bigint(20)    DEFAULT NULL             COMMENT '默认检测组ID（关联 lims_test_group.id）',
  	test_group_name varchar(100)  DEFAULT NULL             COMMENT '检测组名称（冗余）',
  sort_order      int(11)       DEFAULT '0'              COMMENT '排序号',
  
remark          varchar(500)  DEFAULT NULL             COMMENT '备注',
  del_flag        char(1)       DEFAULT '0'              COMMENT '删除标志（0代表存在 2代表删除）',
  create_time     datetime      DEFAULT NULL             COMMENT '创建时间',
  update_time     datetime      DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_spec_id (spec_id),
  KEY idx_test_group_id (	test_group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规格书检测项目定义';

-- ========================
-- 5. 报检单主表
-- ========================
DROP TABLE IF EXISTS lims_inspection;
CREATE TABLE lims_inspection (
  id              bigint(20)   NOT NULL AUTO_INCREMENT  COMMENT '主键',
  inspection_no   varchar(64)  NOT NULL                 COMMENT '报检单号（INS + 年月日 + 流水）',
  spec_id         bigint(20)   NOT NULL                 COMMENT '规格书ID（关联 lims_specification.id）',
  batch_no        varchar(64)  NOT NULL                 COMMENT '生产批号',
  sample_date     datetime     DEFAULT NULL             COMMENT '取样日期',
  sample_place    varchar(200) DEFAULT NULL             COMMENT '取样地点/生产线',
  applicant       varchar(64)  NOT NULL                 COMMENT '申请人账号',
  status          varchar(32)  NOT NULL DEFAULT '0'     COMMENT '业务状态（0待检 1检测中 2审核中 3已完成 4已驳回）',
  
result_verdict  char(1)      DEFAULT NULL             COMMENT '终审结论（P合格 F不合格）',
  
remark          varchar(500) DEFAULT NULL             COMMENT '备注',
  del_flag        char(1)      DEFAULT '0'              COMMENT '删除标志（0代表存在 2代表删除）',
  create_by       varchar(64)  DEFAULT NULL             COMMENT '创建者',
  create_time     datetime     DEFAULT NULL             COMMENT '创建时间',
  update_by       varchar(64)  DEFAULT NULL             COMMENT '更新者',
  update_time     datetime     DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_spec_id (spec_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报检单主表';

-- ========================
-- 6. 检测项目结果表
-- ========================
DROP TABLE IF EXISTS lims_test_item;
CREATE TABLE lims_test_item (
  id              bigint(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
  inspection_id   bigint(20)    NOT NULL                 COMMENT '报检单ID（关联 lims_inspection.id）',
  item_name       varchar(200)  NOT NULL                 COMMENT '检测项目名称',
  item_method     varchar(200)  DEFAULT NULL             COMMENT '检测方法',
  spec_lower      decimal(10,4) DEFAULT NULL             COMMENT '规格下限（源自规格书，快照）',
  spec_upper      decimal(10,4) DEFAULT NULL             COMMENT '规格上限（源自规格书，快照）',
  
result_value    varchar(64)   DEFAULT NULL             COMMENT '检测结果值',
  unit            varchar(32)   DEFAULT NULL             COMMENT '单位',
  qc_result       char(1)       DEFAULT NULL             COMMENT 'QC判定（P合格 F不合格）',
  	test_group_id   bigint(20)    DEFAULT NULL             COMMENT '检测组ID（源自规格书项目）',
  	test_group_name varchar(100)  DEFAULT NULL             COMMENT '检测组名称（冗余）',
  assignee        varchar(64)   DEFAULT NULL             COMMENT '检测人工号',
  detect_time     datetime      DEFAULT NULL             COMMENT '检测时间',
  status          char(1)       NOT NULL DEFAULT '0'     COMMENT '状态（0待分配 1已分配 2检测中 3已完成）',
  
remark          varchar(500)  DEFAULT NULL             COMMENT '备注',
  del_flag        char(1)       DEFAULT '0'              COMMENT '删除标志（0代表存在 2代表删除）',
  create_time     datetime      DEFAULT NULL             COMMENT '创建时间',
  update_time     datetime      DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_inspection_id (inspection_id),
  KEY idx_test_group_id (	test_group_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测项目结果表';
