package com.aicalendar.service.impl;

import com.aicalendar.dto.request.ProfileUpdateRequest;
import com.aicalendar.dto.response.ProfileResponse;
import com.aicalendar.entity.Profile;
import com.aicalendar.mapper.MemorialMapper;
import com.aicalendar.mapper.ProfileMapper;
import com.aicalendar.mapper.ScheduleMapper;
import com.aicalendar.service.ProfileService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 用户信息服务实现类
 * profile.id 即 userId，按当前登录用户查询；不存在时返回默认占位（不写库）
 * 统计数据按 userId 在 schedule/memorial 表统计
 */
@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileMapper profileMapper;
    private final ScheduleMapper scheduleMapper;
    private final MemorialMapper memorialMapper;

    public ProfileServiceImpl(ProfileMapper profileMapper,
                             ScheduleMapper scheduleMapper,
                             MemorialMapper memorialMapper) {
        this.profileMapper = profileMapper;
        this.scheduleMapper = scheduleMapper;
        this.memorialMapper = memorialMapper;
    }

    @Override
    public ProfileResponse getProfile(Long userId) {
        // 按当前登录用户ID查询profile
        Profile profile = profileMapper.selectById(userId);
        if (profile == null) {
            profile = buildDefaultProfile(userId);
        }

        return convertToResponse(profile, userId);
    }

    @Override
    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        Profile profile = profileMapper.selectById(userId);
        if (profile == null) {
            // 首次更新：插入新记录，id=userId
            profile = buildDefaultProfile(userId);
            if (request.getUsername() != null) {
                profile.setUsername(request.getUsername());
            }
            if (request.getAvatar() != null) {
                profile.setAvatar(request.getAvatar());
            }
            if (request.getDescription() != null) {
                profile.setDescription(request.getDescription());
            }
            profileMapper.insert(profile);
            return convertToResponse(profile, userId);
        }

        if (request.getUsername() != null) {
            profile.setUsername(request.getUsername());
        }
        if (request.getAvatar() != null) {
            profile.setAvatar(request.getAvatar());
        }
        if (request.getDescription() != null) {
            profile.setDescription(request.getDescription());
        }

        profileMapper.updateById(profile);
        return convertToResponse(profile, userId);
    }

    /**
     * 构建默认profile（仅内存，不写库）
     */
    private Profile buildDefaultProfile(Long userId) {
        Profile profile = new Profile();
        profile.setId(userId);
        profile.setUsername("朋友");
        profile.setDescription("生活记录者");
        profile.setRegisterDate(LocalDate.now());
        return profile;
    }

    /**
     * 转换为响应DTO，包含按userId统计的日程/纪念日数量
     */
    private ProfileResponse convertToResponse(Profile profile, Long userId) {
        ProfileResponse response = new ProfileResponse();
        BeanUtils.copyProperties(profile, response);

        // 计算使用天数
        LocalDate today = LocalDate.now();
        LocalDate registerDate = profile.getRegisterDate();
        if (registerDate != null) {
            response.setDaysUsing((int) ChronoUnit.DAYS.between(registerDate, today) + 1);
        }

        // 按userId统计日程数量
        LambdaQueryWrapper<com.aicalendar.entity.Schedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(com.aicalendar.entity.Schedule::getUserId, userId);
        response.setScheduleCount(scheduleMapper.selectCount(scheduleWrapper).intValue());

        // 按userId统计纪念日数量
        LambdaQueryWrapper<com.aicalendar.entity.Memorial> memorialWrapper = new LambdaQueryWrapper<>();
        memorialWrapper.eq(com.aicalendar.entity.Memorial::getUserId, userId);
        response.setMemorialCount(memorialMapper.selectCount(memorialWrapper).intValue());

        return response;
    }
}
