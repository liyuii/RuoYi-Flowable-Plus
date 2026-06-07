-- ==============================================================
-- LIMS 检测流程 - 报检单及检测结果样本数据
-- ==============================================================

-- ========================
-- 报检单 1：工业硫酸 98% — 全部合格
-- ========================
INSERT INTO `lims_inspection` (`id`, `inspection_no`, `spec_id`, `batch_no`, `sample_date`, `sample_place`, `applicant`, `status`, `result_verdict`, `remark`, `del_flag`, `create_by`, `create_time`)
VALUES (1, 'INS20260601', 1, 'B24001', '2026-06-01 08:30:00', '硫酸装置 2#生产线', 'admin', '3', 'P', '硫酸装置例行出厂检测', '0', 'admin', '2026-06-01 08:30:00');

INSERT INTO `lims_test_item` (`id`, `inspection_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `result_value`, `unit`, `qc_result`, `test_group_id`, `test_group_name`, `assignee`, `detect_time`, `status`, `remark`, `del_flag`)
VALUES (1, 1, '纯度（H₂SO₄）', '滴定法', 98.00, 99.50, '99.15', '%', 'P', 2, '化学分析组', '王五', '2026-06-01 10:20:00', '3', NULL, '0');

INSERT INTO `lims_test_item` (`id`, `inspection_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `result_value`, `unit`, `qc_result`, `test_group_id`, `test_group_name`, `assignee`, `detect_time`, `status`, `remark`, `del_flag`)
VALUES (2, 1, '铁含量（Fe）', 'AAS原子吸收法', 0, 0.010, '0.005', '%', 'P', 3, '仪器分析组', '钱七', '2026-06-01 11:15:00', '3', NULL, '0');

INSERT INTO `lims_test_item` (`id`, `inspection_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `result_value`, `unit`, `qc_result`, `test_group_id`, `test_group_name`, `assignee`, `detect_time`, `status`, `remark`, `del_flag`)
VALUES (3, 1, '灰分', '灼烧法', 0, 0.03, '0.01', '%', 'P', 1, '物理检测组', '张三', '2026-06-01 14:00:00', '3', NULL, '0');

INSERT INTO `lims_test_item` (`id`, `inspection_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `result_value`, `unit`, `qc_result`, `test_group_id`, `test_group_name`, `assignee`, `detect_time`, `status`, `remark`, `del_flag`)
VALUES (4, 1, '色度', '目视比色法', 0, 50, '20', 'Hazen', 'P', 1, '物理检测组', '李四', '2026-06-01 14:30:00', '3', NULL, '0');

-- ========================
-- 报检单 2：液碱 32% — 部分检测完成（进行中）
-- ========================
INSERT INTO `lims_inspection` (`id`, `inspection_no`, `spec_id`, `batch_no`, `sample_date`, `sample_place`, `applicant`, `status`, `result_verdict`, `remark`, `del_flag`, `create_by`, `create_time`)
VALUES (2, 'INS20260602', 2, 'J24001', '2026-06-02 09:00:00', '液碱装置 1#槽车', 'admin', '1', NULL, '液碱槽车出厂检测', '0', 'admin', '2026-06-02 09:00:00');

INSERT INTO `lims_test_item` (`id`, `inspection_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `result_value`, `unit`, `qc_result`, `test_group_id`, `test_group_name`, `assignee`, `detect_time`, `status`, `remark`, `del_flag`)
VALUES (5, 2, 'NaOH含量', '盐酸滴定法', 32.00, 34.00, '33.20', '%', 'P', 2, '化学分析组', '赵六', '2026-06-02 10:30:00', '3', NULL, '0');

INSERT INTO `lims_test_item` (`id`, `inspection_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `result_value`, `unit`, `qc_result`, `test_group_id`, `test_group_name`, `assignee`, `detect_time`, `status`, `remark`, `del_flag`)
VALUES (6, 2, 'Na₂CO₃含量', '双指示剂滴定法', 0, 0.50, '0.32', '%', 'P', 2, '化学分析组', '王五', '2026-06-02 11:00:00', '3', NULL, '0');

INSERT INTO `lims_test_item` (`id`, `inspection_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `result_value`, `unit`, `qc_result`, `test_group_id`, `test_group_name`, `assignee`, `detect_time`, `status`, `remark`, `del_flag`)
VALUES (7, 2, 'Fe₂O₃含量', 'AAS原子吸收法', 0, 0.005, '0.003', '%', 'P', 3, '仪器分析组', '钱七', '2026-06-02 15:00:00', '3', NULL, '0');

INSERT INTO `lims_test_item` (`id`, `inspection_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `result_value`, `unit`, `qc_result`, `test_group_id`, `test_group_name`, `assignee`, `detect_time`, `status`, `remark`, `del_flag`)
VALUES (8, 2, 'NaCl含量', '硝酸银滴定法', 0, 0.03, NULL, '%', NULL, 2, '化学分析组', NULL, NULL, '0', '待分配——化学组人手不足', '0');

-- ========================
-- 报检单 3：工业磷酸 85% — 完成但铁含量不合格
-- ========================
INSERT INTO `lims_inspection` (`id`, `inspection_no`, `spec_id`, `batch_no`, `sample_date`, `sample_place`, `applicant`, `status`, `result_verdict`, `remark`, `del_flag`, `create_by`, `create_time`)
VALUES (3, 'INS20260603', 3, 'P24001', '2026-06-03 07:50:00', '磷酸装置 3#储罐', 'admin', '3', 'F', '磷酸储罐出厂检测——铁超标，已通知工艺调整', '0', 'admin', '2026-06-03 07:50:00');

INSERT INTO `lims_test_item` (`id`, `inspection_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `result_value`, `unit`, `qc_result`, `test_group_id`, `test_group_name`, `assignee`, `detect_time`, `status`, `remark`, `del_flag`)
VALUES (9, 3, 'H₃PO₄含量', '酸碱滴定法', 85.00, 87.00, '86.20', '%', 'P', 2, '化学分析组', '赵六', '2026-06-03 09:15:00', '3', NULL, '0');

INSERT INTO `lims_test_item` (`id`, `inspection_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `result_value`, `unit`, `qc_result`, `test_group_id`, `test_group_name`, `assignee`, `detect_time`, `status`, `remark`, `del_flag`)
VALUES (10, 3, '硫酸盐（SO₄）', '比浊法', 0, 0.01, '0.008', '%', 'P', 3, '仪器分析组', '孙八', '2026-06-03 09:45:00', '3', NULL, '0');

INSERT INTO `lims_test_item` (`id`, `inspection_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `result_value`, `unit`, `qc_result`, `test_group_id`, `test_group_name`, `assignee`, `detect_time`, `status`, `remark`, `del_flag`)
VALUES (11, 3, '铁含量（Fe）', 'AAS原子吸收法', 0, 0.002, '0.003', '%', 'F', 3, '仪器分析组', '钱七', '2026-06-03 10:00:00', '3', '铁含量0.003%，超出规格上限0.002%', '0');

INSERT INTO `lims_test_item` (`id`, `inspection_id`, `item_name`, `item_method`, `spec_lower`, `spec_upper`, `result_value`, `unit`, `qc_result`, `test_group_id`, `test_group_name`, `assignee`, `detect_time`, `status`, `remark`, `del_flag`)
VALUES (12, 3, '比重（20℃）', '比重计法', 1.70, 1.75, '1.73', 'g/mL', 'P', 1, '物理检测组', '张三', '2026-06-03 13:30:00', '3', NULL, '0');