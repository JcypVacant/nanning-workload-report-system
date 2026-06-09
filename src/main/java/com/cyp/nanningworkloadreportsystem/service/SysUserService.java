package com.cyp.nanningworkloadreportsystem.service;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyp.nanningworkloadreportsystem.entity.SysUser;
import com.cyp.nanningworkloadreportsystem.entity.SysUserOrgScope;
import com.cyp.nanningworkloadreportsystem.mapper.SysUserMapper;
import com.cyp.nanningworkloadreportsystem.mapper.SysUserOrgScopeMapper;
import com.cyp.nanningworkloadreportsystem.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户服务类
 * 负责用户登录认证、密码管理、账号CRUD等核心业务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserOrgScopeMapper sysUserOrgScopeMapper;
    private final OperationLogService logService;

    /** 系统默认密码（从配置文件读取） */
    @Value("${app.default-password:123456}")
    private String defaultPassword;

    /**
     * 用户登录
     * 验证用户名和密码，成功后返回 Token 和用户信息
     *
     * @param username 用户名
     * @param password 密码（明文，后端进行BCrypt比对）
     * @return Token 字符串
     * @throws RuntimeException 账号不存在、已禁用或密码错误
     */
    public String login(String username, String password) {
        // 1. 基本格式校验
        if (username == null || username.length() < 5 || username.length() > 20) {
            throw new RuntimeException("账号或密码错误");
        }
        if (password == null || password.length() < 6 || password.length() > 20) {
            throw new RuntimeException("账号或密码错误");
        }

        // 2. 查询用户
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new RuntimeException("账号或密码错误");
        }

        // 3. 检查账号是否启用
        if (user.getEnabled() != null && user.getEnabled() == 0) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        // 4. 验证密码（BCrypt）
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new RuntimeException("账号或密码错误");
        }

        // 4. 执行 Sa-Token 登录
        StpUtil.login(user.getId());
        // 将用户信息保存到 Sa-Token Session
        setUserInfoToSession(user);

        // 5. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        // 6. 记录登录日志（先填充UserContext，因为此时拦截器还未设置）
        log.info("用户 {} (ID={}) 登录成功", username, user.getId());
        UserContext.setUserId(user.getId());
        UserContext.setUsername(user.getUsername());
        UserContext.setRealName(user.getRealName());
        logService.record("用户认证", "LOGIN", String.valueOf(user.getId()),
                "用户登录: " + username);
        return StpUtil.getTokenValue();
    }

    /**
     * 将当前登录用户的角色、数据范围信息存入 Sa-Token Session
     */
    private void setUserInfoToSession(SysUser user) {
        // 查询用户角色绑定信息
        List<SysUserOrgScope> scopes = sysUserOrgScopeMapper.selectByUserId(user.getId());
        if (scopes != null && !scopes.isEmpty()) {
            SysUserOrgScope scope = scopes.get(0);  // 取第一个绑定作为主角色
            StpUtil.getSession().set("roleCode", scope.getRoleCode());
            StpUtil.getSession().set("scopeType", scope.getScopeType());
            StpUtil.getSession().set("orgId", scope.getOrgId());
        }
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("realName", user.getRealName());
    }

    /**
     * 获取当前登录用户的详细信息
     */
    public SysUser getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user != null) {
            // 脱敏：不返回密码
            user.setPassword(null);
            // 加载角色信息
            List<SysUserOrgScope> scopes = sysUserOrgScopeMapper.selectByUserId(userId);
            if (scopes != null && !scopes.isEmpty()) {
                SysUserOrgScope scope = scopes.get(0);
                user.setRoleCode(scope.getRoleCode());
                user.setOrgId(scope.getOrgId());
            }
        }
        return user;
    }

    /**
     * 修改当前用户密码
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 新密码长度校验
        if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 20) {
            throw new RuntimeException("新密码长度必须为6-20个字符");
        }

        // 验证旧密码
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        // 加密并更新新密码
        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        user.setFirstLogin(0);  // 标记为非首次登录
        sysUserMapper.updateById(user);

        log.info("用户 {} (ID={}) 修改密码成功", user.getUsername(), userId);
        logService.record("个人中心", "UPDATE", String.valueOf(userId), "修改密码");
    }

    /**
     * 分页查询用户列表
     * 手动分页：先 COUNT 获取总数，再用 LIMIT 拼到 SQL 尾部
     */
    public IPage<SysUser> getPage(Integer pageNum, Integer pageSize, String keyword) {
        // 构建查询条件（不含 LIMIT）
        LambdaQueryWrapper<SysUser> countWrapper = new LambdaQueryWrapper<SysUser>()
                .and(keyword != null, w -> w
                    .like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getRealName, keyword));

        // 1. 查总数
        Long total = sysUserMapper.selectCount(countWrapper);

        // 2. 查当前页数据：手动拼接 LIMIT offset, size
        LambdaQueryWrapper<SysUser> dataWrapper = new LambdaQueryWrapper<SysUser>()
                .and(keyword != null, w -> w
                    .like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getRealName, keyword))
                .orderByDesc(SysUser::getCreateTime)
                .last("LIMIT " + ((pageNum - 1) * pageSize) + ", " + pageSize);

        List<SysUser> records = sysUserMapper.selectList(dataWrapper);

        // 脱敏处理并加载角色
        records.forEach(user -> {
            user.setPassword(null);
            List<SysUserOrgScope> scopes = sysUserOrgScopeMapper.selectByUserId(user.getId());
            if (scopes != null && !scopes.isEmpty()) {
                SysUserOrgScope scope = scopes.get(0);
                user.setRoleCode(scope.getRoleCode());
                user.setOrgId(scope.getOrgId());
            }
        });

        // 构建分页结果
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    /**
     * 创建新用户账号
     */
    @Transactional
    public SysUser createUser(SysUser user, String roleCode, Long orgId) {
        // 校验账号长度
        String username = user.getUsername();
        if (username == null || username.length() < 5 || username.length() > 20) {
            throw new RuntimeException("账号长度必须为5-20个字符");
        }
        // 检查用户名是否已存在
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 设置默认密码（BCrypt加密）
        user.setPassword(BCrypt.hashpw(defaultPassword, BCrypt.gensalt()));
        user.setFirstLogin(1);
        user.setEnabled(1);
        sysUserMapper.insert(user);

        // 保存用户组织角色绑定
        saveUserOrgScope(user.getId(), roleCode, orgId);

        log.info("创建用户账号: username={}, role={}", user.getUsername(), roleCode);
        logService.record("账号管理", "CREATE", String.valueOf(user.getId()),
                "创建账号: " + user.getUsername() + "(" + roleCode + ")");
        return user;
    }

    /**
     * 保存用户组织角色绑定
     */
    private void saveUserOrgScope(Long userId, String roleCode, Long orgId) {
        SysUserOrgScope scope = new SysUserOrgScope();
        scope.setUserId(userId);
        scope.setRoleCode(roleCode);
        scope.setOrgId(orgId != null ? orgId : 0L);

        // 根据角色确定数据范围类型
        switch (roleCode) {
            case "SECTION_ADMIN" -> scope.setScopeType("ALL");
            case "WORKSHOP_ADMIN" -> scope.setScopeType("WORKSHOP");
            case "AREA_REPORTER" -> scope.setScopeType("AREA");
        }
        sysUserOrgScopeMapper.insert(scope);
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public void updateUser(SysUser user, String roleCode, Long orgId) {
        // 不允许修改用户名
        user.setUsername(null);
        user.setPassword(null);
        sysUserMapper.updateById(user);

        // 更新角色绑定（先删后增）
        if (roleCode != null) {
            sysUserOrgScopeMapper.delete(
                    new LambdaQueryWrapper<SysUserOrgScope>().eq(SysUserOrgScope::getUserId, user.getId()));
            saveUserOrgScope(user.getId(), roleCode, orgId);
        }
        logService.record("账号管理", "UPDATE", String.valueOf(user.getId()),
                "更新账号信息: " + (user.getRealName() != null ? user.getRealName() : ""));
    }

    /**
     * 重置用户密码为默认密码
     */
    @Transactional
    public void resetPassword(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(BCrypt.hashpw(defaultPassword, BCrypt.gensalt()));
        user.setFirstLogin(1);
        sysUserMapper.updateById(user);
        log.info("用户 {} (ID={}) 密码已重置", user.getUsername(), userId);
        logService.record("账号管理", "UPDATE", String.valueOf(userId), "重置密码: " + user.getUsername());
    }

    /**
     * 启用/禁用用户
     */
    public void toggleEnabled(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setEnabled(user.getEnabled() == 1 ? 0 : 1);
        sysUserMapper.updateById(user);
        logService.record("账号管理", "UPDATE", String.valueOf(userId),
                (user.getEnabled() == 1 ? "启用" : "禁用") + "账号: " + user.getUsername());
    }

    /**
     * 用户退出登录
     */
    public void logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }
}
