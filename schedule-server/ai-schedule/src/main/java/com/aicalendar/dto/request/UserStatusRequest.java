package com.aicalendar.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台用户启用/禁用请求对象
 */
@Data
public class UserStatusRequest {

    /** 状态（1启用/0禁用） */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
