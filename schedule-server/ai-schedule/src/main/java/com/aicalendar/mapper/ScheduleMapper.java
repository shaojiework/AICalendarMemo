package com.aicalendar.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aicalendar.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {

    /** 按日期查询某用户的日程 */
    List<Schedule> selectByDate(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    /** 按类型查询某用户的日程 */
    List<Schedule> selectByType(@Param("userId") Long userId, @Param("type") String type);

    /** 按关键字搜索某用户的日程 */
    List<Schedule> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    /** 按时间范围查询某用户的日程 */
    List<Schedule> selectByTimeRange(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}