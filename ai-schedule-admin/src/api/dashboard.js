import request from '@/utils/request'

/**
 * 后台仪表盘接口
 */

// 获取统计数据（用户/日程/纪念日/AI会话总数）
export function getStats() {
  return request.get('/admin/dashboard/stats')
}

// 获取最近7天新增数据趋势
export function getTrend() {
  return request.get('/admin/dashboard/trend')
}

// 获取日程类型分布
export function getScheduleTypeDistribution() {
  return request.get('/admin/dashboard/schedule-type-distribution')
}
