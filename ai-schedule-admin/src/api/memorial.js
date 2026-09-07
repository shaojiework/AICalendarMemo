import request from '@/utils/request'

/**
 * 后台纪念日管理接口（/api/admin/memorial）
 */

// 分页查询纪念日列表（名称关键字+类型过滤）
export function pageMemorials(params) {
  return request.get('/admin/memorial', { params })
}

// 查询纪念日详情
export function getMemorialById(id) {
  return request.get(`/admin/memorial/${id}`)
}

// 删除纪念日
export function deleteMemorial(id) {
  return request.delete(`/admin/memorial/${id}`)
}
