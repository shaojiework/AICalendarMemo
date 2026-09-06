package com.aicalendar.mapper;

import com.aicalendar.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口（MyBatis-Plus通用CRUD）
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
