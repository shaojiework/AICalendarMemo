import { request } from '@/utils/request'

/**
 * 用户信息接口：当前登录用户的查询与更新
 */
export const userApi = {
  getInfo() {
    return request({
      url: '/user/info',
      method: 'GET'
    })
  },

  updateProfile(data) {
    return request({
      url: '/user/profile',
      method: 'PUT',
      data
    })
  }
}
