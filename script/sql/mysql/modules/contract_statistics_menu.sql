-- 合同统计菜单：挂在“合同管理”目录下
-- 菜单ID使用 2120，如已占用请自行更换
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
SELECT 2120, '合同统计', menu_id, 6, 'statistics', 'lims/contract/statistics', '',
       1, 0, 'C', '0', '0', 'lims:contract:statistics', 'chart',
       'admin', sysdate(), '', NULL, '合同统计-数据权限演示'
FROM sys_menu
WHERE menu_name = '合同管理'
  AND menu_type = 'M'
LIMIT 1;

-- 授权给普通角色；admin 角色无需授权自动可见
INSERT INTO sys_role_menu VALUES ('2', '2120');
