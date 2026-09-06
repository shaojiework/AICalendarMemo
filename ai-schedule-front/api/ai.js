import { baseUrl } from '@/config/baseUrl'

/**
 * AI 接口封装
 */
export const aiApi = {
  /**
   * UniApp WebSocket 流式聊天
   * @param {string} conversationId 对话ID（可为空）
   * @param {string} message 用户消息
   * @param {Function} onMessage 收到流式片段回调(chunk)
   * @param {Function} onComplete 流结束回调(conversationId)
   * @param {Function} onError 错误回调
   */
  chatWebSocket(conversationId, message, onMessage, onComplete, onError) {
    // 使用配置的baseUrl，如果未定义则使用默认值
    const apiBaseUrl = baseUrl || 'http://localhost:8080'

    // 将 http/https 转换为 ws/wss
    let wsUrl = apiBaseUrl.replace(/^http/, 'ws') + '/ws/ai/chat'

    console.log('WebSocket URL:', wsUrl)
    console.log('发送消息:', { conversationId, message })
    
    // 关闭之前的连接
    if (aiApi.socketTask) {
      aiApi.socketTask.close()
    }
    
    // 创建 WebSocket 连接
    aiApi.socketTask = uni.connectSocket({
      url: wsUrl,
      success: () => {
        console.log('WebSocket连接创建成功')
      },
      fail: (err) => {
        console.error('WebSocket连接创建失败:', err)
        onError && onError(err)
      }
    })
    
    // 连接成功回调
    aiApi.socketTask.onOpen(() => {
      console.log('WebSocket连接已建立')
      // 发送消息
      const data = {
        conversationId: conversationId,
        message: message
      }
      aiApi.socketTask.send({
        data: JSON.stringify(data),
        success: () => {
          console.log('消息发送成功')
        },
        fail: (err) => {
          console.error('WebSocket发送失败:', err)
          onError && onError(err)
        }
      })
    })
    
    // 收到消息回调（流式协议：chunk片段 / end结束 / error错误）
    aiApi.socketTask.onMessage((res) => {
      try {
        const response = JSON.parse(res.data)

        if (response.type === 'chunk') {
          // 流式片段：交给页面追加渲染，不关闭连接
          onMessage && onMessage(response.content)
        } else if (response.type === 'end') {
          // 流结束：更新对话ID并关闭连接
          onComplete && onComplete(response.conversationId)
          aiApi.socketTask.close()
        } else if (response.type === 'error') {
          // 服务端错误：content为降级提示文案
          console.error('后端返回错误:', response.content)
          onError && onError(new Error(response.content || 'AI服务暂时不可用'))
          aiApi.socketTask.close()
        }
      } catch (e) {
        console.error('WebSocket消息解析失败:', e, '原始数据:', res.data)
        onError && onError(e)
        aiApi.socketTask.close()
      }
    })
    
    // 连接关闭回调
    aiApi.socketTask.onClose((res) => {
      console.log('WebSocket连接已关闭:', res)
      aiApi.socketTask = null
    })
    
    // 错误回调
    aiApi.socketTask.onError((err) => {
      console.error('WebSocket错误:', err)
      onError && onError(err)
      aiApi.socketTask = null
    })
    
    return aiApi.socketTask
  },

  /**
   * 普通聊天（非流式）
   */
  chat(conversationId, message) {
    const apiBaseUrl = baseUrl || 'http://localhost:8080'
    
    return new Promise((resolve, reject) => {
      uni.request({
        url: `${apiBaseUrl}/api/ai/chat`,
        method: 'POST',
        data: {
          conversationId: conversationId,
          message: message
        },
        success: (res) => {
          if (res.data.code === 200) {
            resolve(res.data.data)
          } else {
            reject(new Error(res.data.message || '请求失败'))
          }
        },
        fail: (err) => {
          reject(err)
        }
      })
    })
  },

  /**
   * 获取对话历史
   */
  getHistory(conversationId) {
    const apiBaseUrl = baseUrl || 'http://localhost:8080'
    
    return new Promise((resolve, reject) => {
      uni.request({
        url: `${apiBaseUrl}/api/ai/chat/history`,
        method: 'GET',
        data: {
          conversationId: conversationId
        },
        success: (res) => {
          if (res.data.code === 200) {
            resolve(res.data.data)
          } else {
            reject(new Error(res.data.message || '请求失败'))
          }
        },
        fail: (err) => {
          reject(err)
        }
      })
    })
  },

  /**
   * 获取最近聊天记录
   */
  getRecent(limit = 20) {
    const apiBaseUrl = baseUrl || 'http://localhost:8080'
    
    return new Promise((resolve, reject) => {
      uni.request({
        url: `${apiBaseUrl}/api/ai/chat/recent`,
        method: 'GET',
        data: {
          limit: limit
        },
        success: (res) => {
          if (res.data.code === 200) {
            resolve(res.data.data)
          } else {
            reject(new Error(res.data.message || '请求失败'))
          }
        },
        fail: (err) => {
          reject(err)
        }
      })
    })
  },

  /**
   * 删除对话
   */
  deleteConversation(conversationId) {
    const apiBaseUrl = baseUrl || 'http://localhost:8080'
    
    return new Promise((resolve, reject) => {
      uni.request({
        url: `${apiBaseUrl}/api/ai/chat`,
        method: 'DELETE',
        data: {
          conversationId: conversationId
        },
        success: (res) => {
          if (res.data.code === 200) {
            resolve()
          } else {
            reject(new Error(res.data.message || '请求失败'))
          }
        },
        fail: (err) => {
          reject(err)
        }
      })
    })
  }
}

// 保存 socketTask 引用
aiApi.socketTask = null