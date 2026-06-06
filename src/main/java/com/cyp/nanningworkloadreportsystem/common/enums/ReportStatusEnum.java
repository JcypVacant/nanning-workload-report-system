package com.cyp.nanningworkloadreportsystem.common.enums;

/**
 * 填报数据状态枚举
 * 定义填报数据从草稿到锁定的完整生命周期
 *
 * 状态流转规则：
 * 草稿 -> 已提交（工区提交审核）
 * 已提交 -> 已审核（车间审核通过）
 * 已提交 -> 已退回（车间退回）
 * 已退回 -> 已提交（工区修改后重新提交）
 * 已审核 -> 已锁定（段级锁定）
 */
public enum ReportStatusEnum {

    /** 草稿 - 工区或车间本级正在填写，可修改 */
    DRAFT("草稿", "工区正在填写，可随时修改"),

    /** 已提交 - 已提交车间审核，不可随意修改 */
    SUBMITTED("已提交", "已提交至车间，等待审核"),

    /** 已退回 - 被车间退回，可修改后重新提交 */
    RETURNED("已退回", "数据被退回，需修改后重新提交"),

    /** 已审核 - 车间审核通过 */
    APPROVED("已审核", "车间审核通过，已进入段级汇总"),

    /** 已锁定 - 段级锁定，普通账号不可修改 */
    LOCKED("已锁定", "段级已锁定，不可修改");

    /** 状态名称 */
    private final String name;

    /** 状态说明 */
    private final String description;

    ReportStatusEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
