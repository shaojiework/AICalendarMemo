package com.aicalendar.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新个人信息请求参数（字段均可选，传了才更新）
 */
@Data
public class UpdateUserRequest {

    /** 昵称 */
    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    /** 头像路径 */
    @Size(max = 500, message = "头像路径过长")
    private String avatar;

    /** 手机号 */
    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;
}
