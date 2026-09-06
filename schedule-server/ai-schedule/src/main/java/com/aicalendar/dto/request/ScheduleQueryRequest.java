package com.aicalendar.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleQueryRequest {

    private LocalDate date;

    private String type;

    private String keyword;
}