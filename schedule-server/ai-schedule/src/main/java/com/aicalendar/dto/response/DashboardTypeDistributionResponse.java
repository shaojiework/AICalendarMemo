package com.aicalendar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 仪表盘日程类型分布响应对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardTypeDistributionResponse {

    /** 类型分布列表 */
    private List<TypeItem> items;

    /**
     * 单个类型分布项
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TypeItem {
        /** 类型名称（中文展示） */
        private String name;
        /** 该类型日程数 */
        private Long value;
    }
}
