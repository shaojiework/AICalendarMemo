import { baseUrl } from '@/config/baseUrl'
import { getToken, getRefreshToken, refreshAccessToken, clearLoginState } from '@/utils/auth'

// 接口基地址（与WebSocket保持一致的本地兜底）
const httpBase = baseUrl || 'http://localhost:8080'

// 带JWT令牌的请求头（ai模块直连uni.request，需手动携带token）
const authHeader = () => ({
  'Authorization': 'Bearer ' + getToken()
})

/**
 * AI 接口封装
 * 流式聊天走WebSocket：握手携带访问令牌，连接被拒时刷新令牌后重试一次
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
    const apiBaseUrl = baseUrl || 'http://localhost:8080'
    return this.openSocket(apiBaseUrl, conversationId, message, onMessage, onComplete, onError, false)
  },

  /**
   * 建立连接并流式对话
   * @param {boolean} retried 是否已因令牌过期刷新重试过，防止无限重连
   */
  openSocket(apiBaseUrl, conversationId, message, onMessage, onComplete, onError, retried) {
    // 将 http/https 转换为 ws/wss，握手URL携带访问令牌供后端鉴权
    const wsUrl = apiBaseUrl.replace(/^http/, 'ws') + '/ws/ai/chat?token=' + encodeURIComponent(getToken())

    console.log('发送消息:', { conversationId, message })

    // 关闭之前的连接
    if (aiApi.socketTask) {
      try { aiApi.socketTask.close() } catch (e) { /* 忽略 */ }
    }

    const socketTask = uni.connectSocket({
      url: wsUrl,
      success: () => {},
      fail: (err) => {
        console.error('WebSocket连接创建失败:', err)
        onError && onError(err)
      }
    })
    aiApi.socketTask = socketTask

    // 连接是否成功建立过：没建立就被关闭即握手被拒，可刷新令牌重试
    let opened = false
    // 本轮对话是否已终结：终结后的关闭属于正常收尾，不触发重试
    let finished = false

    // 连接成功回调
    socketTask.onOpen(() => {
      opened = true
      socketTask.send({
        data: JSON.stringify({ conversationId, message }),
        fail: (err) => {
          console.error('WebSocket发送失败:', err)
          finished = true
          onError && onError(err)
        }
      })
    })

    // 收到消息回调（流式协议：chunk片段 / end结束 / error错误）
    socketTask.onMessage((res) => {
      try {
        const response = JSON.parse(res.data)

        if (response.type === 'chunk') {
          // 流式片段：交给页面追加渲染，不关闭连接
          onMessage && onMessage(response.content)
        } else if (response.type === 'end') {
          // 流结束：更新对话ID并关闭连接
          finished = true
          onComplete && onComplete(response.conversationId)
          socketTask.close()
        } else if (response.type === 'error') {
          // 服务端错误：content为降级提示文案
          console.error('后端返回错误:', response.content)
          finished = true
          onError && onError(new Error(response.content || 'AI服务暂时不可用'))
          socketTask.close()
        }
      } catch (e) {
        console.error('WebSocket消息解析失败:', e, '原始数据:', res.data)
        finished = true
        onError && onError(e)
        socketTask.close()
      }
    })

    // 连接关闭回调
    socketTask.onClose((res) => {
      console.log('WebSocket连接已关闭:', res)
      if (aiApi.socketTask === socketTask) {
        aiApi.socketTask = null
      }
      // 握手被拒（访问令牌过期）：刷新令牌后重试一次
      if (!opened && !finished && !retried && getRefreshToken()) {
        refreshAccessToken()
          .then(() => aiApi.openSocket(apiBaseUrl, conversationId, message, onMessage, onComplete, onError, true))
          .catch(() => {
            // 刷新失败：登录态彻底失效，清除状态回登录页
            clearLoginState()
            finished = true
            onError && onError(new Error('登录已失效，请重新登录'))
          })
        return
      }
      if (!opened && !finished) {
        finished = true
        onError && onError(new Error('AI服务连接失败，请稍后重试'))
      }
    })

    // 错误回调
    socketTask.onError((err) => {
      console.error('WebSocket错误:', err)
      if (aiApi.socketTask === socketTask) {
        aiApi.socketTask = null
      }
    })

    return socketTask
  },

  /**
   * 获取对话历史
   */
  getHistory(conversationId) {
    return new Promise((resolve, reject) => {
      uni.request({
        url: `${httpBase}/api/ai/chat/history`,
        method: 'GET',
        header: authHeader(),
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
    return new Promise((resolve, reject) => {
      uni.request({
        url: `${httpBase}/api/ai/chat/recent`,
        method: 'GET',
        header: authHeader(),
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
    return new Promise((resolve, reject) => {
      uni.request({
        url: `${httpBase}/api/ai/chat`,
        method: 'DELETE',
        header: authHeader(),
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
