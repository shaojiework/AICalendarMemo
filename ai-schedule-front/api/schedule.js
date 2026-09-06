import { request } from '@/utils/request'

export const scheduleApi = {
  create: (data) => request({
    url: '/schedule',
    method: 'POST',
    data
  }),
  
  getById: (id) => request({
    url: `/schedule/${id}`,
    method: 'GET'
  }),
  
  getByDate: (date) => request({
    url: `/schedule/date/${date}`,
    method: 'GET'
  }),
  
  getByType: (type) => request({
    url: `/schedule/type/${type}`,
    method: 'GET'
  }),
  
  search: (keyword) => request({
    url: `/schedule/search?keyword=${encodeURIComponent(keyword)}`,
    method: 'GET'
  }),
  
  getAll: () => request({
    url: '/schedule',
    method: 'GET'
  }),
  
  update: (id, data) => request({
    url: `/schedule/${id}`,
    method: 'PUT',
    data
  }),
  
  delete: (id) => request({
    url: `/schedule/${id}`,
    method: 'DELETE'
  })
}