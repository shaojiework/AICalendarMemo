package com.aicalendar.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aicalendar.entity.Memorial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MemorialMapper extends BaseMapper<Memorial> {

    /** 按类型查询某用户的纪念日 */
    List<Memorial> selectByType(@Param("userId") Long userId, @Param("type") String type);

    /** 查询某用户非指定类型的纪念日 */
    List<Memorial> selectByTypeNot(@Param("userId") Long userId, @Param("type") String type);

    /** 查询某用户指定日期范围内的纪念日 */
    List<Memorial> selectUpcoming(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /** 按月份查询某用户的纪念日 */
    List<Memorial> selectByMonth(@Param("userId") Long userId, @Param("month") int month);
}