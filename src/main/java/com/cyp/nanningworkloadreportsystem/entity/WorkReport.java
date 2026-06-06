package com.cyp.nanningworkloadreportsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 工时工分填报主表实体类
 * 对应数据库表 work_report
 *
 * 注意：此表使用 created_time/updated_time 列名（不同于其他表的 create_time/update_time）
 * 因此不继承 BaseEntity，独立定义字段
 *
 * 业务规则：同一期间、同一人员、同一日期、同一填报类别（工时/工分）只能有一条记录
 * 一条主记录可包含多条明细（work_report_item）
 */
@Data
@TableName("work_report")
public class WorkReport {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 月度期间ID（关联monthly_period.id） */
    private Long periodId;

    /** 所属车间ID（填报时的车间，用于历史数据归属） */
    private Long workshopId;

    /** 所属工区ID（填报时的工区，用于历史数据归属） */
    private Long areaId;

    /** 人员ID（关联employee.id） */
    private Long employeeId;

    /** 工作日期 */
    private LocalDate workDate;

    /** 填报类别：HOURS-工时, POINTS-工分 */
    private String reportType;

    /** 数据状态：草稿/已提交/已退回/已审核/已锁定 */
    private String status;

    /** 备注 */
    private String remark;

    /** 创建人ID（关联sys_user.id） */
    private Long createdBy;

    /** 创建时间（注意：列名为 created_time） */
    private LocalDateTime createdTime;

    /** 最后修改人ID */
    private Long updatedBy;

    /** 最后修改时间（注意：列名为 updated_time） */
    private LocalDateTime updatedTime;

    // ==================== 非数据库字段（关联查询） ====================

    /** 填报明细列表（关联work_report_item表） */
    @TableField(exist = false)
    private List<WorkReportItem> items = new ArrayList<>();

    /** 人员姓名（关联查询） */
    @TableField(exist = false)
    private String employeeName;

    /** 工区名称（关联查询） */
    @TableField(exist = false)
    private String areaName;

    /** 车间名称（关联查询） */
    @TableField(exist = false)
    private String workshopName;

    /** 期间名称（关联查询） */
    @TableField(exist = false)
    private String periodName;
}
