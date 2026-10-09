-- ============================================================
-- 文档管理菜单（挂在“投标方案管理”一级目录下，仅管理员使用，不授权普通角色）
-- 菜单ID使用 4020-4023 段
-- 执行后重新登录（或清一次缓存），菜单与按钮权限才会生效
-- ============================================================

-- 0) 若之前执行过“文档管理一级目录”的那版脚本（菜单ID 4000-4004），先清掉；没执行过请跳过这两条
DELETE FROM sys_role_menu WHERE menu_id IN (4000, 4001, 4002, 4003, 4004);
DELETE FROM sys_menu WHERE menu_id IN (4000, 4001, 4002, 4003, 4004);

-- 1) 文档管理菜单：父级自动取“投标方案管理”目录，order_num 可按需调整
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
SELECT 4020, '文档管理', m.menu_id, 9, 'docInfo', 'doc/info/index', '',
       1, 0, 'C', '0', '0', 'doc:info:list', 'documentation',
       'admin', sysdate(), '', NULL, '文档管理-文档列表'
FROM sys_menu m
WHERE m.menu_name = '投标方案管理'
  AND m.menu_type = 'M'
LIMIT 1;

-- 2) 按钮权限（父级固定为上面的 4020）
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (4021, '文档查询', 4020, 1, '#', '', '',
        1, 0, 'F', '0', '0', 'doc:info:query', '#',
        'admin', sysdate(), '', NULL, '');

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (4022, '文档下载', 4020, 2, '#', '', '',
        1, 0, 'F', '0', '0', 'doc:info:download', '#',
        'admin', sysdate(), '', NULL, '');

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (4023, '文档删除', 4020, 3, '#', '', '',
        1, 0, 'F', '0', '0', 'doc:info:remove', '#',
        'admin', sysdate(), '', NULL, '');

-- 说明：只给 admin 用，所以不插 sys_role_menu；admin 角色默认拥有全部权限
