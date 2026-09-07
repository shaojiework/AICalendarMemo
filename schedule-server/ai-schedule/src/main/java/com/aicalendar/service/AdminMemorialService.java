package com.aicalendar.service;

import com.aicalendar.dto.response.AdminMemorialResponse;
import com.aicalendar.dto.response.PageResponse;

/**
 * 后台纪念日管理服务接口
 */
public interface AdminMemorialService {

    /**
     * 分页查询纪念日（按名称模糊、类型过滤）
     *
     * @param keyword  名称关键字
     * @param type     纪念日类型
     * @param pageNum  页码
     * @param pageSize 每页条数
     */
    PageResponse<AdminMemorialResponse> pageMemorials(String keyword, String type, int pageNum, int pageSize);

    /**
     * 根据ID查询纪念日详情
     */
    AdminMemorialResponse getMemorialById(Long id);

    /**
     * 根据ID删除纪念日
     */
    void deleteMemorial(Long id);
}
