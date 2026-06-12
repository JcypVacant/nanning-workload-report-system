package com.cyp.nanningworkloadreportsystem.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cyp.nanningworkloadreportsystem.common.PageResult;
import com.cyp.nanningworkloadreportsystem.common.Result;
import com.cyp.nanningworkloadreportsystem.entity.SysUser;
import com.cyp.nanningworkloadreportsystem.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户账号管理控制器
 */
@Tag(name = "账号管理", description = "用户账号的增删改查、密码重置、启停")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @Operation(summary = "分页查询用户")
    @SaCheckRole("SECTION_ADMIN")
    @GetMapping("/page")
    public Result<PageResult<SysUser>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String roleCode) {
        IPage<SysUser> page = sysUserService.getPage(pageNum, pageSize, keyword, roleCode);
        return Result.ok(PageResult.of(page.getTotal(), pageNum, pageSize, page.getRecords()));
    }

    @Operation(summary = "新增用户")
    @SaCheckRole("SECTION_ADMIN")
    @PostMapping
    public Result<SysUser> create(@RequestBody CreateUserRequest req) {
        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setRealName(req.getRealName());
        user.setPhone(req.getPhone());
        return Result.ok(sysUserService.createUser(user, req.getRoleCode(), req.getOrgId()));
    }

    @Operation(summary = "更新用户")
    @SaCheckRole("SECTION_ADMIN")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CreateUserRequest req) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setRealName(req.getRealName());
        user.setPhone(req.getPhone());
        sysUserService.updateUser(user, req.getRoleCode(), req.getOrgId());
        return Result.ok();
    }

    @Operation(summary = "重置密码")
    @SaCheckRole("SECTION_ADMIN")
    @PatchMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        sysUserService.resetPassword(id);
        return Result.ok();
    }

    @Operation(summary = "启停用户")
    @SaCheckRole("SECTION_ADMIN")
    @PatchMapping("/{id}/toggle")
    public Result<Void> toggleEnabled(@PathVariable Long id) {
        sysUserService.toggleEnabled(id);
        return Result.ok();
    }

    @Data
    public static class CreateUserRequest {
        private String username;
        private String realName;
        private String phone;
        private String roleCode;
        private Long orgId;
    }
}
