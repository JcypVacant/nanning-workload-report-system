package com.cyp.nanningworkloadreportsystem.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 车间汇总DTO — 按工区分组汇总工时工分数据
 */
@Data
public class WorkshopSummaryDTO {

    /** 工区ID */
    private Long areaId;

    /** 工区名称 */
    private String areaName;

    /** 车间ID */
    private Long workshopId;

    /** 车间名称 */
    private String workshopName;

    /** 填报记录数 */
    private Long reportCount;

    /** 参与填报人数 */
    private Long employeeCount;

    /** 工时合计（分钟） */
    private BigDecimal totalMinutes;

    /** 工分合计 */
    private BigDecimal totalPoints;

    /** 施工工时小计 */
    private BigDecimal constructionMinutes;

    /** 施工配合工时小计 */
    private BigDecimal cooperationMinutes;

    /** 维修工时小计 */
    private BigDecimal maintenanceMinutes;

    /** 其他项目工时小计 */
    private BigDecimal otherMinutes;
}
