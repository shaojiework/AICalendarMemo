import request from '@/utils/request'

/**
 * 后台AI聊天记录管理接口（/api/admin/chat）
 */

// 分页查询会话列表（按对话ID分组聚合，支持按对话ID关键字模糊搜索）
export function pageConversations(params) {
  return request.get('/admin/chat/conversations', { params })
}

// 查询指定会话的全部消息（按时间升序）
export function getMessages(conversationId) {
  return request.get(`/admin/chat/${conversationId}/messages`)
}
