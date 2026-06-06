package com.cyp.nanningworkloadreportsystem.common.enums;

/**
 * 组织类型枚举
 * 定义段-车间-工区-车间本级四种组织类型
 */
public enum OrgTypeEnum {

    /** 段级 - 最高组织层级，例如：南宁通信段 */
    SECTION("SECTION", "段级"),

    /** 车间 - 中间组织层级，例如：贺州通信车间 */
    WORKSHOP("WORKSHOP", "车间"),

    /** 工区 - 最底层组织，例如：梧州通信工区 */
    WORK_AREA("WORK_AREA", "工区"),

    /** 车间本级 - 车间直属人员填报 */
    WORKSHOP_LEVEL("WORKSHOP_LEVEL", "车间本级");

    /** 组织类型编码 */
    private final String code;

    /** 组织类型中文名称 */
    private final String name;

    OrgTypeEnum(String code, String name) {
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
