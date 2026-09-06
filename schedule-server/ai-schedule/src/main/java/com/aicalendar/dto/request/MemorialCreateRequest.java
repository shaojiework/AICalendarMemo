package com.aicalendar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MemorialCreateRequest {

    @NotBlank(message = "名称不能为空")
    private String name;

    @NotNull(message = "日期不能为空")
    private LocalDate date;

    private String type;

    private String description;

    private Integer isYearly;

    private String color;

    private String avatar;
}