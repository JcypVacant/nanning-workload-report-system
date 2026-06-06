package com.cyp.nanningworkloadreportsystem.security;

import cn.dev33.satoken.stp.StpInterface;
import com.cyp.nanningworkloadreportsystem.mapper.SysUserOrgScopeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限加载接口实现
 * 负责从数据库加载当前用户的角色列表和权限码列表
 *
 * Sa-Token 会在每次鉴权时调用此接口获取最新权限，
 * 确保权限的实时性（不依赖缓存）
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private SysUserOrgScopeMapper sysUserOrgScopeMapper;

    /**
     * 获取当前用户的权限码列表
     * 用于 @SaCheckPermission 注解的权限校验
     *
     * @param loginId   当前登录用户ID
     * @param loginType 登录类型
     * @return 权限码列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 获取用户角色列表
        List<String> roles = getRoleList(loginId, loginType);

        // 根据角色生成权限码
        List<String> permissions = new ArrayList<>();
        for (String role : roles) {
            switch (role) {
                case "SECTION_ADMIN" -> {
                    // 段级管理员拥有全部权限
                    permissions.add("org:manage");
                    permissions.add("user:manage");
                    permissions.add("employee:manage");
                    permissions.add("work-item:manage");
                    permissions.add("period:manage");
                    permissions.add("report:all");
                    permissions.add("audit:all");
                    permissions.add("summary:section");
                    permissions.add("statistics:all");
                    permissions.add("export:all");
                    permissions.add("log:view");
                }
                case "WORKSHOP_ADMIN" -> {
                    // 车间管理员权限
                    permissions.add("employee:view");
                    permissions.add("report:workshop");
                    permissions.add("audit:workshop");
                    permissions.add("summary:workshop");
                    permissions.add("statistics:workshop");
                    permissions.add("export:workshop");
                }
                case "AREA_REPORTER" -> {
                    // 工区填报员权限
                    permissions.add("report:area");
                    permissions.add("statistics:area");
                    permissions.add("export:area");
                }
            }
        }
        return permissions;
    }

    /**
     * 获取当前用户的角色列表
     * 用于 @SaCheckRole 注解的角色校验
     *
     * @param loginId   当前登录用户ID
     * @param loginType 登录类型
     * @return 角色编码列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());
        // 从数据库查询用户的角色列表
        return sysUserOrgScopeMapper.selectRoleCodesByUserId(userId);
    }
}
