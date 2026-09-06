package com.aicalendar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求参数
 */
@Data
public class RegisterRequest {

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50")
    private String username;

    /** 密码（6-32位） */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度须为6-32位")
    private String password;

    /** 昵称（可选，默认取用户名） */
    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    /** 手机号（可选） */
    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;
}
