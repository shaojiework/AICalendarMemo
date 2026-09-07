import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动注入 token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理业务码、401 未登录、错误提示
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
  (error) => {
    // 401 未登录或 token 过期：清除本地信息并跳转登录页
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_user_info')
      ElMessage.error('登录已过期，请重新登录')
      // 避免重复跳转
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
      return Promise.reject(error)
    }
    ElMessage.error(error.response?.data?.message || '网络异常，请稍后重试')
    return Promise.reject(error)
  }
)

export default request
