package com.aicalendar.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aicalendar.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {

    List<Schedule> selectByDate(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    List<Schedule> selectByType(@Param("type") String type);

    List<Schedule> searchByKeyword(@Param("keyword") String keyword);

    List<Schedule> selectByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}