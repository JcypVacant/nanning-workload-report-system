-- ============================================================================
-- 用工项目字典初始化数据（完整版）
-- 包含全部26个默认用工项目分类及其完整树形结构
-- 严格遵循《南宁通信段劳动用工工时工分统计管理系统》需求文档的用工项目层级
--
-- 类型说明：
--   CATEGORY - 分类项（仅作分类节点，不可填写）
--   NUMBER   - 数值项（可填写分钟或工分）
--   TEXT     - 文本项（填写文字内容，如备注、培训班名称等）
-- 报表类型：HOURS-工时, POINTS-工分, BOTH-通用
--
-- 重要设计：每一项工时配套单列工分字段（report_type=BOTH），
-- 同一项目在填报工时和填报工分时均可使用
-- ============================================================================

USE nanning_workload;

-- 启用变量跟踪当前插入的父ID
SET @cat_id = 0;
SET @sub_id = 0;
SET @sub2_id = 0;

-- ============================================================================
-- 1. 施工（顶级分类1）
--    包含 Ⅰ级施工 / Ⅱ级施工 / Ⅲ级施工 三个二级分类
--    每级施工包含：准备时间、往返路程时间、施工时间、备注
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '施工', '施工', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 1);
SET @cat_id = LAST_INSERT_ID();

-- 施工 / Ⅰ级施工
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, 'Ⅰ级施工', '施工/Ⅰ级施工', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 1);
SET @sub_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, '准备时间', '施工/Ⅰ级施工/准备时间', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@sub_id, '往返路程时间', '施工/Ⅰ级施工/往返路程时间', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2),
(@sub_id, '施工时间', '施工/Ⅰ级施工/施工时间', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 3),
(@sub_id, '备注', '施工/Ⅰ级施工/备注', 3, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 4);

-- 施工 / Ⅱ级施工
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, 'Ⅱ级施工', '施工/Ⅱ级施工', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 2);
SET @sub_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, '准备时间', '施工/Ⅱ级施工/准备时间', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@sub_id, '往返路程时间', '施工/Ⅱ级施工/往返路程时间', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2),
(@sub_id, '施工时间', '施工/Ⅱ级施工/施工时间', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 3),
(@sub_id, '备注', '施工/Ⅱ级施工/备注', 3, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 4);

-- 施工 / Ⅲ级施工
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, 'Ⅲ级施工', '施工/Ⅲ级施工', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 3);
SET @sub_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, '准备时间', '施工/Ⅲ级施工/准备时间', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@sub_id, '往返路程时间', '施工/Ⅲ级施工/往返路程时间', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2),
(@sub_id, '施工时间', '施工/Ⅲ级施工/施工时间', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 3),
(@sub_id, '备注', '施工/Ⅲ级施工/备注', 3, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 4);

-- ============================================================================
-- 2. 施工配合（顶级分类2）
--    包含 路内 / 路外 两个二级分类
--    每类包含：准备时间、往返路程时间、施工时间、备注
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '施工配合', '施工配合', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 2);
SET @cat_id = LAST_INSERT_ID();

-- 施工配合 / 路内
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '路内', '施工配合/路内', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 1);
SET @sub_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, '准备时间', '施工配合/路内/准备时间', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@sub_id, '往返路程时间', '施工配合/路内/往返路程时间', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2),
(@sub_id, '施工时间', '施工配合/路内/施工时间', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 3),
(@sub_id, '备注', '施工配合/路内/备注', 3, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 4);

-- 施工配合 / 路外
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '路外', '施工配合/路外', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 2);
SET @sub_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, '准备时间', '施工配合/路外/准备时间', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@sub_id, '往返路程时间', '施工配合/路外/往返路程时间', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2),
(@sub_id, '施工时间', '施工配合/路外/施工时间', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 3),
(@sub_id, '备注', '施工配合/路外/备注', 3, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 4);

-- ============================================================================
-- 3. 维修（顶级分类3）
--    包含 Ⅰ级维修 / Ⅱ级维修 两个二级分类
--    每级包含：准备时间、往返路程时间、维修时间、备注
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '维修', '维修', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 3);
SET @cat_id = LAST_INSERT_ID();

-- 维修 / Ⅰ级维修
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, 'Ⅰ级维修', '维修/Ⅰ级维修', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 1);
SET @sub_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, '准备时间', '维修/Ⅰ级维修/准备时间', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@sub_id, '往返路程时间', '维修/Ⅰ级维修/往返路程时间', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2),
(@sub_id, '维修时间', '维修/Ⅰ级维修/维修时间', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 3),
(@sub_id, '备注', '维修/Ⅰ级维修/备注', 3, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 4);

-- 维修 / Ⅱ级维修
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, 'Ⅱ级维修', '维修/Ⅱ级维修', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 2);
SET @sub_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, '准备时间', '维修/Ⅱ级维修/准备时间', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@sub_id, '往返路程时间', '维修/Ⅱ级维修/往返路程时间', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2),
(@sub_id, '维修时间', '维修/Ⅱ级维修/维修时间', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 3),
(@sub_id, '备注', '维修/Ⅱ级维修/备注', 3, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 4);

-- ============================================================================
-- 4. 核心网/数据网/传输网/数调系统软件升级/网络调整优化（顶级分类4）
--    包含：准备时间、往返路程时间、施工时间、备注
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '核心网/数据网/传输网/数调系统软件升级/网络调整优化', '核心网/数据网/传输网/数调系统软件升级/网络调整优化', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 4);
SET @cat_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '准备时间', '核心网/数据网/传输网/数调系统软件升级/网络调整优化/准备时间', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@cat_id, '往返路程时间', '核心网/数据网/传输网/数调系统软件升级/网络调整优化/往返路程时间', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2),
(@cat_id, '施工时间', '核心网/数据网/传输网/数调系统软件升级/网络调整优化/施工时间', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 3),
(@cat_id, '备注', '核心网/数据网/传输网/数调系统软件升级/网络调整优化/备注', 2, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 4);

-- ============================================================================
-- 5. 故障处理（顶级分类5）
--    有线 → 室内：高铁/普铁，室外：高铁/普铁
--    无线 → 室内：高铁/普铁，室外：高铁/普铁
--    备注：故障名称
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '故障处理', '故障处理', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 5);
SET @cat_id = LAST_INSERT_ID();

-- 故障处理 / 有线
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '有线', '故障处理/有线', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 1);
SET @sub_id = LAST_INSERT_ID();

-- 故障处理 / 有线 / 室内
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, '室内', '故障处理/有线/室内', 3, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 1);
SET @sub2_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub2_id, '高铁', '故障处理/有线/室内/高铁', 4, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@sub2_id, '普铁', '故障处理/有线/室内/普铁', 4, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2);

-- 故障处理 / 有线 / 室外
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, '室外', '故障处理/有线/室外', 3, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 2);
SET @sub2_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub2_id, '高铁', '故障处理/有线/室外/高铁', 4, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@sub2_id, '普铁', '故障处理/有线/室外/普铁', 4, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2);

-- 故障处理 / 无线
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '无线', '故障处理/无线', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 2);
SET @sub_id = LAST_INSERT_ID();

-- 故障处理 / 无线 / 室内
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, '室内', '故障处理/无线/室内', 3, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 1);
SET @sub2_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub2_id, '高铁', '故障处理/无线/室内/高铁', 4, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@sub2_id, '普铁', '故障处理/无线/室内/普铁', 4, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2);

-- 故障处理 / 无线 / 室外
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, '室外', '故障处理/无线/室外', 3, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 2);
SET @sub2_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub2_id, '高铁', '故障处理/无线/室外/高铁', 4, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@sub2_id, '普铁', '故障处理/无线/室外/普铁', 4, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2);

-- 故障处理 / 备注（故障名称）
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '备注：故障名称', '故障处理/备注：故障名称', 2, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 3);

-- ============================================================================
-- 6. 任务（顶级分类6）
--    包含 客运 / 货运，各分 Ⅰ级/Ⅱ级/Ⅲ级
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '任务', '任务', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 6);
SET @cat_id = LAST_INSERT_ID();

-- 任务 / 客运
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '客运', '任务/客运', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 1);
SET @sub_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, 'Ⅰ级', '任务/客运/Ⅰ级', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@sub_id, 'Ⅱ级', '任务/客运/Ⅱ级', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2),
(@sub_id, 'Ⅲ级', '任务/客运/Ⅲ级', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 3);

-- 任务 / 货运
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '货运', '任务/货运', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 2);
SET @sub_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, 'Ⅰ级', '任务/货运/Ⅰ级', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@sub_id, 'Ⅱ级', '任务/货运/Ⅱ级', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2),
(@sub_id, 'Ⅲ级', '任务/货运/Ⅲ级', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 3);

-- ============================================================================
-- 7. 防洪（顶级分类7）
--    包含 高铁 / 普铁，各分 Ⅰ级/Ⅱ级/Ⅲ级
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '防洪', '防洪', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 7);
SET @cat_id = LAST_INSERT_ID();

-- 防洪 / 高铁
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '高铁', '防洪/高铁', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 1);
SET @sub_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, 'Ⅰ级', '防洪/高铁/Ⅰ级', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@sub_id, 'Ⅱ级', '防洪/高铁/Ⅱ级', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2),
(@sub_id, 'Ⅲ级', '防洪/高铁/Ⅲ级', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 3);

-- 防洪 / 普铁
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '普铁', '防洪/普铁', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 2);
SET @sub_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, 'Ⅰ级', '防洪/普铁/Ⅰ级', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@sub_id, 'Ⅱ级', '防洪/普铁/Ⅱ级', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2),
(@sub_id, 'Ⅲ级', '防洪/普铁/Ⅲ级', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 3);

-- ============================================================================
-- 8. 防台（顶级分类8）
--    包含 高铁 / 普铁，各分 Ⅰ级/Ⅱ级/Ⅲ级
--    备注：调度令号、台风名称等
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '防台', '防台', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 8);
SET @cat_id = LAST_INSERT_ID();

-- 防台 / 高铁
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '高铁', '防台/高铁', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 1);
SET @sub_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, 'Ⅰ级', '防台/高铁/Ⅰ级', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@sub_id, 'Ⅱ级', '防台/高铁/Ⅱ级', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2),
(@sub_id, 'Ⅲ级', '防台/高铁/Ⅲ级', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 3);

-- 防台 / 普铁
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '普铁', '防台/普铁', 2, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 2);
SET @sub_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@sub_id, 'Ⅰ级', '防台/普铁/Ⅰ级', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@sub_id, 'Ⅱ级', '防台/普铁/Ⅱ级', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2),
(@sub_id, 'Ⅲ级', '防台/普铁/Ⅲ级', 3, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 3);

-- 防台 / 备注（调度令号、台风名称等）
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '备注：调度令号、台风名称等', '防台/备注：调度令号、台风名称等', 2, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 3);

-- ============================================================================
-- 9. 培训（顶级分类9）
--    适应性培训（局内/局外）、资格性培训（局内/局外）、厂培、其他类培训
--    备注：培训班名称
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

-- 培训 / 备注（培训班名称）
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '备注：培训班名称', '培训/备注：培训班名称', 2, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 5);

-- ============================================================================
-- 10. 休假（顶级分类10）
--     工休、补休、产假、哺乳假、育儿假、病休、疗养、其他假
--     备注：假别名称
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '休假', '休假', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 10);
SET @cat_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '工休', '休假/工休', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@cat_id, '补休', '休假/补休', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2),
(@cat_id, '产假', '休假/产假', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 3),
(@cat_id, '哺乳假', '休假/哺乳假', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 4),
(@cat_id, '育儿假', '休假/育儿假', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 5),
(@cat_id, '病休', '休假/病休', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 6),
(@cat_id, '疗养', '休假/疗养', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 7),
(@cat_id, '其他假', '休假/其他假', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 8);

-- 休假 / 备注（假别名称）
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '备注：假别名称', '休假/备注：假别名称', 2, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 9);

-- ============================================================================
-- 11. 学习、考试（顶级分类11）
--     学习、考试(分钟) + 备注
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '学习、考试', '学习、考试', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 11);
SET @cat_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '学习、考试', '学习、考试/学习、考试', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@cat_id, '备注：学习、考试名称', '学习、考试/备注：学习、考试名称', 2, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 2);

-- ============================================================================
-- 12. 比赛（顶级分类12）
--     比赛(分钟) + 备注
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '比赛', '比赛', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 12);
SET @cat_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '比赛', '比赛/比赛', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@cat_id, '备注：比赛名称', '比赛/备注：比赛名称', 2, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 2);

-- ============================================================================
-- 13. 会议（顶级分类13）
--     会议(分钟) + 备注
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '会议', '会议', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 13);
SET @cat_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '会议', '会议/会议', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@cat_id, '备注：会议名称', '会议/备注：会议名称', 2, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 2);

-- ============================================================================
-- 14. 助勤（顶级分类14）
--     助勤(分钟) + 备注
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '助勤', '助勤', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 14);
SET @cat_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '助勤', '助勤/助勤', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@cat_id, '备注：助勤部门', '助勤/备注：助勤部门', 2, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 2);

-- ============================================================================
-- 15. 轮岗、交流（顶级分类15）
--     轮岗、交流(分钟) + 备注
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '轮岗、交流', '轮岗、交流', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 15);
SET @cat_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '轮岗、交流', '轮岗、交流/轮岗、交流', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@cat_id, '备注：轮岗、交流部门', '轮岗、交流/备注：轮岗、交流部门', 2, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 2);

-- ============================================================================
-- 16. 新线介入（顶级分类16）
--     新线介入(分钟) + 备注
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '新线介入', '新线介入', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 16);
SET @cat_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '新线介入', '新线介入/新线介入', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@cat_id, '备注：介入线别', '新线介入/备注：介入线别', 2, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 2);

-- ============================================================================
-- 17. 客运段支援（顶级分类17）- 单一叶子节点
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '客运段支援', '客运段支援', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 17);

-- ============================================================================
-- 18. 武汉高铁担任驻站培训师（顶级分类18）- 单一叶子节点
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '武汉高铁担任驻站培训师', '武汉高铁担任驻站培训师', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 18);

-- ============================================================================
-- 19. 设备日常检维修（顶级分类19）- 单一叶子节点
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '设备日常检维修', '设备日常检维修', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 19);

-- ============================================================================
-- 20. 应急处置(演练)（顶级分类20）- 单一叶子节点
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '应急处置(演练)', '应急处置(演练)', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 20);

-- ============================================================================
-- 21. 添乘、场强测试（顶级分类21）
--     添乘、场强测试(分钟) + 备注
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '添乘、场强测试', '添乘、场强测试', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 21);
SET @cat_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '添乘、场强测试', '添乘、场强测试/添乘、场强测试', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@cat_id, '备注：添乘、场强测试区间', '添乘、场强测试/备注：添乘、场强测试区间', 2, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 2);

-- ============================================================================
-- 22. 报表台账整理（顶级分类22）- 单一叶子节点
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '报表台账整理', '报表台账整理', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 22);

-- ============================================================================
-- 23. 党工会团活动（顶级分类23）
--     党工会团活动(分钟) + 备注
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '党工会团活动', '党工会团活动', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 23);
SET @cat_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '党工会团活动', '党工会团活动/党工会团活动', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@cat_id, '备注：活动内容', '党工会团活动/备注：活动内容', 2, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 2);

-- ============================================================================
-- 24. 迎检工作（顶级分类24）
--     迎检工作(分钟) + 备注
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '迎检工作', '迎检工作', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 24);
SET @cat_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '迎检工作', '迎检工作/迎检工作', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@cat_id, '备注：迎检内容', '迎检工作/备注：迎检内容', 2, 'TEXT', NULL, '文本', 0, 0, 1, NULL, 2);

-- ============================================================================
-- 25. 值班（顶级分类25）- 单一叶子节点
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '值班', '值班', 1, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 25);

-- ============================================================================
-- 26. 其他（顶级分类26）
--     吃饭、间休、其余自定义项目
-- ============================================================================
INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(0, '其他', '其他', 1, 'CATEGORY', NULL, NULL, 1, 0, 0, NULL, 26);
SET @cat_id = LAST_INSERT_ID();

INSERT INTO work_item (parent_id, item_name, item_path, item_level, input_type, report_type, unit, is_category, is_input_item, need_remark, excel_column, sort_order) VALUES
(@cat_id, '吃饭', '其他/吃饭', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 1),
(@cat_id, '间休', '其他/间休', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 2),
(@cat_id, '其余自定义项目', '其他/其余自定义项目', 2, 'NUMBER', 'BOTH', '分钟', 0, 1, 0, NULL, 3);
