import { baseUrl } from '@/config/baseUrl'
import { getToken, refreshAccessToken, clearLoginState } from '@/utils/auth'

/** 跳转登录页（并发请求同时失效时只跳一次） */
let redirecting = false
const gotoLogin = () => {
  clearLoginState()
  if (redirecting) return
  redirecting = true
  uni.showToast({ title: '登录已失效，请重新登录', icon: 'none' })
  setTimeout(() => {
    redirecting = false
    uni.reLaunch({ url: '/pages/login/login' })
  }, 800)
}

/**
 * 统一请求：携带访问令牌，返回401时刷新令牌并重试一次
 * @param {object} options { url, method, data, header }
 */
export const request = (options) => {
  return new Promise((resolve, reject) => {
    uni.request({
      url: baseUrl + '/api' + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        // 携带访问令牌（未登录时为空，由后端拦截器返回401）
        'Authorization': getToken() ? 'Bearer ' + getToken() : '',
        ...options.header
      },
      success: (res) => {
        // 401：访问令牌过期或已被登出，刷新后重试一次
        if (res.statusCode === 401) {
          if (options.__retryed) {
            // 刷新过仍401，说明刷新令牌也失效，只能重新登录
            gotoLogin()
            reject(res)
            return
          }
          refreshAccessToken()
            .then(() => request({ ...options, __retryed: true }))
            .then(resolve)
            .catch(() => {
              gotoLogin()
              reject(res)
            })
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
