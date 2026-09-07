package com.aicalendar.service.impl;

import com.aicalendar.common.BizException;
import com.aicalendar.common.ResponseCode;
import com.aicalendar.dto.response.AdminMemorialResponse;
import com.aicalendar.dto.response.PageResponse;
import com.aicalendar.entity.Memorial;
import com.aicalendar.mapper.MemorialMapper;
import com.aicalendar.service.AdminMemorialService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 后台纪念日管理服务实现：分页查询、详情、删除
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMemorialServiceImpl implements AdminMemorialService {

    private final MemorialMapper memorialMapper;

    @Override
    public PageResponse<AdminMemorialResponse> pageMemorials(String keyword, String type, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Memorial> pageList = memorialMapper.selectList(Wrappers.<Memorial>lambdaQuery()
                .like(StringUtils.hasText(keyword), Memorial::getName, keyword)
                .eq(StringUtils.hasText(type), Memorial::getType, type)
                .orderByDesc(Memorial::getDate));
        PageInfo<Memorial> pageInfo = new PageInfo<>(pageList);
        List<AdminMemorialResponse> list = pageList.stream().map(this::toResponse).toList();
        return PageResponse.of(list, pageInfo);
    }

    @Override
    public AdminMemorialResponse getMemorialById(Long id) {
        Memorial memorial = memorialMapper.selectById(id);
        if (memorial == null) {
            throw new BizException(ResponseCode.NOT_FOUND);
        }
        return toResponse(memorial);
    }

    @Override
    public void deleteMemorial(Long id) {
        Memorial memorial = memorialMapper.selectById(id);
        if (memorial == null) {
            throw new BizException(ResponseCode.NOT_FOUND);
        }
        memorialMapper.deleteById(id);
        log.info("[后台纪念日管理] 删除纪念日: id={}, name={}", id, memorial.getName());
    }

    /**
     * 实体转后台纪念日响应
     */
    private AdminMemorialResponse toResponse(Memorial memorial) {
        AdminMemorialResponse response = new AdminMemorialResponse();
        response.setId(memorial.getId());
        response.setName(memorial.getName());
        response.setDate(memorial.getDate());
        response.setType(memorial.getType());
        response.setDescription(memorial.getDescription());
        response.setIsYearly(memorial.getIsYearly());
        response.setColor(memorial.getColor());
        response.setAvatar(memorial.getAvatar());
        response.setCreatedAt(memorial.getCreatedAt());
        return response;
    }
}
