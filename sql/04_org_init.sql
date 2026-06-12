-- 初始化组织架构
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE org_unit;
SET FOREIGN_KEY_CHECKS = 1;

-- 段级
INSERT INTO org_unit (id, parent_id, org_name, org_type, sort_order, enabled) VALUES (1, 0, '南宁通信段', 'SECTION', 1, 1);

-- 1. 南宁高铁通信车间
INSERT INTO org_unit (id, parent_id, org_name, org_type, sort_order, enabled) VALUES
(101, 1, '南宁高铁通信车间', 'WORKSHOP', 1, 1),
(102, 101, '南宁高铁通信车间本级', 'WORKSHOP_LEVEL', 1, 1),
(103, 101, '崇左南高铁通信工区', 'WORK_AREA', 2, 1),
(104, 101, '百色高铁通信工区', 'WORK_AREA', 3, 1),
(105, 101, '南宁东高铁通信工区', 'WORK_AREA', 4, 1),
(106, 101, '南宁南高铁通信工区', 'WORK_AREA', 5, 1),
(107, 101, '平果高铁通信工区', 'WORK_AREA', 6, 1),
(108, 101, '龙州高铁通信工区', 'WORK_AREA', 7, 1);

-- 2. 桂林西高铁通信车间
INSERT INTO org_unit (id, parent_id, org_name, org_type, sort_order, enabled) VALUES
(201, 1, '桂林西高铁通信车间', 'WORKSHOP', 2, 1),
(202, 201, '桂林西高铁通信车间本级', 'WORKSHOP_LEVEL', 1, 1),
(203, 201, '桂林西高铁通信工区', 'WORK_AREA', 2, 1),
(204, 201, '贺州高铁通信工区', 'WORK_AREA', 3, 1);

-- 3. 桂林北高铁通信车间
INSERT INTO org_unit (id, parent_id, org_name, org_type, sort_order, enabled) VALUES
(301, 1, '桂林北高铁通信车间', 'WORKSHOP', 3, 1),
(302, 301, '桂林北高铁通信车间本级', 'WORKSHOP_LEVEL', 1, 1),
(303, 301, '全州南高铁通信工区', 'WORK_AREA', 2, 1),
(304, 301, '桂林北高铁通信工区', 'WORK_AREA', 3, 1),
(305, 301, '东安东高铁通信工区', 'WORK_AREA', 4, 1),
(306, 301, '柳州高铁通信工区', 'WORK_AREA', 5, 1),
(307, 301, '桂林北高铁通信青年骨干培训工区', 'WORK_AREA', 6, 1);

-- 4. 贵港高铁通信车间
INSERT INTO org_unit (id, parent_id, org_name, org_type, sort_order, enabled) VALUES
(401, 1, '贵港高铁通信车间', 'WORKSHOP', 4, 1),
(402, 401, '贵港高铁通信车间本级', 'WORKSHOP_LEVEL', 1, 1),
(403, 401, '来宾北高铁通信工区', 'WORK_AREA', 2, 1),
(404, 401, '宾阳高铁通信工区', 'WORK_AREA', 3, 1),
(405, 401, '梧州南高铁通信工区', 'WORK_AREA', 4, 1),
(406, 401, '贵港高铁通信工区', 'WORK_AREA', 5, 1);

-- 5. 河池高铁通信车间
INSERT INTO org_unit (id, parent_id, org_name, org_type, sort_order, enabled) VALUES
(501, 1, '河池高铁通信车间', 'WORKSHOP', 5, 1),
(502, 501, '河池高铁通信车间本级', 'WORKSHOP_LEVEL', 1, 1),
(503, 501, '南宁北高铁通信工区', 'WORK_AREA', 2, 1),
(504, 501, '河池南高铁通信工区', 'WORK_AREA', 3, 1),
(505, 501, '河池北高铁通信工区', 'WORK_AREA', 4, 1);

-- 6. 玉林北高铁通信车间
INSERT INTO org_unit (id, parent_id, org_name, org_type, sort_order, enabled) VALUES
(601, 1, '玉林北高铁通信车间', 'WORKSHOP', 6, 1),
(602, 601, '玉林北高铁通信车间本级', 'WORKSHOP_LEVEL', 1, 1),
(603, 601, '横州高铁通信工区', 'WORK_AREA', 2, 1),
(604, 601, '玉林北高铁通信工区', 'WORK_AREA', 3, 1);

-- 7. 南宁通信车间
INSERT INTO org_unit (id, parent_id, org_name, org_type, sort_order, enabled) VALUES
(701, 1, '南宁通信车间', 'WORKSHOP', 7, 1),
(702, 701, '南宁通信车间本级', 'WORKSHOP_LEVEL', 1, 1),
(703, 701, '黎塘通信工区', 'WORK_AREA', 2, 1),
(704, 701, '来宾通信工区', 'WORK_AREA', 3, 1),
(705, 701, '南宁通信工区', 'WORK_AREA', 4, 1),
(706, 701, '南宁南通信工区', 'WORK_AREA', 5, 1),
(707, 701, '凭祥通信工区', 'WORK_AREA', 6, 1),
(708, 701, '崇左通信工区', 'WORK_AREA', 7, 1);

-- 8. 柳州通信车间
INSERT INTO org_unit (id, parent_id, org_name, org_type, sort_order, enabled) VALUES
(801, 1, '柳州通信车间', 'WORKSHOP', 8, 1),
(802, 801, '柳州通信车间本级', 'WORKSHOP_LEVEL', 1, 1),
(803, 801, '柳州通信工区', 'WORK_AREA', 2, 1),
(804, 801, '柳南通信工区', 'WORK_AREA', 3, 1),
(805, 801, '宜州通信工区', 'WORK_AREA', 4, 1),
(806, 801, '金城江通信工区', 'WORK_AREA', 5, 1),
(807, 801, '融安通信工区', 'WORK_AREA', 6, 1),
(808, 801, '融水通信工区', 'WORK_AREA', 7, 1);

-- 9. 玉林通信车间
INSERT INTO org_unit (id, parent_id, org_name, org_type, sort_order, enabled) VALUES
(901, 1, '玉林通信车间', 'WORKSHOP', 9, 1),
(902, 901, '玉林通信车间本级', 'WORKSHOP_LEVEL', 1, 1),
(903, 901, '玉林通信工区', 'WORK_AREA', 2, 1),
(904, 901, '陆川通信工区', 'WORK_AREA', 3, 1),
(905, 901, '茂名通信工区', 'WORK_AREA', 4, 1),
(906, 901, '湛江通信工区', 'WORK_AREA', 5, 1),
(907, 901, '贵港通信工区', 'WORK_AREA', 6, 1),
(908, 901, '河唇通信工区', 'WORK_AREA', 7, 1);

-- 10. 百色通信车间
INSERT INTO org_unit (id, parent_id, org_name, org_type, sort_order, enabled) VALUES
(1001, 1, '百色通信车间', 'WORKSHOP', 10, 1),
(1002, 1001, '百色通信车间本级', 'WORKSHOP_LEVEL', 1, 1),
(1003, 1001, '百色通信工区', 'WORK_AREA', 2, 1),
(1004, 1001, '田林通信工区', 'WORK_AREA', 3, 1),
(1005, 1001, '兴义通信工区', 'WORK_AREA', 4, 1),
(1006, 1001, '田东通信工区', 'WORK_AREA', 5, 1),
(1007, 1001, '平果通信工区', 'WORK_AREA', 6, 1),
(1008, 1001, '八渡通信青年骨干培训工区', 'WORK_AREA', 7, 1);

-- 11. 贺州通信车间
INSERT INTO org_unit (id, parent_id, org_name, org_type, sort_order, enabled) VALUES
(1101, 1, '贺州通信车间', 'WORKSHOP', 11, 1),
(1102, 1101, '贺州通信车间本级', 'WORKSHOP_LEVEL', 1, 1),
(1103, 1101, '梧州通信工区', 'WORK_AREA', 2, 1),
(1104, 1101, '道州通信工区', 'WORK_AREA', 3, 1),
(1105, 1101, '贺州通信工区', 'WORK_AREA', 4, 1),
(1106, 1101, '岑溪通信工区', 'WORK_AREA', 5, 1);

-- 12. 桂林通信车间
INSERT INTO org_unit (id, parent_id, org_name, org_type, sort_order, enabled) VALUES
(1201, 1, '桂林通信车间', 'WORKSHOP', 12, 1),
(1202, 1201, '桂林通信车间本级', 'WORKSHOP_LEVEL', 1, 1),
(1203, 1201, '全州通信工区', 'WORK_AREA', 2, 1),
(1204, 1201, '桂林通信工区', 'WORK_AREA', 3, 1),
(1205, 1201, '鹿寨通信工区', 'WORK_AREA', 4, 1);

-- 13. 南宁传输车间
INSERT INTO org_unit (id, parent_id, org_name, org_type, sort_order, enabled) VALUES
(1301, 1, '南宁传输车间', 'WORKSHOP', 13, 1),
(1302, 1301, '南宁传输车间本级', 'WORKSHOP_LEVEL', 1, 1),
(1303, 1301, '柳州传输工区', 'WORK_AREA', 2, 1),
(1304, 1301, '南宁传输工区', 'WORK_AREA', 3, 1),
(1305, 1301, '南宁传输工区电话组', 'WORK_AREA', 4, 1),
(1306, 1301, '桂林传输工区', 'WORK_AREA', 5, 1);

-- 14. 南宁GSM-R车间
INSERT INTO org_unit (id, parent_id, org_name, org_type, sort_order, enabled) VALUES
(1401, 1, '南宁GSM-R车间', 'WORKSHOP', 14, 1),
(1402, 1401, '南宁GSM-R车间本级', 'WORKSHOP_LEVEL', 1, 1),
(1403, 1401, '南宁调度工区', 'WORK_AREA', 2, 1),
(1404, 1401, '南宁会议工区', 'WORK_AREA', 3, 1),
(1405, 1401, '南宁局集团公司电报', 'WORK_AREA', 4, 1),
(1406, 1401, '综合数据网工区', 'WORK_AREA', 5, 1);

-- 15. 南宁专修车间
INSERT INTO org_unit (id, parent_id, org_name, org_type, sort_order, enabled) VALUES
(1501, 1, '南宁专修车间', 'WORKSHOP', 15, 1),
(1502, 1501, '南宁专修车间本级', 'WORKSHOP_LEVEL', 1, 1),
(1503, 1501, '南宁通信专修工区', 'WORK_AREA', 2, 1),
(1504, 1501, '柳州通信专修工区', 'WORK_AREA', 3, 1);
