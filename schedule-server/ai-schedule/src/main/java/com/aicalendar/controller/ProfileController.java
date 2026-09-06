package com.aicalendar.controller;

import com.aicalendar.dto.request.ProfileUpdateRequest;
import com.aicalendar.dto.response.ProfileResponse;
import com.aicalendar.dto.response.Result;
import com.aicalendar.service.ProfileService;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息控制器
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * 获取用户信息（包含统计数据）
     */
    @GetMapping
    public Result<ProfileResponse> getProfile() {
        ProfileResponse response = profileService.getProfile();
        return Result.success("查询成功", response);
    }

    /**
     * 更新用户信息
     */
    @PutMapping
    public Result<ProfileResponse> updateProfile(@RequestBody ProfileUpdateRequest request) {
        ProfileResponse response = profileService.updateProfile(request);
        return Result.success("更新成功", response);
    }
}