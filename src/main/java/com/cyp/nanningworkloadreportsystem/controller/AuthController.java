package com.cyp.nanningworkloadreportsystem.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.cyp.nanningworkloadreportsystem.common.Result;
import com.cyp.nanningworkloadreportsystem.entity.SysUser;
import com.cyp.nanningworkloadreportsystem.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器
 * 负责处理登录、登出、获取用户信息、修改密码等认证相关请求
 */
@Tag(name = "认证管理", description = "登录、登出、获取用户信息、修改密码")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;

    /**
     * 用户登录
     * POST /api/v1/auth/login
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        // 执行登录，获取 Token
        String token = sysUserService.login(request.getUsername(), request.getPassword());

        // 获取用户信息
        SysUser user = sysUserService.getCurrentUser();

        // 构建登录响应
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("roleCode", user.getRoleCode());
        userInfo.put("scopeType", StpUtil.getSession().getString("scopeType"));
        userInfo.put("orgId", StpUtil.getSession().getLong("orgId"));
        userInfo.put("orgName", user.getOrgName());
        result.put("userInfo", userInfo);

        return Result.ok(result);
    }

    /**
     * 用户登出
     * POST /api/v1/auth/logout
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        sysUserService.logout();
        return Result.ok();
    }

    /**
     * 获取当前登录用户信息
     * GET /api/v1/auth/user-info
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user-info")
    public Result<Map<String, Object>> getUserInfo() {
        SysUser user = sysUserService.getCurrentUser();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("roleCode", user.getRoleCode());
        userInfo.put("scopeType", StpUtil.getSession().getString("scopeType"));
        userInfo.put("orgId", StpUtil.getSession().getLong("orgId"));
        userInfo.put("orgName", user.getOrgName());

        return Result.ok(userInfo);
    }

    /**
     * 修改当前用户密码
     * PUT /api/v1/auth/password
     */
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        sysUserService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.ok();
    }

    // ==================== 请求DTO（内部类） ====================

    /**
     * 登录请求DTO
     */
    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;
    }

    /**
     * 修改密码请求DTO
     */
    @Data
    public static class ChangePasswordRequest {
        @NotBlank(message = "原密码不能为空")
        private String oldPassword;

        @NotBlank(message = "新密码不能为空")
        private String newPassword;
    }
}
