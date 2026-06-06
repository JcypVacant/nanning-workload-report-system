package com.cyp.nanningworkloadreportsystem.security;

import com.cyp.nanningworkloadreportsystem.util.UserContext;

/**
 * 数据权限控制工具类
 *
 * 数据权限通过 Service 层手动控制实现：
 * - 各 Service 的查询方法中，根据 UserContext.getRoleCode() 判断当前用户角色
 * - 车间管理员：自动添加 workshop_id = UserContext.getOrgId() 条件
 * - 工区填报员：自动添加 area_id = UserContext.getOrgId() 条件
 * - 段级管理员：不添加限制
 *
 * 这种方式的优点：
 * 1. 不依赖 MyBatis 拦截器的复杂 JSqlParser 解析
 * 2. 逻辑清晰，易于调试和维护
 * 3. 与 MyBatis Plus 版本不耦合
 *
 * 各 Service 类中使用 applyDataScope() 方法即可实现数据权限过滤。
 * 参见 WorkReportService.applyDataScope() 作为参考实现。
 */
public class DataScopeInterceptor {

    private DataScopeInterceptor() {
        // 工具类，禁止实例化
    }

    /**
     * 判断当前用户的数据范围是否允许访问指定的车间
     *
     * @param workshopId 要检查的车间ID
     * @return true 表示允许访问
     */
    public static boolean canAccessWorkshop(Long workshopId) {
        if (UserContext.isSectionAdmin()) {
            return true; // 段级管理员可访问所有车间
        }
        if (UserContext.isWorkshopAdmin()) {
            Long userWorkshopId = UserContext.getOrgId();
            return userWorkshopId != null && userWorkshopId.equals(workshopId);
        }
        return false;
    }

    /**
     * 判断当前用户的数据范围是否允许访问指定的工区
     *
     * @param areaId 要检查的工区ID
     * @return true 表示允许访问
     */
    public static boolean canAccessArea(Long areaId) {
        if (UserContext.isSectionAdmin()) {
            return true;
        }
        if (UserContext.isAreaReporter()) {
            Long userAreaId = UserContext.getOrgId();
            return userAreaId != null && userAreaId.equals(areaId);
        }
        return false;
    }
}
