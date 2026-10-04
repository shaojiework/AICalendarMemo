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
    FORBIDDEN(403, "禁止访问"),

    /** 访问令牌已过期：可凭刷新令牌静默续期，无需用户重新登录 */
    TOKEN_EXPIRED(40101, "访问令牌已过期"),

    /** 令牌非法：签名错误、类型不符或已被登出拉黑，必须重新登录 */
    TOKEN_INVALID(40102, "登录状态无效，请重新登录"),

    /** 刷新令牌失效：已过期、已被撤销或已被使用过，必须重新登录 */
    REFRESH_INVALID(40103, "登录已失效，请重新登录");

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