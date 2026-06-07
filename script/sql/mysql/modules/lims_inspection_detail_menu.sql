-- 报检单详情（隐藏路由，不显示在侧边栏）
INSERT INTO sys_menu VALUES (2005, '报检单详情', 2000, 5, 'inspection/detail/:id', 'lims/inspection/detail', '', 1, 0, 'C', '1', '0', 'lims:inspection:query', 'bugle', 'admin', sysdate(), '', NULL, '报检单详情');
INSERT INTO sys_role_menu VALUES ('2', '2005');