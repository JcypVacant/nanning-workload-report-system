package com.cyp.nanningworkloadreportsystem.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cyp.nanningworkloadreportsystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 月度填报期间实体类
 * 对应数据库表 monthly_period
 * 管理填报月份的状态和周期
 *
 * 状态流转：未开始 -> 填报中 -> 车间审核中 -> 段级汇总中 -> 已锁定 -> 已归档
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("monthly_period")
public class MonthlyPeriod extends BaseEntity {

    /** 期间名称（如：2026年6月） */
    private String periodName;

    /** 年份 */
    private Integer year;

    /** 月份 */
    private Integer month;

    /** 月份状态：未开始/填报中/车间审核中/段级汇总中/已锁定/已归档 */
    private String status;

    /** 填报开始日期 */
    private LocalDate startDate;

    /** 填报截止日期 */
    private LocalDate endDate;

    /** 审核截止日期 */
    private LocalDate auditDeadline;

    /** 是否锁定：1-已锁定, 0-未锁定 */
    private Integer locked;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
