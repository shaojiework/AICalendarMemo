import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearAuth, refreshAccessToken } from '@/utils/token'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动注入访问令牌
request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

/** 跳转登录页（并发请求同时失效时只跳一次） */
let redirecting = false
const gotoLogin = () => {
  clearAuth()
  if (redirecting) return
  redirecting = true
  ElMessage.error('登录已失效，请重新登录')
  setTimeout(() => {
    redirecting = false
    if (window.location.pathname !== '/login') {
      window.location.href = '/login'
    }
  }, 800)
}

/**
 * 刷新令牌并重放原请求
 * 只重试一次，重试仍401说明刷新令牌也已失效，跳登录页
 */
const retryAfterRefresh = async (config) => {
  const newToken = await refreshAccessToken()
  config.headers = config.headers || {}
  config.headers.Authorization = `Bearer ${newToken}`
  // 标记已重试，避免再次401时循环刷新
  config.__retryed = true
  return request(config)
}

// 响应拦截器：统一处理业务码、401刷新重试、错误提示
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 业务码 200 表示成功，直接返回 data 字段
    if (res.code === 200) {
      return res.data
    }
    // 其他业务码：提示错误信息
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  async (error) => {
    const { response, config } = error
    // 401：访问令牌过期或已登出，刷新令牌后重试一次
    if (response && response.status === 401 && config && !config.__retryed) {
      try {
        return await retryAfterRefresh(config)
      } catch (e) {
        // 刷新失败：刷新令牌也已失效，只能重新登录
        gotoLogin()
        return Promise.reject(e)
      }
    }
    // 重试过仍401：登录态彻底失效
    if (response && response.status === 401) {
      gotoLogin()
      return Promise.reject(error)
    }
    ElMessage.error(error.response?.data?.message || '网络异常，请稍后重试')
    return Promise.reject(error)
  }
)

export default request
