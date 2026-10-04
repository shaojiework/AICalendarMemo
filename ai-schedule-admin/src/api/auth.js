import request from '@/utils/request'

/**
 * 后台管理员认证接口
 */

// 管理员登录
export function adminLogin(data) {
  return request.post('/admin/login', data)
}

// 刷新令牌：换取新的访问令牌与刷新令牌
export function adminRefresh(refreshToken) {
  return request.post('/admin/refresh', { refreshToken })
}

// 退出登录：后端拉黑当前访问令牌并撤销刷新令牌
export function adminLogout(refreshToken) {
  return request.post('/admin/logout', { refreshToken })
}
