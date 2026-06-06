package com.cyp.nanningworkloadreportsystem.util;

import com.cyp.nanningworkloadreportsystem.common.enums.RoleEnum;

/**
 * 当前用户上下文工具类
 * 使用 ThreadLocal 保存当前请求的用户信息，线程安全
 * 主要用于在 Service 层获取当前操作用户的 ID、角色、数据范围等信息
 * 请求结束后由拦截器清理 ThreadLocal，防止内存泄漏
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLE_CODE = new ThreadLocal<>();
    private static final ThreadLocal<String> SCOPE_TYPE = new ThreadLocal<>();
    private static final ThreadLocal<Long> ORG_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<String> REAL_NAME = new ThreadLocal<>();

    /**
     * 设置当前用户信息到 ThreadLocal
     */
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static void setRoleCode(String roleCode) {
        ROLE_CODE.set(roleCode);
    }

    public static void setScopeType(String scopeType) {
        SCOPE_TYPE.set(scopeType);
    }

    public static void setOrgId(Long orgId) {
        ORG_ID.set(orgId);
    }

    public static void setUsername(String username) {
        USERNAME.set(username);
    }

    public static void setRealName(String realName) {
        REAL_NAME.set(realName);
    }

    /**
     * 获取当前用户信息
     */
    public static Long getUserId() {
        return USER_ID.get();
    }

    public static String getRoleCode() {
        return ROLE_CODE.get();
    }

    public static String getScopeType() {
        return SCOPE_TYPE.get();
    }

    public static Long getOrgId() {
        return ORG_ID.get();
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static String getRealName() {
        return REAL_NAME.get();
    }

    /**
     * 判断当前用户是否为段级管理员（全段数据权限）
     */
    public static boolean isSectionAdmin() {
        return RoleEnum.SECTION_ADMIN.getCode().equals(ROLE_CODE.get());
    }

    /**
     * 判断当前用户是否为车间管理员
     */
    public static boolean isWorkshopAdmin() {
        return RoleEnum.WORKSHOP_ADMIN.getCode().equals(ROLE_CODE.get());
    }

    /**
     * 判断当前用户是否为工区填报员
     */
    public static boolean isAreaReporter() {
        return RoleEnum.AREA_REPORTER.getCode().equals(ROLE_CODE.get());
    }

    /**
     * 清理 ThreadLocal，防止内存泄漏
     * 必须在每次请求结束后调用（通过拦截器或过滤器）
     */
    public static void clear() {
        USER_ID.remove();
        ROLE_CODE.remove();
        SCOPE_TYPE.remove();
        ORG_ID.remove();
        USERNAME.remove();
        REAL_NAME.remove();
    }
}
