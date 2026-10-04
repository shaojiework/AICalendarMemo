import axios from 'axios'

/**
 * 后台管理端令牌管理（双token模式）
 * accessToken 随业务请求携带；refreshToken 只用于换取新的一对令牌
 */
const TOKEN_KEY = 'admin_token'
const REFRESH_KEY = 'admin_refresh_token'
const USER_KEY = 'admin_user_info'

/** 获取访问令牌 */
export const getToken = () => localStorage.getItem(TOKEN_KEY) || ''

/** 获取刷新令牌 */
export const getRefreshToken = () => localStorage.getItem(REFRESH_KEY) || ''

/**
 * 保存令牌对
 * @param {string} token 访问令牌
 * @param {string} refreshToken 刷新令牌
 */
export const setTokens = (token, refreshToken) => {
  localStorage.setItem(TOKEN_KEY, token)
  if (refreshToken) {
    localStorage.setItem(REFRESH_KEY, refreshToken)
  }
}

/** 清除全部登录态 */
export const clearAuth = () => {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_KEY)
  localStorage.removeItem(USER_KEY)
}

/** 进行中的刷新请求（同一时刻只发一次，其余复用结果） */
let refreshingPromise = null

/**
 * 用刷新令牌换取新的访问令牌与刷新令牌
 * 用裸axios发起，不经request.js，避免刷新失败时递归触发401拦截
 * @return {Promise<string>} 新的访问令牌
 */
export const refreshAccessToken = () => {
  if (refreshingPromise) return refreshingPromise

  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    return Promise.reject(new Error('NO_REFRESH_TOKEN'))
  }

  refreshingPromise = axios
    .post('/api/admin/refresh', { refreshToken })
    .then((res) => {
      const body = res.data
      if (body.code !== 200) {
        throw new Error(body.message || '刷新失败')
      }
      setTokens(body.data.token, body.data.refreshToken)
      return body.data.token
    })
    .finally(() => {
      refreshingPromise = null
    })

  return refreshingPromise
}
