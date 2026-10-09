-- 结果审核（隐藏路由）
INSERT INTO sys_menu VALUES (2007, '结果审核', 2000, 7, 'assay/review', 'lims/assay/review', '', 1, 0, 'C', '1', '0', 'lims:assay:review', 'edit', 'admin', sysdate(), '', NULL, '结果审核');
INSERT INTO sys_role_menu VALUES ('2', '2007');

-- 检测报告（隐藏路由）
INSERT INTO sys_menu VALUES (2008, '检测报告', 2000, 8, 'assay/report', 'lims/assay/report', '', 1, 0, 'C', '1', '0', 'lims:assay:report', 'bugle', 'admin', sysdate(), '', NULL, '检测报告');
INSERT INTO sys_role_menu VALUES ('2', '2008');