-- ============================================================================
-- 南宁通信段劳动用工工时工分统计管理系统 - 初始化种子数据
-- 包含：组织架构、默认管理员账号、示例人员
-- 密码统一使用 BCrypt 加密，默认密码: 123456
-- BCrypt密文: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
-- ============================================================================

USE nanning_workload;

-- ============================================================================
-- 1. 组织架构初始化（段 -> 车间 -> 工区 + 车间本级）
-- ============================================================================

-- 1.1 创建段级组织（根节点）
INSERT INTO org_unit (id, parent_id, org_name, org_type, area_code, sort_order, enabled, remark) VALUES
(1, 0, '南宁通信段', 'SECTION', NULL, 1, 1, '段级组织');

-- 1.2 创建车间（15个车间，这里先创建4个示例车间）
INSERT INTO org_unit (id, parent_id, org_name, org_type, area_code, sort_order, enabled, remark) VALUES
(10, 1, '贺州通信车间', 'WORKSHOP', NULL, 1, 1, '示例车间'),
(20, 1, '桂林通信车间', 'WORKSHOP', NULL, 2, 1, '示例车间'),
(30, 1, '柳州通信车间', 'WORKSHOP', NULL, 3, 1, '示例车间'),
(40, 1, '南宁通信车间', 'WORKSHOP', NULL, 4, 1, '示例车间');

-- 1.3 为贺州通信车间创建车间本级和下属工区
INSERT INTO org_unit (id, parent_id, org_name, org_type, area_code, sort_order, enabled, remark) VALUES
(11, 10, '贺州车间本级', 'WORKSHOP_LEVEL', NULL, 1, 1, '车间本级'),
(12, 10, '梧州通信工区', 'WORK_AREA', 'A', 1, 1, '贺州车间下属工区'),
(13, 10, '道州通信工区', 'WORK_AREA', 'B', 2, 1, '贺州车间下属工区'),
(14, 10, '贺州通信工区', 'WORK_AREA', 'C', 3, 1, '贺州车间下属工区'),
(15, 10, '岑溪通信工区', 'WORK_AREA', 'D', 4, 1, '贺州车间下属工区');

-- 1.4 为桂林通信车间创建车间本级和下属工区
INSERT INTO org_unit (id, parent_id, org_name, org_type, area_code, sort_order, enabled, remark) VALUES
(21, 20, '桂林车间本级', 'WORKSHOP_LEVEL', NULL, 1, 1, '车间本级'),
(22, 20, '桂林通信工区', 'WORK_AREA', 'A', 1, 1, '桂林车间下属工区'),
(23, 20, '全州通信工区', 'WORK_AREA', 'B', 2, 1, '桂林车间下属工区'),
(24, 20, '兴安通信工区', 'WORK_AREA', 'C', 3, 1, '桂林车间下属工区');

-- 1.5 为柳州通信车间创建车间本级和下属工区
INSERT INTO org_unit (id, parent_id, org_name, org_type, area_code, sort_order, enabled, remark) VALUES
(31, 30, '柳州车间本级', 'WORKSHOP_LEVEL', NULL, 1, 1, '车间本级'),
(32, 30, '柳州通信工区', 'WORK_AREA', 'A', 1, 1, '柳州车间下属工区'),
(33, 30, '来宾通信工区', 'WORK_AREA', 'B', 2, 1, '柳州车间下属工区'),
(34, 30, '宜州通信工区', 'WORK_AREA', 'C', 3, 1, '柳州车间下属工区');

-- 1.6 为南宁通信车间创建车间本级和下属工区
INSERT INTO org_unit (id, parent_id, org_name, org_type, area_code, sort_order, enabled, remark) VALUES
(41, 40, '南宁车间本级', 'WORKSHOP_LEVEL', NULL, 1, 1, '车间本级'),
(42, 40, '南宁通信工区', 'WORK_AREA', 'A', 1, 1, '南宁车间下属工区'),
(43, 40, '崇左通信工区', 'WORK_AREA', 'B', 2, 1, '南宁车间下属工区'),
(44, 40, '凭祥通信工区', 'WORK_AREA', 'C', 3, 1, '南宁车间下属工区');

-- ============================================================================
-- 2. 系统用户账号初始化
-- 密码均为: 123456 (BCrypt加密)
-- ============================================================================

-- 2.1 段级管理员账号
INSERT INTO sys_user (id, username, password, real_name, phone, enabled, first_login) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '段级管理员', '13800000001', 1, 0);

-- 2.2 贺州通信车间管理员账号（演示一个车间多个管理员）
INSERT INTO sys_user (id, username, password, real_name, phone, enabled, first_login) VALUES
(10, 'hezhou_admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '贺州车间管理员A', '13800000010', 1, 1),
(11, 'hezhou_admin2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '贺州车间管理员B', '13800000011', 1, 1);

-- 2.3 桂林通信车间管理员
INSERT INTO sys_user (id, username, password, real_name, phone, enabled, first_login) VALUES
(20, 'guilin_admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '桂林车间管理员', '13800000020', 1, 1);

-- 2.4 工区填报员账号
INSERT INTO sys_user (id, username, password, real_name, phone, enabled, first_login) VALUES
(101, 'wuzhou_user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '梧州工区填报员', '13800000101', 1, 1),
(102, 'daozhou_user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '道州工区填报员', '13800000102', 1, 1),
(103, 'hezhou_user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '贺州工区填报员', '13800000103', 1, 1),
(104, 'cenxi_user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '岑溪工区填报员', '13800000104', 1, 1);

-- ============================================================================
-- 3. 用户组织角色绑定
-- ============================================================================

-- 3.1 段级管理员（全段权限，org_id=0表示全段）
INSERT INTO sys_user_org_scope (user_id, org_id, role_code, scope_type) VALUES
(1, 0, 'SECTION_ADMIN', 'ALL');

-- 3.2 贺州车间管理员（绑定贺州通信车间，演示一个车间多个管理员）
INSERT INTO sys_user_org_scope (user_id, org_id, role_code, scope_type) VALUES
(10, 10, 'WORKSHOP_ADMIN', 'WORKSHOP'),
(11, 10, 'WORKSHOP_ADMIN', 'WORKSHOP');

-- 3.3 桂林车间管理员
INSERT INTO sys_user_org_scope (user_id, org_id, role_code, scope_type) VALUES
(20, 20, 'WORKSHOP_ADMIN', 'WORKSHOP');

-- 3.4 工区填报员（各自绑定对应的工区）
INSERT INTO sys_user_org_scope (user_id, org_id, role_code, scope_type) VALUES
(101, 12, 'AREA_REPORTER', 'AREA'),
(102, 13, 'AREA_REPORTER', 'AREA'),
(103, 14, 'AREA_REPORTER', 'AREA'),
(104, 15, 'AREA_REPORTER', 'AREA');

-- ============================================================================
-- 4. 示例人员信息
-- ============================================================================

INSERT INTO employee (id, name, gender, unit_name, department_name, workshop_id, team_name, area_id, birth_date, position_name, professional_post_type, work_type, rank_category, employee_status, enabled) VALUES
(1, '张三', '男', '南宁通信段', '贺州通信车间', 10, '梧州通信工区', 12, '1990-03-15', '通信工', '通信工', '线路维护', '中级', '在岗', 1),
(2, '李四', '男', '南宁通信段', '贺州通信车间', 10, '梧州通信工区', 12, '1992-07-20', '通信工', '通信工', '线路维护', '初级', '在岗', 1),
(3, '王五', '女', '南宁通信段', '贺州通信车间', 10, '道州通信工区', 13, '1988-11-08', '通信工', '通信工', '机房维护', '高级', '在岗', 1),
(4, '赵六', '男', '南宁通信段', '贺州通信车间', 10, '贺州通信工区', 14, '1995-01-25', '通信工', '通信工', '设备维护', '初级', '在岗', 1),
(5, '陈七', '男', '南宁通信段', '贺州通信车间', 10, '岑溪通信工区', 15, '1991-09-12', '通信工', '通信工', '线路维护', '中级', '在岗', 1),
(6, '周八', '男', '南宁通信段', '桂林通信车间', 20, '桂林通信工区', 22, '1993-05-30', '通信工', '通信工', '机房维护', '中级', '在岗', 1),
(7, '吴九', '女', '南宁通信段', '柳州通信车间', 30, '柳州通信工区', 32, '1994-12-18', '通信工', '通信工', '设备维护', '初级', '在岗', 1),
(8, '郑十', '男', '南宁通信段', '南宁通信车间', 40, '南宁通信工区', 42, '1989-08-22', '通信工', '通信工', '线路维护', '高级', '在岗', 1);
