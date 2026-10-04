import { request } from '@/utils/request'

/**
 * 认证接口：登录、注册（后端白名单匿名放行）
 */
export const authApi = {
  login(data) {
    return request({
      url: '/auth/login',
      method: 'POST',
      data
    })
  },

  register(data) {
    return request({
      url: '/auth/register',
      method: 'POST',
      data
    })
  },

  /** 刷新令牌：换取新的访问令牌与刷新令牌 */
  refresh(refreshToken) {
    return request({
      url: '/auth/refresh',
      method: 'POST',
      data: { refreshToken }
    })
  },

  /** 退出登录：后端拉黑当前访问令牌并撤销刷新令牌 */
  logout(refreshToken) {
    return request({
      url: '/auth/logout',
      method: 'POST',
      data: { refreshToken }
    })
  }
}
