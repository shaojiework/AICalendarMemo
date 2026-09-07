package com.aicalendar.controller.app;

import com.aicalendar.dto.request.ProfileUpdateRequest;
import com.aicalendar.dto.response.ProfileResponse;
import com.aicalendar.dto.response.Result;
import com.aicalendar.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息控制器（profile.id即userId，按当前登录用户查询）
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * 获取当前登录用户信息（包含统计数据）
     */
    @GetMapping
    public Result<ProfileResponse> getProfile(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        ProfileResponse response = profileService.getProfile(userId);
        return Result.success("查询成功", response);
    }

    /**
     * 更新当前登录用户信息
     */
    @PutMapping
    public Result<ProfileResponse> updateProfile(@RequestBody ProfileUpdateRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        ProfileResponse response = profileService.updateProfile(userId, request);
        return Result.success("更新成功", response);
    }
}
