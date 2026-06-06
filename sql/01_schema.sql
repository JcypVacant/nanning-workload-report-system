-- ============================================================================
-- 南宁通信段劳动用工工时工分统计管理系统 - 数据库建表脚本
-- 数据库名称: nanning_workload
-- 创建日期: 2026-06-06
-- 说明: 包含组织架构、用户账号、人员信息、用工项目字典、
--        月度填报、工区填报、审核记录、操作日志等全部业务表
-- ============================================================================

-- 创建数据库（如尚未创建）
-- CREATE DATABASE IF NOT EXISTS nanning_workload DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE nanning_workload;

-- ============================================================================
-- 1. 组织架构表 org_unit
-- 用于保存段、车间、工区、车间本级等组织信息，采用树形结构
-- ============================================================================
DROP TABLE IF EXISTS `org_unit`;
CREATE TABLE `org_unit` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '上级组织ID，0表示根节点',
    `org_name`    VARCHAR(100) NOT NULL COMMENT '组织名称（如：贺州通信车间、梧州通信工区）',
    `org_type`    VARCHAR(20)  NOT NULL COMMENT '组织类型：SECTION-段, WORKSHOP-车间, WORK_AREA-工区, WORKSHOP_LEVEL-车间本级',
    `area_code`   VARCHAR(10)  DEFAULT NULL COMMENT 'Excel工作表映射代码（A/B/C/D/E/F/G/H）',
    `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    `enabled`     TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用, 0-停用',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_org_type` (`org_type`),
    INDEX `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织架构表：段-车间-工区-车间本级树形结构';

-- ============================================================================
-- 2. 用户账号表 sys_user
-- 用于保存系统登录账号信息
-- ============================================================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`        VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `password`        VARCHAR(200) NOT NULL COMMENT '登录密码（BCrypt加密）',
    `real_name`       VARCHAR(50)  NOT NULL COMMENT '真实姓名',
    `phone`           VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    `enabled`         TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用, 0-禁用',
    `first_login`     TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否首次登录：1-是, 0-否（用于强制修改密码）',
    `last_login_time` DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户账号表';

-- ============================================================================
-- 3. 用户组织权限表 sys_user_org_scope
-- 用于绑定用户可管理的数据范围（角色 + 组织范围）
-- 支持一个车间绑定多个车间管理员
-- ============================================================================
DROP TABLE IF EXISTS `sys_user_org_scope`;
CREATE TABLE `sys_user_org_scope` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT      NOT NULL COMMENT '用户ID（关联sys_user.id）',
    `org_id`      BIGINT      NOT NULL COMMENT '组织ID（关联org_unit.id，段级管理员为0）',
    `role_code`   VARCHAR(30) NOT NULL COMMENT '角色编码：SECTION_ADMIN-段级管理员, WORKSHOP_ADMIN-车间管理员, AREA_REPORTER-工区填报员',
    `scope_type`  VARCHAR(20) NOT NULL COMMENT '数据范围类型：ALL-全段, WORKSHOP-车间范围, AREA-工区范围',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_org_id` (`org_id`),
    UNIQUE INDEX `uk_user_org_role` (`user_id`, `org_id`, `role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户组织权限绑定表';

-- ============================================================================
-- 4. 人员信息表 employee
-- 用于保存参与工时工分填报的人员基础信息
-- ============================================================================
DROP TABLE IF EXISTS `employee`;
CREATE TABLE `employee` (
    `id`                      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`                    VARCHAR(50)  NOT NULL COMMENT '姓名',
    `gender`                  VARCHAR(5)   DEFAULT NULL COMMENT '性别：男/女',
    `unit_name`               VARCHAR(100) DEFAULT '南宁通信段' COMMENT '所属单位名称',
    `department_name`         VARCHAR(100) DEFAULT NULL COMMENT '部门（车间）名称',
    `workshop_id`             BIGINT       NOT NULL COMMENT '所属车间ID（关联org_unit.id）',
    `team_name`               VARCHAR(100) DEFAULT NULL COMMENT '班组名称',
    `area_id`                 BIGINT       NOT NULL COMMENT '所属工区ID（关联org_unit.id）',
    `birth_date`              DATE         DEFAULT NULL COMMENT '出生日期',
    `position_name`           VARCHAR(100) DEFAULT NULL COMMENT '职位名称',
    `professional_post_type`  VARCHAR(100) DEFAULT NULL COMMENT '聘任专业职务工种',
    `work_type`               VARCHAR(100) DEFAULT NULL COMMENT '工种',
    `rank_category`           VARCHAR(100) DEFAULT NULL COMMENT '职级分类',
    `employee_status`         VARCHAR(20)  DEFAULT '在岗' COMMENT '人员状态：在岗/调出/停用',
    `enabled`                 TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用, 0-停用',
    `remark`                  VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time`             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_workshop_id` (`workshop_id`),
    INDEX `idx_area_id` (`area_id`),
    INDEX `idx_name` (`name`),
    INDEX `idx_employee_status` (`employee_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人员信息表';

-- ============================================================================
-- 5. 人员调动记录表 employee_transfer_record
-- 用于记录人员所属车间或工区的变更历史
-- ============================================================================
DROP TABLE IF EXISTS `employee_transfer_record`;
CREATE TABLE `employee_transfer_record` (
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `employee_id`        BIGINT       NOT NULL COMMENT '人员ID（关联employee.id）',
    `before_workshop_id` BIGINT       DEFAULT NULL COMMENT '调动前车间ID',
    `before_area_id`     BIGINT       DEFAULT NULL COMMENT '调动前工区ID',
    `before_team_name`   VARCHAR(100) DEFAULT NULL COMMENT '调动前班组名称',
    `after_workshop_id`  BIGINT       DEFAULT NULL COMMENT '调动后车间ID',
    `after_area_id`      BIGINT       DEFAULT NULL COMMENT '调动后工区ID',
    `after_team_name`    VARCHAR(100) DEFAULT NULL COMMENT '调动后班组名称',
    `transfer_reason`    VARCHAR(500) DEFAULT NULL COMMENT '调动原因',
    `operator_id`        BIGINT       NOT NULL COMMENT '操作人ID（关联sys_user.id）',
    `operate_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `remark`             VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    INDEX `idx_employee_id` (`employee_id`),
    INDEX `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人员调动记录表';

-- ============================================================================
-- 6. 用工项目字典表 work_item
-- 用于保存所有可填报的用工项目（树形多级字典）
-- 包含分类项(CATEGORY)、数值项(NUMBER)、文本项(TEXT)三种类型
-- ============================================================================
DROP TABLE IF EXISTS `work_item`;
CREATE TABLE `work_item` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id`     BIGINT       NOT NULL DEFAULT 0 COMMENT '上级项目ID，0表示根项目',
    `item_name`     VARCHAR(100) NOT NULL COMMENT '项目名称',
    `item_path`     VARCHAR(500) DEFAULT NULL COMMENT '完整路径（如：培训/适应性培训/局内）',
    `item_level`    INT          NOT NULL DEFAULT 1 COMMENT '层级（1=一级分类, 2=二级, 3=三级...）',
    `input_type`    VARCHAR(20)  NOT NULL COMMENT '输入类型：CATEGORY-分类项, NUMBER-数值项, TEXT-文本项',
    `report_type`   VARCHAR(20)  DEFAULT NULL COMMENT '适用报表类型：HOURS-工时, POINTS-工分, BOTH-通用',
    `unit`          VARCHAR(20)  DEFAULT NULL COMMENT '单位：分钟/分/文本',
    `is_category`   TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否仅为分类节点（不可填写）：1-是, 0-否',
    `is_input_item` TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否为可填写的最终项：1-是, 0-否',
    `need_remark`   TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否需要填写备注/文本：1-需要, 0-不需要',
    `excel_column`  VARCHAR(10)  DEFAULT NULL COMMENT '对应Excel列标识',
    `sort_order`    INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    `enabled`       TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用, 0-停用',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_item_path` (`item_path`),
    INDEX `idx_input_type` (`input_type`),
    INDEX `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用工项目字典表（树形多级字典）';

-- ============================================================================
-- 7. 月度填报期间表 monthly_period
-- 用于管理填报月份的状态和周期
-- ============================================================================
DROP TABLE IF EXISTS `monthly_period`;
CREATE TABLE `monthly_period` (
    `id`             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `period_name`    VARCHAR(50) NOT NULL COMMENT '期间名称（如：2026年6月）',
    `year`           INT         NOT NULL COMMENT '年份',
    `month`          INT         NOT NULL COMMENT '月份',
    `status`         VARCHAR(20) NOT NULL DEFAULT '未开始' COMMENT '月份状态：未开始/填报中/车间审核中/段级汇总中/已锁定/已归档',
    `start_date`     DATE        DEFAULT NULL COMMENT '填报开始日期',
    `end_date`       DATE        DEFAULT NULL COMMENT '填报截止日期',
    `audit_deadline` DATE        DEFAULT NULL COMMENT '审核截止日期',
    `locked`         TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否锁定：1-已锁定, 0-未锁定',
    `create_time`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_year_month` (`year`, `month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='月度填报期间表';

-- ============================================================================
-- 8. 填报主表 work_report
-- 记录某个人某天某种类别的一次填报记录
-- 唯一约束：同一期间、同一人员、同一日期、同一类别只能有一条记录
-- ============================================================================
DROP TABLE IF EXISTS `work_report`;
CREATE TABLE `work_report` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `period_id`    BIGINT       NOT NULL COMMENT '月度期间ID（关联monthly_period.id）',
    `workshop_id`  BIGINT       NOT NULL COMMENT '所属车间ID（填报时的车间）',
    `area_id`      BIGINT       NOT NULL COMMENT '所属工区ID（填报时的工区）',
    `employee_id`  BIGINT       NOT NULL COMMENT '人员ID（关联employee.id）',
    `work_date`    DATE         NOT NULL COMMENT '工作日期',
    `report_type`  VARCHAR(20)  NOT NULL COMMENT '填报类别：HOURS-工时, POINTS-工分',
    `status`       VARCHAR(20)  NOT NULL DEFAULT '草稿' COMMENT '数据状态：草稿/已提交/已退回/已审核/已锁定',
    `remark`       VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_by`   BIGINT       NOT NULL COMMENT '创建人ID（关联sys_user.id）',
    `created_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by`   BIGINT       DEFAULT NULL COMMENT '最后修改人ID',
    `updated_time` DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_period_emp_date_type` (`period_id`, `employee_id`, `work_date`, `report_type`),
    INDEX `idx_period_id` (`period_id`),
    INDEX `idx_employee_id` (`employee_id`),
    INDEX `idx_area_id` (`area_id`),
    INDEX `idx_workshop_id` (`workshop_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_work_date` (`work_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工时工分填报主表';

-- ============================================================================
-- 9. 填报明细表 work_report_item
-- 记录具体填报了哪个用工项目、多少数值或什么文本内容
-- ============================================================================
DROP TABLE IF EXISTS `work_report_item`;
CREATE TABLE `work_report_item` (
    `id`           BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `report_id`    BIGINT         NOT NULL COMMENT '填报主表ID（关联work_report.id）',
    `work_item_id` BIGINT         NOT NULL COMMENT '用工项目ID（关联work_item.id，仅限叶子节点）',
    `number_value` DECIMAL(10,2)  DEFAULT NULL COMMENT '工时数值（分钟，NUMBER类型项目填写）',
    `points_value` DECIMAL(10,2)  DEFAULT NULL COMMENT '工分数值（工分，NUMBER类型项目填写）',
    `text_value`   TEXT           DEFAULT NULL COMMENT '文本内容（TEXT类型项目填写）',
    `unit`         VARCHAR(20)    DEFAULT NULL COMMENT '单位：分钟/分/文本',
    `sort_order`   INT            NOT NULL DEFAULT 0 COMMENT '排序号',
    `remark`       VARCHAR(500)   DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    INDEX `idx_report_id` (`report_id`),
    INDEX `idx_work_item_id` (`work_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='填报明细表';

-- ============================================================================
-- 10. 审核记录表 audit_record
-- 用于记录工区提交、车间审核、退回修改等流程操作
-- ============================================================================
DROP TABLE IF EXISTS `audit_record`;
CREATE TABLE `audit_record` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `period_id`    BIGINT       NOT NULL COMMENT '月度期间ID（关联monthly_period.id）',
    `report_id`    BIGINT       DEFAULT NULL COMMENT '填报记录ID（关联work_report.id，可为空表示批量操作）',
    `org_id`       BIGINT       NOT NULL COMMENT '目标组织ID（关联org_unit.id）',
    `audit_level`  VARCHAR(20)  NOT NULL COMMENT '审核层级：WORKSHOP-车间级, SECTION-段级',
    `action`       VARCHAR(20)  NOT NULL COMMENT '操作类型：SUBMIT-提交, APPROVE-通过, RETURN-退回, LOCK-锁定, UNLOCK-解锁',
    `comment`      VARCHAR(500) DEFAULT NULL COMMENT '审核意见（退回原因等）',
    `operator_id`  BIGINT       NOT NULL COMMENT '操作人ID（关联sys_user.id）',
    `operate_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    INDEX `idx_period_id` (`period_id`),
    INDEX `idx_report_id` (`report_id`),
    INDEX `idx_org_id` (`org_id`),
    INDEX `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核记录表';

-- ============================================================================
-- 11. 操作日志表 operation_log
-- 用于记录系统中的关键操作，方便后期追溯
-- ============================================================================
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`        BIGINT        NOT NULL COMMENT '操作用户ID（关联sys_user.id）',
    `username`       VARCHAR(50)   DEFAULT NULL COMMENT '操作用户名（冗余字段，便于查询）',
    `module_name`    VARCHAR(50)   NOT NULL COMMENT '操作模块（如：工区填报/车间审核/账号管理）',
    `operation_type` VARCHAR(30)   NOT NULL COMMENT '操作类型：LOGIN/CREATE/UPDATE/DELETE/SUBMIT/APPROVE/RETURN/EXPORT/LOCK/UNLOCK/TRANSFER',
    `target_id`      VARCHAR(100)  DEFAULT NULL COMMENT '操作目标ID',
    `summary`        VARCHAR(500)  DEFAULT NULL COMMENT '操作摘要（人可读的描述）',
    `before_json`    TEXT          DEFAULT NULL COMMENT '操作前数据（JSON格式）',
    `after_json`     TEXT          DEFAULT NULL COMMENT '操作后数据（JSON格式）',
    `ip`             VARCHAR(50)   DEFAULT NULL COMMENT '操作IP地址',
    `operate_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_module_name` (`module_name`),
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作日志表';
