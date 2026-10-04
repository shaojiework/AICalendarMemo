package com.aicalendar.common;

/**
 * 令牌类型枚举：区分访问令牌与刷新令牌
 * 两类令牌使用不同密钥签发并写入typ声明，防止刷新令牌被当作访问令牌直接访问业务接口
 */
public enum TokenType {

    /** 访问令牌：短寿命，随请求头携带 */
    ACCESS("ACCESS"),

    /** 刷新令牌：长寿命，仅用于换取新的令牌对 */
    REFRESH("REFRESH");

    private final String value;

    TokenType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
