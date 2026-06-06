package com.cyp.nanningworkloadreportsystem.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.cyp.nanningworkloadreportsystem.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理各类异常，返回标准 Result 响应格式
 * 避免异常信息直接暴露给前端，同时提供可追溯的日志
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 Sa-Token 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleNotLoginException(NotLoginException e) {
        log.warn("用户未登录或Token已过期: {}", e.getMessage());
        return Result.unauthorized("请先登录");
    }

    /**
     * 处理 Sa-Token 角色权限不足异常
     */
    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleNotRoleException(NotRoleException e) {
        log.warn("用户角色权限不足: {}", e.getMessage());
        return Result.forbidden("权限不足，当前角色无法执行此操作");
    }

    /**
     * 处理 Sa-Token 细粒度权限不足异常
     */
    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleNotPermissionException(NotPermissionException e) {
        log.warn("用户权限码不足: {}", e.getMessage());
        return Result.forbidden("权限不足，无法执行此操作");
    }

    /**
     * 处理参数校验异常（@Valid 注解触发的异常）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return Result.badRequest(message);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", message);
        return Result.badRequest(message);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return Result.badRequest(e.getMessage());
    }

    /**
     * 处理业务运行时异常
     * 将异常消息直接返回给前端（用于业务校验错误提示）
     * 对于非预期的运行时异常，记录完整堆栈日志
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleRuntimeException(RuntimeException e) {
        // 如果是业务校验抛出的异常（如：密码错误、账号不存在等），直接返回消息
        // 这类异常的 message 是面向用户的友好提示
        if (e.getMessage() != null && !e.getMessage().contains("Null")) {
            log.warn("业务异常: {}", e.getMessage());
            return Result.fail(e.getMessage());
        }
        // 其他未预期的运行时异常，记录完整堆栈并返回通用错误
        log.error("系统运行时异常: ", e);
        return Result.fail("系统错误，请联系管理员");
    }

    /**
     * 处理其他未预料的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("系统未知异常: ", e);
        return Result.fail("系统错误，请联系管理员");
    }
}
