import { defineStore } from 'pinia'
import { ref } from 'vue'
import { adminLogin, adminLogout } from '@/api/auth'
import { getToken, setTokens, clearAuth, getRefreshToken } from '@/utils/token'

/**
 * 认证状态管理：访问令牌、刷新令牌与管理员信息
 * 令牌读写统一走 utils/token，保证axios拦截器与本store看到同一份数据
 */
export const useAuthStore = defineStore('auth', () => {
  // 从 localStorage 恢复初始值，避免刷新页面丢失登录态
  const token = ref(getToken())
  const userInfo = ref(JSON.parse(localStorage.getItem('admin_user_info') || 'null'))

  // 登录：调用接口，成功后持久化令牌对和用户信息
  const login = async (loginForm) => {
    const data = await adminLogin(loginForm)
    token.value = data.token
    userInfo.value = data
    setTokens(data.token, data.refreshToken)
    localStorage.setItem('admin_user_info', JSON.stringify(data))
    return data
  }

  // 退出登录：先清本地状态，再通知后端撤销令牌
  const logout = () => {
    // 本地态同步清除，保证随后的路由跳转立即判定为未登录
    token.value = ''
    userInfo.value = null
    const refreshToken = getRefreshToken()
    clearAuth()
    if (refreshToken) {
      // 登出接口失败不阻断退出，令牌到期后自然失效
      adminLogout(refreshToken).catch((e) => console.warn('退出登录接口调用失败:', e))
    }
  }

  return { token, userInfo, login, logout }
})
