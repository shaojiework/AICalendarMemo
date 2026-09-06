package com.aicalendar.service;

import com.aicalendar.dto.request.MemorialCreateRequest;
import com.aicalendar.dto.request.MemorialUpdateRequest;
import com.aicalendar.dto.response.MemorialResponse;

import java.util.List;

/**
 * 纪念日服务接口
 */
public interface MemorialService {

    /**
     * 创建纪念日
     */
    MemorialResponse createMemorial(MemorialCreateRequest request);

    /**
     * 根据ID获取纪念日
     */
    MemorialResponse getMemorialById(Long id);

    /**
     * 获取所有纪念日
     */
    List<MemorialResponse> getAllMemorials();

    /**
     * 按类型查询纪念日
     */
    List<MemorialResponse> getMemorialsByType(String type);

    /**
     * 获取即将到来的纪念日
     */
    List<MemorialResponse> getUpcomingMemorials();

    /**
     * 获取生日列表
     */
    List<MemorialResponse> getBirthdays();

    /**
     * 更新纪念日
     */
    MemorialResponse updateMemorial(Long id, MemorialUpdateRequest request);

    /**
     * 删除纪念日
     */
    void deleteMemorial(Long id);
}