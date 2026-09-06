/**
 * 登录状态管理：token与用户信息的本地存取
 */
const TOKEN_KEY = 'token'
const USER_KEY = 'userInfo'

/** 获取token（未登录返回空字符串） */
export const getToken = () => uni.getStorageSync(TOKEN_KEY) || ''

/** 获取当前登录用户信息 */
export const getUserInfo = () => uni.getStorageSync(USER_KEY) || null

/** 更新本地用户信息（如修改头像/昵称后同步） */
export const setUserInfo = (userInfo) => {
  uni.setStorageSync(USER_KEY, userInfo)
}

/** 登录成功后保存token与用户信息 */
export const setLoginState = (token, userInfo) => {
  uni.setStorageSync(TOKEN_KEY, token)
  uni.setStorageSync(USER_KEY, userInfo)
}

/** 退出登录/登录过期时清除登录状态 */
export const clearLoginState = () => {
  uni.removeStorageSync(TOKEN_KEY)
  uni.removeStorageSync(USER_KEY)
}
