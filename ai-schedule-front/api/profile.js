import { request } from '@/utils/request'
import { baseUrl } from '@/config/baseUrl'

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

  uploadAvatar(filePath) {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: baseUrl + '/api/file/upload',
        filePath: filePath,
        name: 'file',
        formData: {
          type: 'profile'
        },
        success: (res) => {
          if (res.statusCode === 200) {
            const data = JSON.parse(res.data)
            if (data.code === 200) {
              resolve(data)
            } else {
              uni.showToast({
                title: data.message || '上传失败',
                icon: 'none'
              })
              reject(data)
            }
          } else {
            uni.showToast({
              title: '上传失败',
              icon: 'none'
            })
            reject(res)
          }
        },
        fail: (err) => {
          uni.showToast({
            title: '上传失败',
            icon: 'none'
          })
          reject(err)
        }
      })
    })
  }
}