package com.cyp.nanningworkloadreportsystem.common.enums;

/**
 * 系统角色枚举
 * 定义三种系统角色及其角色编码
 */
public enum RoleEnum {

    /** 段级管理员 - 系统最高权限，可查看和管理全段数据 */
    SECTION_ADMIN("SECTION_ADMIN", "段级管理员"),

    /** 车间管理员 - 可管理本车间及下属工区数据，支持一个车间多个管理员 */
    WORKSHOP_ADMIN("WORKSHOP_ADMIN", "车间管理员"),

    /** 工区填报员 - 仅可填报和查看本工区数据 */
    AREA_REPORTER("AREA_REPORTER", "工区填报员");

    /** 角色编码（存储于数据库） */
    private final String code;

    /** 角色中文名称 */
    private final String name;

    RoleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据角色编码获取角色枚举
     */
    public static RoleEnum fromCode(String code) {
        for (RoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }
}
