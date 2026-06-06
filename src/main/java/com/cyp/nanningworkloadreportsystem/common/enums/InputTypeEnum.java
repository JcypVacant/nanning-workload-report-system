package com.cyp.nanningworkloadreportsystem.common.enums;

/**
 * 用工项目输入类型枚举
 * 用于区分用工项目字典中不同节点的类型
 */
public enum InputTypeEnum {

    /** 分类项 - 仅用于分类，不可填写数值 */
    CATEGORY("CATEGORY", "分类项"),

    /** 数值项 - 可填写数值（分钟或工分） */
    NUMBER("NUMBER", "数值项"),

    /** 文本项 - 可填写文字内容（如备注、培训班名称） */
    TEXT("TEXT", "文本项");

    /** 类型编码 */
    private final String code;

    /** 类型中文名称 */
    private final String name;

    InputTypeEnum(String code, String name) {
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
