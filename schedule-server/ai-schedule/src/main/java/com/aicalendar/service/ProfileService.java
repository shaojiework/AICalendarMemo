package com.aicalendar.service;

import com.aicalendar.dto.request.ProfileUpdateRequest;
import com.aicalendar.dto.response.ProfileResponse;

/**
 * 用户信息服务接口（profile.id即userId，按当前登录用户查询）
 */
public interface ProfileService {

    /** 获取当前登录用户信息（包含统计数据） */
    ProfileResponse getProfile(Long userId);

    /** 更新当前登录用户信息 */
    ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request);
}
