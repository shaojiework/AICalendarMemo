import request from '@/utils/request'

/**
 * 后台日程管理接口（/api/admin/schedule）
 */

// 分页查询日程列表（标题关键字+类型+日期过滤）
export function pageSchedules(params) {
  return request.get('/admin/schedule', { params })
}

// 查询日程详情
export function getScheduleById(id) {
  return request.get(`/admin/schedule/${id}`)
}

// 删除日程
export function deleteSchedule(id) {
  return request.delete(`/admin/schedule/${id}`)
}
