package com.aicalendar.service;

import com.aicalendar.dto.response.HomeResponse;

/**
 * 首页服务接口
 */
public interface HomeService {

    /**
     * 获取首页聚合数据
     */
    HomeResponse getHomeData();
}