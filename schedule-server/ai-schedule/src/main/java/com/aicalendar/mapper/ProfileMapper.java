package com.aicalendar.mapper;

import com.aicalendar.entity.Profile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户信息Mapper接口
 */
@Mapper
public interface ProfileMapper {

    /**
     * 根据ID查询用户信息
     */
    Profile selectById(Long id);

    /**
     * 更新用户信息
     */
    int updateById(Profile profile);

    /**
     * 插入用户信息
     */
    int insert(Profile profile);

    /**
     * 查询记录数
     */
    int count();
}