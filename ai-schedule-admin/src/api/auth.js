import request from '@/utils/request'

/**
 * 后台管理员认证接口
 */

// 管理员登录
export function adminLogin(data) {
  return request.post('/admin/login', data)
}
