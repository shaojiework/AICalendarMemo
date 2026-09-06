import { request } from '@/utils/request'

export const homeApi = {
  getHomeData() {
    return request({
      url: '/home',
      method: 'GET'
    })
  }
}