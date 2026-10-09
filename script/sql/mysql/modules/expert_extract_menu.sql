-- ============================================================
-- 专家抽取：菜单和按钮权限
-- 菜单ID使用 2200-2230 段，如果已被占用请自行换一段空闲的
-- 执行后重新登录（或清一次缓存），菜单和按钮权限才会生效
-- ============================================================

-- 重复执行前先清理（第一次执行可以跳过这段）
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2200 AND 2230;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2200 AND 2230;

-- ------------------------------------------------------------
-- 1) 一级目录
-- ------------------------------------------------------------
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (2200, '专家抽取', 0, 7, 'expert', NULL, '',
        1, 0, 'M', '0', '0', '', 'peoples',
        'admin', sysdate(), '', NULL, '专家抽取目录');

-- ------------------------------------------------------------
-- 2) 专家库
-- ------------------------------------------------------------
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (2210, '专家库', 2200, 1, 'info', 'lims/expert/info/index', '',
        1, 0, 'C', '0', '0', 'expert:info:list', 'user',
        'admin', sysdate(), '', NULL, '专家库-专家列表');

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (2211, '专家查询', 2210, 1, '#', '', '', 1, 0, 'F', '0', '0', 'expert:info:query',  '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (2212, '专家新增', 2210, 2, '#', '', '', 1, 0, 'F', '0', '0', 'expert:info:add',    '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (2213, '专家修改', 2210, 3, '#', '', '', 1, 0, 'F', '0', '0', 'expert:info:edit',   '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (2214, '专家删除', 2210, 4, '#', '', '', 1, 0, 'F', '0', '0', 'expert:info:remove', '#', 'admin', sysdate(), '', NULL, '');

-- ------------------------------------------------------------
-- 3) 抽取申请
-- ------------------------------------------------------------
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (2220, '抽取申请', 2200, 2, 'extract', 'lims/expert/extract/index', '',
        1, 0, 'C', '0', '0', 'expert:extract:list', 'form',
        'admin', sysdate(), '', NULL, '专家抽取-申请列表');

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (2221, '申请查询', 2220, 1, '#', '', '', 1, 0, 'F', '0', '0', 'expert:extract:query',      '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (2222, '申请新增', 2220, 2, '#', '', '', 1, 0, 'F', '0', '0', 'expert:extract:add',        '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (2223, '申请修改', 2220, 3, '#', '', '', 1, 0, 'F', '0', '0', 'expert:extract:edit',       '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (2224, '申请删除', 2220, 4, '#', '', '', 1, 0, 'F', '0', '0', 'expert:extract:remove',     '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (2225, '提交审核', 2220, 5, '#', '', '', 1, 0, 'F', '0', '0', 'expert:extract:submit',     '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (2226, '申请审核', 2220, 6, '#', '', '', 1, 0, 'F', '0', '0', 'expert:extract:audit',      '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (2227, '执行抽取', 2220, 7, '#', '', '', 1, 0, 'F', '0', '0', 'expert:extract:draw',       '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (2228, '补充抽取', 2220, 8, '#', '', '', 1, 0, 'F', '0', '0', 'expert:extract:supplement', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (2229, '专家状态变更', 2220, 9, '#', '', '', 1, 0, 'F', '0', '0', 'expert:extract:status', '#', 'admin', sysdate(), '', NULL, '');

-- ------------------------------------------------------------
-- 4) 申请详情页（隐藏菜单，只用于挂路由）
-- ------------------------------------------------------------
INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query_param,
                     is_frame, is_cache, menu_type, visible, status, perms, icon,
                     create_by, create_time, update_by, update_time, remark)
VALUES (2230, '抽取申请详情', 2200, 3, 'extract-detail', 'lims/expert/extract/detail', '',
        1, 0, 'C', '1', '0', 'expert:extract:query', '#',
        'admin', sysdate(), '', NULL, '专家抽取-申请详情，隐藏菜单');

-- ------------------------------------------------------------
-- 5) 授权给普通角色；admin 角色默认拥有全部权限，不用授权
-- ------------------------------------------------------------
INSERT INTO sys_role_menu VALUES ('2', '2200');
INSERT INTO sys_role_menu VALUES ('2', '2210');
INSERT INTO sys_role_menu VALUES ('2', '2211');
INSERT INTO sys_role_menu VALUES ('2', '2212');
INSERT INTO sys_role_menu VALUES ('2', '2213');
INSERT INTO sys_role_menu VALUES ('2', '2214');
INSERT INTO sys_role_menu VALUES ('2', '2220');
INSERT INTO sys_role_menu VALUES ('2', '2221');
INSERT INTO sys_role_menu VALUES ('2', '2222');
INSERT INTO sys_role_menu VALUES ('2', '2223');
INSERT INTO sys_role_menu VALUES ('2', '2224');
INSERT INTO sys_role_menu VALUES ('2', '2225');
INSERT INTO sys_role_menu VALUES ('2', '2226');
INSERT INTO sys_role_menu VALUES ('2', '2227');
INSERT INTO sys_role_menu VALUES ('2', '2228');
INSERT INTO sys_role_menu VALUES ('2', '2229');
INSERT INTO sys_role_menu VALUES ('2', '2230');
