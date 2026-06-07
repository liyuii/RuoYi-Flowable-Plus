-- 测试记录管理菜单（追加到 lims_menu.sql 尾部）
INSERT INTO `sys_menu` VALUES (2004, '测试记录管理', 2000, 4, 'testDemo', 'lims/testDemo/index', '', 1, 0, 'C', '0', '0', 'lims:testDemo:list', 'table', 'admin', sysdate(), '', NULL, '测试记录管理菜单');
INSERT INTO sys_role_menu VALUES ('2', '2004');