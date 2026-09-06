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
  }
}
