import request from '@/utils/request'

/**
 * 后台用户管理接口（/api/admin/user）
 */

// 分页查询用户列表（关键字+状态过滤）
export function pageUsers(params) {
  return request.get('/admin/user', { params })
}

// 修改用户启用/禁用状态
export function updateUserStatus(id, status) {
  return request.put(`/admin/user/${id}/status`, { status })
}
