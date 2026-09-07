package com.aicalendar.service;

import com.aicalendar.dto.request.MemorialCreateRequest;
import com.aicalendar.dto.request.MemorialUpdateRequest;
import com.aicalendar.dto.response.MemorialResponse;

import java.util.List;

/**
 * 纪念日服务接口（按当前登录用户隔离数据）
 */
public interface MemorialService {

    /** 创建纪念日 */
    MemorialResponse createMemorial(Long userId, MemorialCreateRequest request);

    /** 根据ID获取纪念日（校验归属） */
    MemorialResponse getMemorialById(Long userId, Long id);

    /** 获取某用户所有纪念日 */
    List<MemorialResponse> getAllMemorials(Long userId);

    /** 按类型查询某用户纪念日 */
    List<MemorialResponse> getMemorialsByType(Long userId, String type);

    /** 获取某用户即将到来的纪念日 */
    List<MemorialResponse> getUpcomingMemorials(Long userId);

    /** 获取某用户生日列表 */
    List<MemorialResponse> getBirthdays(Long userId);

    /** 更新纪念日（校验归属） */
    MemorialResponse updateMemorial(Long userId, Long id, MemorialUpdateRequest request);

    /** 删除纪念日（校验归属） */
    void deleteMemorial(Long userId, Long id);
}
