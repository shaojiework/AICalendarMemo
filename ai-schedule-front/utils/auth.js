import { baseUrl } from '@/config/baseUrl'

/**
 * 登录状态管理：访问令牌、刷新令牌与用户信息的本地存取
 * accessToken 随业务请求携带；refreshToken 只用于调用刷新接口换新令牌
 */
const TOKEN_KEY = 'token'
const REFRESH_KEY = 'refreshToken'
const USER_KEY = 'userInfo'

/** 获取访问令牌（未登录返回空字符串） */
export const getToken = () => uni.getStorageSync(TOKEN_KEY) || ''

/** 获取刷新令牌 */
export const getRefreshToken = () => uni.getStorageSync(REFRESH_KEY) || ''

/** 获取当前登录用户信息 */
export const getUserInfo = () => uni.getStorageSync(USER_KEY) || null

/** 更新本地用户信息（如修改头像/昵称后同步） */
export const setUserInfo = (userInfo) => {
  uni.setStorageSync(USER_KEY, userInfo)
}

/**
 * 保存访问令牌与刷新令牌
 * @param {string} token 访问令牌
 * @param {string} refreshToken 刷新令牌
 */
export const setTokens = (token, refreshToken) => {
  uni.setStorageSync(TOKEN_KEY, token)
  if (refreshToken) {
    uni.setStorageSync(REFRESH_KEY, refreshToken)
  }
}

/** 登录成功后保存令牌与用户信息 */
export const setLoginState = (token, refreshToken, userInfo) => {
  setTokens(token, refreshToken)
  uni.setStorageSync(USER_KEY, userInfo)
}

/** 退出登录/登录失效时清除本地登录态 */
export const clearLoginState = () => {
  uni.removeStorageSync(TOKEN_KEY)
  uni.removeStorageSync(REFRESH_KEY)
  uni.removeStorageSync(USER_KEY)
}

/** 正在进行中的刷新请求（同一时刻只发一次，其余请求复用结果） */
let refreshingPromise = null

/**
 * 用刷新令牌换取新的访问令牌与刷新令牌
 * 直接走 uni.request，不经 request.js，避免刷新失败时递归触发401处理
 * @return {Promise<string>} 新的访问令牌
 */
export const refreshAccessToken = () => {
  // 单飞：多个请求同时401时只刷新一次，防止重复刷新把新令牌作废
  if (refreshingPromise) return refreshingPromise

  refreshingPromise = new Promise((resolve, reject) => {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      reject(new Error('NO_REFRESH_TOKEN'))
      return
    }
    uni.request({
      url: baseUrl + '/api/auth/refresh',
      method: 'POST',
      header: { 'Content-Type': 'application/json' },
      data: { refreshToken },
      success: (res) => {
        if (res.statusCode === 200 && res.data && res.data.code === 200) {
          const data = res.data.data
          setTokens(data.token, data.refreshToken)
          resolve(data.token)
        } else {
          reject(new Error('REFRESH_FAILED'))
        }
      },
      fail: (err) => reject(err)
    })
  }).finally(() => {
    refreshingPromise = null
  })

  return refreshingPromise
}

/**
 * 带鉴权的文件上传：自动携带访问令牌，返回401时刷新令牌并重试一次
 * uni.uploadFile不走request.js，鉴权与续期在此单独处理
 * @param {object} options uni.uploadFile参数 + failToast 网络失败提示文案
 * @return {Promise<object>} 原始响应（statusCode与data），业务解析交由调用方
 */
export const uploadWithAuth = (options) => {
  const { failToast = '上传失败', ...uploadOptions } = options
  return new Promise((resolve, reject) => {
    const doUpload = (retried) => {
      uni.uploadFile({
        ...uploadOptions,
        header: { 'Authorization': 'Bearer ' + getToken() },
        success: (res) => {
          // 访问令牌过期：刷新成功后重试一次，重试仍401则原样返回交由上层处理
          if (res.statusCode === 401 && !retried) {
            refreshAccessToken()
              .then(() => doUpload(true))
              .catch(() => {
                clearLoginState()
                uni.showToast({ title: '登录已失效，请重新登录', icon: 'none' })
                reject(res)
              })
            return
          }
          resolve(res)
        },
        fail: (err) => {
          uni.showToast({ title: failToast, icon: 'none' })
          reject(err)
        }
      })
    }
    doUpload(false)
  })
}
