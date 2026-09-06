package com.aicalendar.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aicalendar.entity.Memorial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MemorialMapper extends BaseMapper<Memorial> {

    List<Memorial> selectByType(@Param("type") String type);

    List<Memorial> selectByTypeNot(@Param("type") String type);

    List<Memorial> selectUpcoming(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<Memorial> selectByMonth(@Param("month") int month);
}