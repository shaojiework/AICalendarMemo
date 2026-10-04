import { request } from '@/utils/request'
import { baseUrl } from '@/config/baseUrl'
import { uploadWithAuth } from '@/utils/auth'

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

  /** 上传纪念日头像：令牌过期自动刷新重试，业务解析与原逻辑一致 */
  uploadAvatar(filePath) {
    return uploadWithAuth({
      url: baseUrl + '/api/file/upload',
      filePath: filePath,
      name: 'file',
      formData: { type: 'memorial' }
    }).then((res) => {
      if (res.statusCode === 200) {
        const data = JSON.parse(res.data)
        if (data.code === 200) {
          return data
        }
        uni.showToast({
          title: data.message || '上传失败',
          icon: 'none'
        })
        return Promise.reject(data)
      }
      uni.showToast({
        title: '上传失败',
        icon: 'none'
      })
      return Promise.reject(res)
    })
  }
}