package com.aicalendar.dto.response;

import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.util.List;

/**
 * 统一分页响应对象：后台管理端分页接口统一返回该结构
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResponse<T> {

    /** 当前页数据列表 */
    private List<T> list;

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private int page;

    /** 每页条数 */
    private int pageSize;

    /**
     * 基于 PageHelper 的 PageInfo 组装分页响应（查询结果类型与响应类型一致时使用）
     */
    public static <T> PageResponse<T> of(PageInfo<T> pageInfo) {
        PageResponse<T> response = new PageResponse<>();
        response.setList(pageInfo.getList());
        response.setTotal(pageInfo.getTotal());
        response.setPage(pageInfo.getPageNum());
        response.setPageSize(pageInfo.getPageSize());
        return response;
    }

    /**
     * 组装分页响应：实体列表已转换为响应列表，分页信息取自 PageInfo
     *
     * @param list     转换后的当前页响应列表
     * @param pageInfo PageHelper 分页信息（提供总数/页码/每页条数）
     */
    public static <T> PageResponse<T> of(List<T> list, PageInfo<?> pageInfo) {
        PageResponse<T> response = new PageResponse<>();
        response.setList(list);
        response.setTotal(pageInfo.getTotal());
        response.setPage(pageInfo.getPageNum());
        response.setPageSize(pageInfo.getPageSize());
        return response;
    }
}
