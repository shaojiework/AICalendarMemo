import { baseUrl } from '@/config/baseUrl'
import { getToken, clearLoginState } from '@/utils/auth'

export const request = (options) => {
  return new Promise((resolve, reject) => {
    uni.request({
      url: baseUrl + '/api' + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        // 携带JWT令牌（未登录时为空，由后端拦截器返回401）
        'Authorization': getToken() ? 'Bearer ' + getToken() : '',
        ...options.header
      },
      success: (res) => {
        // 401未授权：清除登录状态并跳转登录页
        if (res.statusCode === 401) {
          clearLoginState()
          uni.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none'
          })
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/login/login' })
          }, 800)
          reject(res)
          return
        }
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data)
          } else {
            uni.showToast({
              title: res.data.message || '请求失败',
              icon: 'none'
            })
            reject(res.data)
          }
        } else {
          uni.showToast({
            title: res.data.message || '网络请求失败',
            icon: 'none'
          })
          reject(res)
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '网络连接失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}
