package com.aicalendar.service;

import com.aicalendar.dto.response.HomeResponse;

/**
 * 首页服务接口（按当前登录用户聚合数据）
 */
public interface HomeService {

    /** 获取某用户的首页聚合数据 */
    HomeResponse getHomeData(Long userId);
}
