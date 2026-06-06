package com.cyp.nanningworkloadreportsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 段级汇总DTO — 按车间分组汇总工时工分数据
 */
@Data
public class SectionSummaryDTO {

    /** 车间ID */
    private Long workshopId;

    /** 车间名称 */
    private String workshopName;

    /** 下属工区数量 */
    private Long areaCount;

    /** 填报记录总数 */
    private Long reportCount;

    /** 参与填报总人数 */
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

    /** 下属工区明细（展开时使用） */
    private List<WorkshopSummaryDTO> areas;
}
