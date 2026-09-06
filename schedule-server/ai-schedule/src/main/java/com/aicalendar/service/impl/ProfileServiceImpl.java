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
    public ProfileResponse getProfile() {
        // 查询用户信息，如果不存在则创建默认用户
        Profile profile = profileMapper.selectById(1L);
        if (profile == null) {
            profile = createDefaultProfile();
        }

        return convertToResponse(profile);
    }

    @Override
    public ProfileResponse updateProfile(ProfileUpdateRequest request) {
        Profile profile = profileMapper.selectById(1L);
        if (profile == null) {
            profile = createDefaultProfile();
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
        return convertToResponse(profile);
    }

    /**
     * 创建默认用户信息
     */
    private Profile createDefaultProfile() {
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setUsername("小橘");
        profile.setDescription("@xiao_ju · 生活记录者");
        profile.setRegisterDate(LocalDate.now());
        
        // 检查是否已存在记录
        if (profileMapper.count() == 0) {
            profileMapper.insert(profile);
        }
        
        return profile;
    }

    /**
     * 转换为响应DTO，包含统计数据
     */
    private ProfileResponse convertToResponse(Profile profile) {
        ProfileResponse response = new ProfileResponse();
        BeanUtils.copyProperties(profile, response);

        // 计算使用天数
        LocalDate today = LocalDate.now();
        LocalDate registerDate = profile.getRegisterDate();
        if (registerDate != null) {
            response.setDaysUsing((int) ChronoUnit.DAYS.between(registerDate, today) + 1);
        }

        // 查询日程数量
        response.setScheduleCount(scheduleMapper.selectCount(new LambdaQueryWrapper<>()).intValue());

        // 查询纪念日数量
        response.setMemorialCount(memorialMapper.selectCount(new LambdaQueryWrapper<>()).intValue());

        return response;
    }
}