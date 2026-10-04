package com.aicalendar.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新令牌请求参数（前端 → 后端）
 * 仅接受刷新令牌字符串，用户身份从令牌自身解析，禁止前端传userId
 */
@Data
public class RefreshTokenRequest {

    /** 刷新令牌 */
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}
