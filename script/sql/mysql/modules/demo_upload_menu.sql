-- 文件管理菜单：顶级目录 + 子菜单

-- 1. 顶级目录
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (3010, '文件上传演示', 0, 10, 'fileDemo', NULL, '',
        1, 0, 'M', '0', '0', '', 'upload',
        'admin', sysdate(), '', NULL, '文件上传演示目录');

-- 2. 文件管理子菜单
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (3011, '文件管理', 3010, 1, 'fileManage', 'demo/fileManage/index', '',
        1, 0, 'C', '0', '0', 'demo:upload:list', 'list',
        'admin', sysdate(), '', NULL, '文件管理-分片上传演示');

-- 3. 授权给普通角色；admin 角色无需授权自动可见
INSERT INTO sys_role_menu VALUES ('2', '3010');
INSERT INTO sys_role_menu VALUES ('2', '3011');
