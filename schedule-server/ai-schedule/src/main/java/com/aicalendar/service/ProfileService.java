package com.aicalendar.service;

import com.aicalendar.dto.request.ProfileUpdateRequest;
import com.aicalendar.dto.response.ProfileResponse;

/**
 * 用户信息服务接口
 */
public interface ProfileService {

    /**
     * 获取用户信息（包含统计数据）
     */
    ProfileResponse getProfile();

    /**
     * 更新用户信息
     */
    ProfileResponse updateProfile(ProfileUpdateRequest request);
}