package com.aicalendar.common;

/**
 * 令牌校验结果枚举
 * 关键作用：把「仅过期」与「签名非法/类型不符」分开，
 * 前端据此决定是静默刷新后重试，还是清除登录态跳登录页
 */
public enum TokenStatus {

    /** 校验通过 */
    VALID,

    /** 签名正确但已过期：访问令牌过期可刷新 */
    EXPIRED,

    /** 签名非法、格式错误或令牌类型不符：必须重新登录 */
    INVALID
}
