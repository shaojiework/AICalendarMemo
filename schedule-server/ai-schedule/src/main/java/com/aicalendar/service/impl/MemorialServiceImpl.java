package com.aicalendar.service.impl;

import com.aicalendar.dto.request.MemorialCreateRequest;
import com.aicalendar.dto.request.MemorialUpdateRequest;
import com.aicalendar.dto.response.MemorialResponse;
import com.aicalendar.entity.Memorial;
import com.aicalendar.mapper.MemorialMapper;
import com.aicalendar.service.MemorialService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 纪念日服务实现类
 */
@Service
public class MemorialServiceImpl implements MemorialService {

    private final MemorialMapper memorialMapper;

    public MemorialServiceImpl(MemorialMapper memorialMapper) {
        this.memorialMapper = memorialMapper;
    }

    @Override
    @Transactional
    public MemorialResponse createMemorial(MemorialCreateRequest request) {
        Memorial memorial = new Memorial();
        BeanUtils.copyProperties(request, memorial);
        
        if (memorial.getColor() == null) {
            memorial.setColor("#FF7B9C");
        }
        if (memorial.getType() == null) {
            memorial.setType("normal");
        }
        if (memorial.getIsYearly() == null) {
            memorial.setIsYearly(1);
        }

        memorialMapper.insert(memorial);
        return convertToResponse(memorial);
    }

    @Override
    public MemorialResponse getMemorialById(Long id) {
        Memorial memorial = memorialMapper.selectById(id);
        if (memorial == null) {
            throw new IllegalArgumentException("纪念日不存在");
        }
        return convertToResponse(memorial);
    }

    @Override
    public List<MemorialResponse> getAllMemorials() {
        List<Memorial> memorials = memorialMapper.selectList(null);
        return memorials.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<MemorialResponse> getMemorialsByType(String type) {
        List<Memorial> memorials = memorialMapper.selectByType(type);
        return memorials.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<MemorialResponse> getUpcomingMemorials() {
        // 查询未来30天内即将到来的纪念日，按日期正序
        List<Memorial> memorials = memorialMapper.selectUpcoming(LocalDate.now(), LocalDate.now().plusDays(30));
        return memorials.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<MemorialResponse> getBirthdays() {
        List<Memorial> memorials = memorialMapper.selectByType("birthday");
        return memorials.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MemorialResponse updateMemorial(Long id, MemorialUpdateRequest request) {
        Memorial memorial = memorialMapper.selectById(id);
        if (memorial == null) {
            throw new IllegalArgumentException("纪念日不存在");
        }

        BeanUtils.copyProperties(request, memorial);
        memorialMapper.updateById(memorial);
        
        return convertToResponse(memorial);
    }

    @Override
    @Transactional
    public void deleteMemorial(Long id) {
        Memorial memorial = memorialMapper.selectById(id);
        if (memorial == null) {
            throw new IllegalArgumentException("纪念日不存在");
        }
        memorialMapper.deleteById(id);
    }

    /**
     * 转换为响应DTO，计算天数
     */
    private MemorialResponse convertToResponse(Memorial memorial) {
        MemorialResponse response = new MemorialResponse();
        BeanUtils.copyProperties(memorial, response);
        
        LocalDate today = LocalDate.now();
        LocalDate memorialDate = memorial.getDate();
        
        // 计算距离天数
        if (memorialDate.isBefore(today)) {
            // 日期已过，计算已过去/在一起的天数
            response.setDaysTogether((int) ChronoUnit.DAYS.between(memorialDate, today));
        } else {
            // 日期未到，计算距离天数
            response.setDaysUntil((int) ChronoUnit.DAYS.between(today, memorialDate));
        }
        
        return response;
    }
}