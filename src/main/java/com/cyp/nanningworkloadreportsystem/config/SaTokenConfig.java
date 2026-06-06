package com.cyp.nanningworkloadreportsystem.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 权限认证配置类
 * 配置 Token 策略、路由拦截规则、全局异常处理
 *
 * 核心职责：
 * 1. 注册 Sa-Token 全局过滤器，校验登录状态
 * 2. 配置路由拦截规则（哪些路径需要登录，哪些放行）
 * 3. 统一处理未登录和权限不足的响应格式
 */
@Configuration
public class SaTokenConfig {

    /**
     * 注册 Sa-Token 全局过滤器
     * 对所有 /api/** 请求进行登录校验
     * 返回统一的 JSON 格式错误响应
     */
    @Bean
    public SaServletFilter getSaServletFilter() {
        return new SaServletFilter()
                // 指定拦截的路由
                .addInclude("/api/**")
                // 指定放行的路由
                .addExclude(
                        "/api/v1/auth/login",
                        "/doc.html",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/favicon.ico"
                )
                // 认证函数：校验是否登录
                .setAuth(obj -> {
                    // 所有 /api/** 请求必须登录
                    StpUtil.checkLogin();
                })
                // 异常处理：未登录时返回 JSON 格式错误
                .setError(e -> {
                    // 返回统一的 JSON 错误响应
                    SaHolder.getResponse()
                            .setHeader("Content-Type", "application/json;charset=UTF-8");
                    return SaResult.error("请先登录");
                });
    }
}
