package com.cyp.nanningworkloadreportsystem.common.enums;

/**
 * 审核操作类型枚举
 * 记录审核流程中各操作的类型
 */
public enum AuditActionEnum {

    /** 提交 - 工区或车间本级提交数据 */
    SUBMIT("SUBMIT", "提交"),

    /** 审批通过 - 车间审核通过工区数据 */
    APPROVE("APPROVE", "审核通过"),

    /** 退回 - 车间退回工区数据 */
    RETURN("RETURN", "退回修改"),

    /** 锁定 - 段级锁定月份数据 */
    LOCK("LOCK", "锁定"),

    /** 解锁 - 段级解锁月份数据 */
    UNLOCK("UNLOCK", "解锁");

    /** 操作编码 */
    private final String code;

    /** 操作中文名称 */
    private final String name;

    AuditActionEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
