import { defineStore } from 'pinia'
import { ref } from 'vue'
import { adminLogin } from '@/api/auth'

/**
 * 认证状态管理：token、管理员信息
 */
export const useAuthStore = defineStore('auth', () => {
  // 从 localStorage 恢复初始值，避免刷新丢失
  const token = ref(localStorage.getItem('admin_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('admin_user_info') || 'null'))

  // 登录：调用接口，成功后持久化 token 和用户信息
  const login = async (loginForm) => {
    const data = await adminLogin(loginForm)
    token.value = data.token
    userInfo.value = data
    localStorage.setItem('admin_token', data.token)
    localStorage.setItem('admin_user_info', JSON.stringify(data))
    return data
  }

  // 退出登录：清除状态和本地存储
  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user_info')
  }

  return { token, userInfo, login, logout }
})
