package com.aicalendar.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MemorialResponse {

    private Long id;

    private String name;

    private LocalDate date;

    private String type;

    private String description;

    private Integer isYearly;

    private String color;

    private String avatar;

    private Integer daysUntil;

    private Integer daysTogether;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}