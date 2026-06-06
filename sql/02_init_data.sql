-- ============================================================================
-- 用工项目字典初始化数据
-- 包含全部26个默认用工项目分类及其完整树形结构
-- 类型说明：
--   CATEGORY - 分类项（仅作分类节点，不可填写）
--   NUMBER   - 数值项（可填写分钟或工分）
--   TEXT     - 文本项（填写文字内容，如备注、培训班名称）
-- 报表类型：HOURS-工时, POINTS-工分, BOTH-通用
-- ============================================================================

USE nanning_workload;

-- 启用变量跟踪当前插入的父ID
SET @cat_id = 0;

-- ============================================================================
-- 1. 施工（顶级分类1）- 包含I/II/III级施工，每级含准备时间、往返路程时间、施工时间、备注
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '施工', '施工', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 1);
SET @cat_id = LAST_INSERT_ID();

-- 施工 / Ⅰ级施工
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, 'Ⅰ级施工', '施工/Ⅰ级施工', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 1);
SET @sub_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, '准备时间', '施工/Ⅰ级施工/准备时间', 3, 'NUMBER', 'HOURS', '分钟', 0, 1, 0, NULL, 1),
(@sub_id, '往返路程时间', '施工/Ⅰ级施工/往返路程时间', 3, 'NUMBER', 'HOURS', '分钟', 0, 1, 0, NULL, 2),
(@sub_id, '施工时间', '施工/Ⅰ级施工/施工时间', 3, 'NUMBER', 'HOURS', '分钟', 0, 1, 0, NULL, 3),
(@sub_id, '备注', '施工/Ⅰ级施工/备注', 3, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 4);

-- 施工 / Ⅱ级施工
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, 'Ⅱ级施工', '施工/Ⅱ级施工', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 2);
SET @sub_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, '准备时间', '施工/Ⅱ级施工/准备时间', 3, 'NUMBER', 'HOURS', '分钟', 0, 1, 0, NULL, 1),
(@sub_id, '往返路程时间', '施工/Ⅱ级施工/往返路程时间', 3, 'NUMBER', 'HOURS', '分钟', 0, 1, 0, NULL, 2),
(@sub_id, '施工时间', '施工/Ⅱ级施工/施工时间', 3, 'NUMBER', 'HOURS', '分钟', 0, 1, 0, NULL, 3),
(@sub_id, '备注', '施工/Ⅱ级施工/备注', 3, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 4);

-- 施工 / Ⅲ级施工
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, 'Ⅲ级施工', '施工/Ⅲ级施工', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 3);
SET @sub_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, '准备时间', '施工/Ⅲ级施工/准备时间', 3, 'NUMBER', 'HOURS', '分钟', 0, 1, 0, NULL, 1),
(@sub_id, '往返路程时间', '施工/Ⅲ级施工/往返路程时间', 3, 'NUMBER', 'HOURS', '分钟', 0, 1, 0, NULL, 2),
(@sub_id, '施工时间', '施工/Ⅲ级施工/施工时间', 3, 'NUMBER', 'HOURS', '分钟', 0, 1, 0, NULL, 3),
(@sub_id, '备注', '施工/Ⅲ级施工/备注', 3, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 4);

-- ============================================================================
-- 2. 施工配合
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '施工配合', '施工配合', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 2);
SET @cat_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '施工配合时间', '施工配合/施工配合时间', 2, 'NUMBER', 'HOURS', '分钟', 0, 1, 0, NULL, 1),
(@cat_id, '备注', '施工配合/备注', 2, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 2);

-- ============================================================================
-- 3. 维修
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '维修', '维修', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 3);
SET @cat_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '维修时间', '维修/维修时间', 2, 'NUMBER', 'HOURS', '分钟', 0, 1, 0, NULL, 1),
(@cat_id, '备注', '维修/备注', 2, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 2);

-- ============================================================================
-- 4. 核心网/数据网/传输网/数调系统软件升级/网络调整优化
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '核心网/数据网/传输网/数调系统软件升级/网络调整优化', '核心网/数据网/传输网/数调系统软件升级/网络调整优化', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 4);

-- ============================================================================
-- 5. 故障处理（含多级分类：有线/无线 -> 室内/室外/高铁）
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '故障处理', '故障处理', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 5);
SET @cat_id = LAST_INSERT_ID();

-- 故障处理 / 有线
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '有线', '故障处理/有线', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 1);
SET @sub_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, '室内', '故障处理/有线/室内', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@sub_id, '室外', '故障处理/有线/室外', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2),
(@sub_id, '高铁', '故障处理/有线/高铁', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 3);

-- 故障处理 / 无线
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '无线', '故障处理/无线', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 2);
SET @sub_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, '室内', '故障处理/无线/室内', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@sub_id, '室外', '故障处理/无线/室外', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2),
(@sub_id, '高铁', '故障处理/无线/高铁', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 3);

-- 故障处理备注
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '备注', '故障处理/备注', 2, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 3);

-- ============================================================================
-- 6. 任务
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '任务', '任务', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 6);

-- ============================================================================
-- 7. 防洪
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '防洪', '防洪', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 7);

-- ============================================================================
-- 8. 防台
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '防台', '防台', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 8);

-- ============================================================================
-- 9. 培训（含完整的培训分类树）
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '培训', '培训', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 9);
SET @cat_id = LAST_INSERT_ID();

-- 培训 / 适应性培训
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '适应性培训', '培训/适应性培训', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 1);
SET @sub_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, '局内', '培训/适应性培训/局内', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@sub_id, '局外', '培训/适应性培训/局外', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2);

-- 培训 / 资格性培训
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '资格性培训', '培训/资格性培训', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 2);
SET @sub_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, '局内', '培训/资格性培训/局内', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@sub_id, '局外', '培训/资格性培训/局外', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2);

-- 培训 / 厂培 / 其他类培训
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '厂培', '培训/厂培', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 3),
(@cat_id, '其他类培训', '培训/其他类培训', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 4);

-- 培训 / 备注：培训班名称（文本项）
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '备注：培训班名称', '培训/备注：培训班名称', 2, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 5);

-- ============================================================================
-- 10-26: 剩余用工项目（作为可填写的叶子节点）
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '休假', '休假', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 10),
(0, '学习、考试', '学习、考试', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 11),
(0, '比赛', '比赛', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 12),
(0, '会议', '会议', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 13),
(0, '助勤', '助勤', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 14),
(0, '轮岗、交流', '轮岗、交流', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 15),
(0, '新线介入', '新线介入', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 16),
(0, '客运段支援', '客运段支援', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 17),
(0, '武汉高铁担任驻站培训师', '武汉高铁担任驻站培训师', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 18),
(0, '设备日常检维修', '设备日常检维修', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 19),
(0, '应急处置、演练', '应急处置、演练', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 20),
(0, '添乘、场强测试', '添乘、场强测试', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 21),
(0, '报表台账整理', '报表台账整理', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 22),
(0, '党工会团活动', '党工会团活动', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 23),
(0, '迎检工作', '迎检工作', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 24),
(0, '值班', '值班', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 25),
(0, '其他', '其他', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 26);
