import { request } from '@/utils/request'
import { baseUrl } from '@/config/baseUrl'
import { uploadWithAuth } from '@/utils/auth'

export const profileApi = {
  getProfile() {
    return request({
      url: '/profile',
      method: 'GET'
    })
  },

  updateProfile(data) {
    return request({
      url: '/profile',
      method: 'PUT',
      data
    })
  },

  /** 上传头像：令牌过期自动刷新重试，业务解析与原逻辑一致 */
  uploadAvatar(filePath) {
    return uploadWithAuth({
      url: baseUrl + '/api/file/upload',
      filePath: filePath,
      name: 'file',
      formData: { type: 'profile' }
    }).then((res) => {
      if (res.statusCode === 200) {
        const data = JSON.parse(res.data)
        if (data.code === 200) {
          return data
        }
        uni.showToast({ title: data.message || '上传失败', icon: 'none' })
        return Promise.reject(data)
      }
      uni.showToast({ title: '上传失败', icon: 'none' })
      return Promise.reject(res)
    })
  }
}
