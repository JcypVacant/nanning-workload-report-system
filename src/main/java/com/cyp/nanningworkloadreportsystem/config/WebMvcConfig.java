package com.cyp.nanningworkloadreportsystem.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.cyp.nanningworkloadreportsystem.util.UserContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 配置：
 * 1. Sa-Token 登录拦截器（所有 /api/** 接口均需登录）
 * 2. 请求结束后清理 UserContext ThreadLocal
 * 3. 跨域 CORS 配置（开发环境允许前端跨域访问）
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 注册 Sa-Token 拦截器
     * 拦截所有 /api/** 路径，校验用户是否已登录
     * 同时忽略 Knife4j 和 SpringDoc 的文档路径
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Sa-Token 登录校验拦截器
        registry.addInterceptor(new SaInterceptor(handle -> {
                    // 登录校验（由 SaInterceptor 自动完成）
                    // 校验通过后，将用户信息设置到 UserContext ThreadLocal
                    if (StpUtil.isLogin()) {
                        UserContext.setUserId(StpUtil.getLoginIdAsLong());
                        // 从 Sa-Token Session 中读取用户角色和范围信息
                        UserContext.setRoleCode((String) StpUtil.getSession().get("roleCode"));
                        UserContext.setScopeType((String) StpUtil.getSession().get("scopeType"));
                        UserContext.setOrgId((Long) StpUtil.getSession().get("orgId"));
                        UserContext.setUsername((String) StpUtil.getSession().get("username"));
                        UserContext.setRealName((String) StpUtil.getSession().get("realName"));
                    }
                }))
                .addPathPatterns("/api/**")           // 拦截所有 API 请求
                .excludePathPatterns(
                        "/api/v1/auth/login",          // 登录接口不需要登录
                        "/doc.html",                    // Knife4j 文档页
                        "/v3/api-docs/**",             // SpringDoc API 文档
                        "/swagger-ui/**",               // Swagger UI
                        "/swagger-resources/**",        // Swagger 资源
                        "/webjars/**"                   // WebJars 静态资源
                );

        // 请求结束后的清理拦截器
        registry.addInterceptor(new UserContextCleanupInterceptor())
                .addPathPatterns("/api/**");
    }
}
