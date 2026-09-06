import { request } from '@/utils/request'
import { baseUrl } from '@/config/baseUrl'

export const memorialApi = {
  getAll() {
    return request({
      url: '/memorial',
      method: 'GET'
    })
  },

  getById(id) {
    return request({
      url: `/memorial/${id}`,
      method: 'GET'
    })
  },

  getByType(type) {
    return request({
      url: `/memorial/type/${type}`,
      method: 'GET'
    })
  },

  getBirthdays() {
    return request({
      url: '/memorial/birthday',
      method: 'GET'
    })
  },

  create(data) {
    return request({
      url: '/memorial',
      method: 'POST',
      data
    })
  },

  update(id, data) {
    return request({
      url: `/memorial/${id}`,
      method: 'PUT',
      data
    })
  },

  delete(id) {
    return request({
      url: `/memorial/${id}`,
      method: 'DELETE'
    })
  },

  uploadAvatar(filePath) {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: baseUrl + '/api/file/upload',
        filePath: filePath,
        name: 'file',
        // 携带JWT令牌
        header: {
          'Authorization': 'Bearer ' + getToken()
        },
        formData: {
          type: 'memorial'
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