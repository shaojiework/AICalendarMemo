<script setup>
import { ref, reactive, onBeforeUnmount, nextTick } from 'vue'
import { useAuthStore } from '@/store/auth'

/**
 * 全局悬浮AI智能体组件
 * 复用前台 uniapp ai.js 的 WebSocket 协议逻辑（chunk/end/error 帧）
 * 用浏览器原生 WebSocket 重写（uni.connectSocket 在 web 项目不可用）
 * 切换路由不销毁组件，单例 WS；组件卸载时手动 close 并清监听
 * 默认折叠为 FAB，点击展开为对话窗口
 */

const authStore = useAuthStore()

// 对话窗口可见性（默认折叠态，点击 FAB 展开）
const visible = ref(false)

// 消息列表：{ role: 'user'|'assistant', content, loading }
const messages = reactive([
  {
    role: 'assistant',
    content: '您好，我是后台AI助手。可以帮您查询全部用户的日程和纪念日数据，请告诉我您要查询的内容。',
    loading: false
  }
])

const userInput = ref('')
const sending = ref(false)

// 当前会话ID（首次发送时由后端生成，end 帧回传后保存）
const conversationId = ref('')

// WebSocket 实例（单例，切换路由不重复创建）
let socket = null
// 重连控制：避免主动关闭时还触发重连
let manualClose = false

// ws 连接状态（用于显示连接指示器）
const wsStatus = ref('idle') // idle | connecting | open | closed

/**
 * 构造 WebSocket URL
 * 复用前台 ai.js 的协议：{origin}/ws/ai/chat?token=xxx
 * origin 走当前域名（开发环境经 vite 代理 ws，生产环境走部署域名）
 */
const buildWsUrl = () => {
  const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  const token = authStore.token || localStorage.getItem('admin_token') || ''
  return `${proto}//${host}/ws/ai/chat?token=${encodeURIComponent(token)}`
}

/**
 * 建立 WebSocket 连接（单例：已存在且为 OPEN 状态时复用）
 */
const ensureSocket = () => {
  if (socket && socket.readyState === WebSocket.OPEN) return
  if (socket) {
    manualClose = true
    try { socket.close() } catch (e) { /* 忽略 */ }
    socket = null
    manualClose = false
  }

  wsStatus.value = 'connecting'
  socket = new WebSocket(buildWsUrl())

  socket.onopen = () => {
    wsStatus.value = 'open'
  }

  socket.onmessage = (event) => {
    // 复用前台 ai.js 协议：解析 JSON 后按 type 分发
    try {
      const response = JSON.parse(event.data)
      if (response.type === 'chunk') {
        appendAssistantChunk(response.content)
      } else if (response.type === 'end') {
        if (response.conversationId) conversationId.value = response.conversationId
        markLastComplete()
        sending.value = false
      } else if (response.type === 'error') {
        appendAssistantChunk(response.content || 'AI服务暂时不可用')
        markLastComplete()
        sending.value = false
      }
    } catch (e) {
      console.error('[AiAgent] 消息解析失败:', e, '原始数据:', event.data)
      markLastComplete()
      sending.value = false
    }
  }

  socket.onerror = (err) => {
    console.error('[AiAgent] WebSocket错误:', err)
    wsStatus.value = 'closed'
    sending.value = false
    markLastComplete()
  }

  socket.onclose = () => {
    wsStatus.value = 'closed'
    if (!manualClose && socket) {
      socket = null
    }
  }
}

/**
 * 发送消息：复用前台 ai.js 的发送协议 { conversationId, message }
 */
const sendMessage = () => {
  const text = userInput.value.trim()
  if (!text || sending.value) return

  messages.push({ role: 'user', content: text, loading: false })
  messages.push({ role: 'assistant', content: '', loading: true })
  sending.value = true
  userInput.value = ''

  ensureSocket()

  const payload = JSON.stringify({
    conversationId: conversationId.value,
    message: text
  })

  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send(payload)
  } else if (socket && socket.readyState === WebSocket.CONNECTING) {
    socket.addEventListener('open', () => socket.send(payload), { once: true })
  } else {
    messages[messages.length - 1].content = '连接AI服务失败，请检查登录状态后重试。'
    messages[messages.length - 1].loading = false
    sending.value = false
  }
}

const appendAssistantChunk = (chunk) => {
  const last = messages[messages.length - 1]
  if (last && last.role === 'assistant') {
    // 跳过纯空白 chunk（如 AI 开头输出的 "\n\n\n"），避免流式过程中出现空行
    if (!chunk || chunk.trim().length === 0) return
    last.content += chunk
  }
}

const markLastComplete = () => {
  const last = messages[messages.length - 1]
  if (last && last.role === 'assistant') {
    last.loading = false
    // 流结束后清理：去首尾、压缩3+连续换行为2个（保留段落分隔）
    // 如果清理后只剩空白（说明 AI 实际没返回有效内容），显示兜底文案
    const cleaned = (last.content || '')
      .replace(/\n{3,}/g, '\n\n')
      .trim()
    last.content = cleaned || '抱歉，AI 暂时没生成有效内容，请稍后再试。'
  }
}

/**
 * 关闭悬浮窗（仅隐藏，不卸载组件，ws 不主动关闭以便复用）
 */
const handleClose = () => {
  visible.value = false
}

/**
 * 重新打开悬浮窗
 */
const handleOpen = () => {
  visible.value = true
  nextTick(scrollToBottom)
}

const scrollToBottom = () => {
  const box = document.querySelector('.ai-agent-body')
  if (box) box.scrollTop = box.scrollHeight
}

import { watch } from 'vue'
watch(() => messages.map(m => m.content).join(''), scrollToBottom)

onBeforeUnmount(() => {
  // 组件卸载必须手动 close WebSocket，避免连接泄漏
  manualClose = true
  if (socket) {
    try { socket.close() } catch (e) { /* 忽略 */ }
    socket = null
  }
})

const onKeyEnter = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}
</script>

<template>
  <!-- 折叠态：圆形悬浮按钮，整体可拖拽，拖拽后不会误触发展开 -->
  <div v-show="!visible" class="ai-fab" v-draggable @click="handleOpen" title="打开AI助手">
    <el-icon size="22"><ChatDotRound /></el-icon>
  </div>

  <!-- 展开态：悬浮窗面板，仅头部可拖拽 -->
  <div
    v-show="visible"
    class="ai-agent-panel"
    v-draggable="{ handleSelector: '.ai-agent-header' }"
  >
    <!-- 头部：作为拖拽手柄 -->
    <div class="ai-agent-header drag-handle">
      <div class="title">
        <el-icon size="16"><ChatDotRound /></el-icon>
        <span>AI智能助手</span>
        <span class="status-dot" :class="wsStatus" :title="`连接状态：${wsStatus}`"></span>
      </div>
      <div class="actions">
        <!-- mousedown.stop 阻止冒泡到 header，避免点击关闭时触发拖拽逻辑导致位置跳变 -->
        <el-icon size="16" class="btn-close" @mousedown.stop @click="handleClose"><Close /></el-icon>
      </div>
    </div>

    <!-- 消息区 -->
    <div class="ai-agent-body">
      <div
        v-for="(msg, idx) in messages"
        :key="idx"
        class="msg-item"
        :class="msg.role"
      >
        <div v-if="msg.role === 'user'" class="bubble user-bubble">{{ msg.content }}</div>
        <div v-else class="bubble assistant-bubble">
          <span v-if="msg.loading && !msg.content" class="typing">
            <span class="dot"></span><span class="dot"></span><span class="dot"></span>
          </span>
          <template v-else>
            <span class="content">{{ msg.content }}</span>
            <span v-if="msg.loading" class="cursor">|</span>
          </template>
        </div>
      </div>
    </div>

    <!-- 输入区 -->
    <div class="ai-agent-footer">
      <textarea
        v-model="userInput"
        class="input-box"
        placeholder="输入消息，回车发送（Shift+Enter换行）"
        rows="2"
        :disabled="sending"
        @keydown="onKeyEnter"
      ></textarea>
      <el-button
        type="primary"
        :loading="sending"
        :disabled="!userInput.trim()"
        @click="sendMessage"
      >发送</el-button>
    </div>
  </div>
</template>

<style scoped lang="scss">
/* 折叠态：圆形悬浮按钮 */
.ai-fab {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 9999;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #053f74, #4697d0);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.35);
  transition: transform 0.2s;
  &:hover {
    transform: scale(1.08);
  }
}

/* 展开态：面板容器，默认右侧垂直居中定位 */
.ai-agent-panel {
  position: fixed;
  z-index: 9999;
  /* 默认用 right + top:50% + transform 垂直居中于右侧；
     v-draggable 拖拽后会切换为 left/top 定位并清除 transform */
  right: 24px;
  top: 50%;
  transform: translateY(-50%);
  width: 360px;
  height: 480px;
  display: flex;
  flex-direction: column;
  background-color: $card-bg;
  border: 1px solid $navbar-border;
  border-radius: 10px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.45);
  overflow: hidden;
}

/* 头部：拖拽手柄 */
.ai-agent-header {
  height: 40px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  background: linear-gradient(135deg, #053f74, #0c2444);
  color: #dce8f7;
  user-select: none;

  .title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    font-weight: 600;
  }

  .status-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    margin-left: 4px;
    background: #6b83a0;
    &.open { background: #10b981; }
    &.connecting { background: #f59e0b; }
    &.closed, &.idle { background: #6b83a0; }
  }

  .actions {
    display: flex;
    align-items: center;
    .btn-close {
      cursor: pointer;
      transition: color 0.2s;
      &:hover { color: #ff7b9c; }
    }
  }
}

/* 消息区 */
.ai-agent-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background-color: $page-bg;

  &::-webkit-scrollbar { width: 6px; }
  &::-webkit-scrollbar-thumb { background: #2c4a6e; border-radius: 3px; }
}

.msg-item {
  display: flex;
  &.user { justify-content: flex-end; }
}

.bubble {
  max-width: 80%;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 13px;
  line-height: 1.55;
  word-break: break-all;
  white-space: pre-wrap;
}

.user-bubble {
  background-color: $sidebar-active-bg;
  color: #fff;
  border-top-right-radius: 2px;
}

.assistant-bubble {
  background-color: $overlay-bg;
  color: $text-primary;
  border-top-left-radius: 2px;
  .content { white-space: pre-wrap; }
  .cursor {
    margin-left: 2px;
    animation: blink 1s steps(2) infinite;
  }
}

/* 输入中三点动画 */
.typing {
  display: inline-flex;
  gap: 3px;
  align-items: center;
  .dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: $text-secondary;
    animation: typing 1.2s infinite;
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes typing {
  0%, 60%, 100% { opacity: 0.3; transform: translateY(0); }
  30% { opacity: 1; transform: translateY(-3px); }
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

/* 输入区 */
.ai-agent-footer {
  flex-shrink: 0;
  padding: 8px;
  display: flex;
  gap: 8px;
  align-items: center;
  background-color: $card-bg;
  border-top: 1px solid $navbar-border;

  .input-box {
    flex: 1;
    resize: none;
    border: 1px solid $navbar-border;
    border-radius: 6px;
    padding: 6px 10px;
    background-color: $page-bg;
    color: $text-primary;
    font-size: 13px;
    line-height: 1.5;
    font-family: inherit;
    outline: none;
    transition: border-color 0.2s;
    &:focus { border-color: $sidebar-active-bg; }
    &::placeholder { color: $text-secondary; }
  }
}
</style>
