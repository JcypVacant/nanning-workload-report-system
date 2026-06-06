package com.cyp.nanningworkloadreportsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 填报明细实体类
 * 对应数据库表 work_report_item
 * 记录具体填报了哪个用工项目、多少数值或什么文本内容
 */
@Data
@TableName("work_report_item")
public class WorkReportItem {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 填报主表ID（关联work_report.id） */
    private Long reportId;

    /** 用工项目ID（关联work_item.id，仅限叶子节点） */
    private Long workItemId;

    /** 工时数值（分钟，仅NUMBER类型项目填写） */
    private java.math.BigDecimal numberValue;

    /** 工分数值（工分，仅NUMBER类型项目填写） */
    private java.math.BigDecimal pointsValue;

    /** 文本内容（仅TEXT类型项目填写） */
    private String textValue;

    /** 单位：分钟/分/文本 */
    private String unit;

    /** 排序号 */
    private Integer sortOrder;

    /** 备注 */
    private String remark;

    // ==================== 非数据库字段（关联查询） ====================

    /** 用工项目完整路径（关联查询） */
    @TableField(exist = false)
    private String itemPath;

    /** 用工项目名称（关联查询） */
    @TableField(exist = false)
    private String itemName;
}
