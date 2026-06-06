package com.cyp.nanningworkloadreportsystem.config;

import com.cyp.nanningworkloadreportsystem.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * UserContext 清理拦截器
 * 在每个 HTTP 请求结束后（afterCompletion）清理 ThreadLocal，
 * 防止内存泄漏和线程池复用时数据串扰
 */
public class UserContextCleanupInterceptor implements HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                 Object handler, Exception ex) {
        // 请求结束后必须清理 ThreadLocal
        UserContext.clear();
    }
}
