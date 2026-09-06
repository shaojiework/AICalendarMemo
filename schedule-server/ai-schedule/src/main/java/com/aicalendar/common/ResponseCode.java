package com.aicalendar.common;

/**
 * 响应码枚举
 */
public enum ResponseCode {

    /** 成功 */
    SUCCESS(200, "操作成功"),
    
    /** 失败 */
    ERROR(500, "服务器内部错误"),
    
    /** 参数错误 */
    PARAM_ERROR(400, "参数错误"),
    
    /** 未找到 */
    NOT_FOUND(404, "资源未找到"),
    
    /** 未授权 */
    UNAUTHORIZED(401, "未授权"),
    
    /** 禁止访问 */
    FORBIDDEN(403, "禁止访问");

    private final int code;
    private final String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}