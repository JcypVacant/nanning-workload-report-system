package com.cyp.nanningworkloadreportsystem.common;

import lombok.Data;

/**
 * 统一API响应结果封装类
 * 所有Controller返回数据均使用此类包装，确保前端接收格式一致
 *
 * @param <T> 响应数据类型
 */
@Data
public class Result<T> {

    /** 状态码：200-成功，其他-失败 */
    private int code;

    /** 响应消息 */
    private String msg;

    /** 响应数据 */
    private T data;

    /**
     * 私有构造方法，通过静态工厂方法创建实例
     */
    private Result() {}

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ==================== 成功响应 ====================

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> ok() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功响应（自定义消息 + 数据）
     *
     * @param msg  自定义消息
     * @param data 响应数据
     */
    public static <T> Result<T> ok(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    // ==================== 失败响应 ====================

    /**
     * 失败响应（默认错误码500）
     *
     * @param msg 错误消息
     */
    public static <T> Result<T> fail(String msg) {
        return new Result<>(500, msg, null);
    }

    /**
     * 失败响应（自定义错误码）
     *
     * @param code 错误码
     * @param msg  错误消息
     */
    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 未授权响应（401）
     */
    public static <T> Result<T> unauthorized(String msg) {
        return new Result<>(401, msg, null);
    }

    /**
     * 禁止访问响应（403）
     */
    public static <T> Result<T> forbidden(String msg) {
        return new Result<>(403, msg, null);
    }

    /**
     * 资源不存在响应（404）
     */
    public static <T> Result<T> notFound(String msg) {
        return new Result<>(404, msg, null);
    }

    /**
     * 参数校验失败响应（400）
     */
    public static <T> Result<T> badRequest(String msg) {
        return new Result<>(400, msg, null);
    }
}
