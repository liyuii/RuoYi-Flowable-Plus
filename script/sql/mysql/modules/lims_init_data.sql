-- ==============================================================
-- LIMS 检测流程初始化数据
-- 适用场景：工业硫酸 / 液碱 / 工业磷酸 的入厂/出厂检测
-- 执行顺序：本脚本应在 lims_sample_test.sql 之后运行
-- ==============================================================

-- ========================
-- 1. 检测组
-- ========================
INSERT INTO `lims_test_group` (`id`, `group_name`, `group_code`, `status`, `remark`, `del_flag`, `create_by`, `create_time`)
VALUES (1, '物理检测组', 'PHY', '0', '负责外观、色度、比重、灰分等物理指标检测', '0', 'admin', NOW());

INSERT INTO `lims_test_group` (`id`, `group_name`, `group_code`, `status`, `remark`, `del_flag`, `create_by`, `create_time`)
VALUES (2, '化学分析组', 'CHEM', '0', '负责滴定法、重量法等化学分析检测', '0', 'admin', NOW());

INSERT INTO `lims_test_group` (`id`, `group_name`, `group_code`, `status`, `remark`, `del_flag`, `create_by`, `create_time`)
VALUES (3, '仪器分析组', 'INST', '0', '负责原子吸收、色谱、光谱等仪器分析', '0', 'admin', NOW());

-- ========================
-- 2. 检测组成员（关联 sys_user，测试用 user_id 从 1 起）
-- ========================
INSERT INTO `lims_test_group_member` (`id`, `group_id`, `user_id`, `user_name`, `sort_order`, `del_flag`)
VALUES (1, 1, 2, '张三', 1, '0');
INSERT INTO `lims_test_group_member` (`id`, `group_id`, `user_id`, `user_name`, `sort_order`, `del_flag`)
VALUES (2, 1, 3, '李四', 2, '0');

INSERT INTO `lims_test_group_member` (`id`, `group_id`, `user_id`, `user_name`, `sort_order`, `del_flag`)
VALUES (3, 2, 4, '王五', 1, '0');
INSERT INTO `lims_test_group_member` (`id`, `group_id`, `user_id`, `user_name`, `sort_order`, `del_flag`)
VALUES (4, 2, 5, '赵六', 2, '0');

INSERT INTO `lims_test_group_member` (`id`, `group_id`, `user_id`, `user_name`, `sort_order`, `del_flag`)
VALUES (5, 3, 6, '钱七', 1, '0');
INSERT INTO `lims_test_group_member` (`id`, `group_id`, `user_id`, `user_name`, `sort_order`, `del_flag`)
VALUES (6, 3, 7, '孙八', 2, '0');

-- ========================
-- 3. 规格书
-- ========================
INSERT INTO `lims_specification` (`id`, `spec_name`, `material_name`, `version`, `status`, `remark`, `del_flag`, `create_by`, `create_time`)
VALUES (1, '工业硫酸98% 规格书', '工业硫酸（98%）', 'V1.0', '0', '适用于硫酸装置生产的工业硫酸产品检测', '0', 'admin', NOW());

INSERT INTO `lims_specification` (`id`, `spec_name`, `material_name`, `version`, `status`, `remark`, `del_flag`, `create_by`, `create_time`)
VALUES (2, '液碱32% 规格书', '液碱（32%）', 'V1.0', '0', '适用于离子膜法生产的液碱产品检测', '0', 'admin', NOW());

INSERT INTO `lims_specification` (`id`, `spec_name`, `material_name`, `version`, `status`, `remark`, `del_flag`, `create_by`, `create_time`)
VALUES (3, '工业磷酸85% 规格书', '工业磷酸（85%）', 'V2.0', '0', '适用于热法生产的工业磷酸产品检测', '0', 'admin', NOW());

-- ========================
-- 4. 规格书检测项目
-- ========================

-- 工业硫酸98% 检测项目（4项）
INSERT INTO `lims_spec_item` (`id`, `spec_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `unit`, `test_group_id`, `test_group_name`, `sort_order`, `del_flag`)
VALUES (1, 1, '纯度（H₂SO₄）', '滴定法——用氢氧化钠标准溶液滴定', 98.00, 99.50, '%', 2, '化学分析组', 1, '0');

INSERT INTO `lims_spec_item` (`id`, `spec_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `unit`, `test_group_id`, `test_group_name`, `sort_order`, `del_flag`)
VALUES (2, 1, '铁含量（Fe）', '原子吸收分光光度法（AAS）', 0, 0.010, '%', 3, '仪器分析组', 2, '0');

INSERT INTO `lims_spec_item` (`id`, `spec_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `unit`, `test_group_id`, `test_group_name`, `sort_order`, `del_flag`)
VALUES (3, 1, '灰分', '灼烧法——马弗炉800℃灼烧', 0, 0.03, '%', 1, '物理检测组', 3, '0');

INSERT INTO `lims_spec_item` (`id`, `spec_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `unit`, `test_group_id`, `test_group_name`, `sort_order`, `del_flag`)
VALUES (4, 1, '色度', '目视比色法——铂钴标准比色', 0, 50, 'Hazen', 1, '物理检测组', 4, '0');

-- 液碱32% 检测项目（4项）
INSERT INTO `lims_spec_item` (`id`, `spec_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `unit`, `test_group_id`, `test_group_name`, `sort_order`, `del_flag`)
VALUES (5, 2, 'NaOH含量', '盐酸标准溶液滴定法', 32.00, 34.00, '%', 2, '化学分析组', 1, '0');

INSERT INTO `lims_spec_item` (`id`, `spec_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `unit`, `test_group_id`, `test_group_name`, `sort_order`, `del_flag`)
VALUES (6, 2, 'Na₂CO₃含量', '双指示剂滴定法', 0, 0.50, '%', 2, '化学分析组', 2, '0');

INSERT INTO `lims_spec_item` (`id`, `spec_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `unit`, `test_group_id`, `test_group_name`, `sort_order`, `del_flag`)
VALUES (7, 2, 'Fe₂O₃含量', '原子吸收分光光度法（AAS）', 0, 0.005, '%', 3, '仪器分析组', 3, '0');

INSERT INTO `lims_spec_item` (`id`, `spec_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `unit`, `test_group_id`, `test_group_name`, `sort_order`, `del_flag`)
VALUES (8, 2, 'NaCl含量', '硝酸银滴定法（莫尔法）', 0, 0.03, '%', 2, '化学分析组', 4, '0');

-- 工业磷酸85% 检测项目（4项）
INSERT INTO `lims_spec_item` (`id`, `spec_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `unit`, `test_group_id`, `test_group_name`, `sort_order`, `del_flag`)
VALUES (9, 3, 'H₃PO₄含量', '酸碱滴定法——百里香酚酞指示', 85.00, 87.00, '%', 2, '化学分析组', 1, '0');

INSERT INTO `lims_spec_item` (`id`, `spec_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `unit`, `test_group_id`, `test_group_name`, `sort_order`, `del_flag`)
VALUES (10, 3, '硫酸盐（SO₄）', '比浊法——氯化钡比浊', 0, 0.01, '%', 3, '仪器分析组', 2, '0');

INSERT INTO `lims_spec_item` (`id`, `spec_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `unit`, `test_group_id`, `test_group_name`, `sort_order`, `del_flag`)
VALUES (11, 3, '铁含量（Fe）', '原子吸收分光光度法（AAS）', 0, 0.002, '%', 3, '仪器分析组', 3, '0');

INSERT INTO `lims_spec_item` (`id`, `spec_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `unit`, `test_group_id`, `test_group_name`, `sort_order`, `del_flag`)
VALUES (12, 3, '比重（20℃）', '比重计法——玻璃比重计', 1.70, 1.75, 'g/mL', 1, '物理检测组', 4, '0');