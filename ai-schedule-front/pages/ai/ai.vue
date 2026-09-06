<template>
  <view class="ai-container">
    <!-- 顶部导航栏 -->
    <view class="header-section">
      <view class="header-left">
        <view class="avatar-circle">
          <uni-icons fontFamily="iconfont" size="16" color="#fff">{{'\uea3f'}}</uni-icons>
        </view>
        <view class="header-info">
          <text class="header-title">AI小橘助手</text>
          <view class="online-status">
            <view class="online-dot"></view>
            <text class="online-text">在线，随时为你解答</text>
          </view>
        </view>
      </view>
      <uni-icons type="more-filled" size="16" color="#6B6B8A"></uni-icons>
    </view>

    <!-- 聊天区域 -->
    <scroll-view scroll-y class="chat-scroll" :scroll-into-view="scrollToView">
      <view class="chat-content">
        <!-- 加载中 -->
        <view v-if="loading" class="loading-container">
          <uni-icons type="spinner-cycle" size="32" color="#409EFF"></uni-icons>
          <text class="loading-text">加载中...</text>
        </view>

        <!-- 消息列表 -->
        <view v-for="(msg, index) in messages" :key="index" :id="'msg-' + index" class="message-item" :class="msg.role">
          <view v-if="msg.role === 'assistant'" class="msg-avatar">
            <uni-icons fontFamily="iconfont" size="16" color="#fff">{{'\uea3f'}}</uni-icons>
          </view>
          <view class="msg-bubble" :class="msg.role === 'user' ? 'user-bubble' : 'ai-bubble'">
            <text class="msg-text">{{ formatMessage(msg.content) }}</text>
          </view>
          <view v-if="msg.role === 'user'" class="msg-avatar user-avatar">
            <text class="avatar-text">橘</text>
          </view>
        </view>

        <!-- AI正在输入 -->
        <view v-if="isTyping" class="message-item assistant">
          <view class="msg-avatar">
            <uni-icons fontFamily="iconfont" size="16" color="#fff">{{'\uea3f'}}</uni-icons>
          </view>
          <view class="msg-bubble ai-bubble">
            <view class="typing-indicator">
              <view class="typing-dot"></view>
              <view class="typing-dot"></view>
              <view class="typing-dot"></view>
            </view>
          </view>
        </view>

        <!-- 你可能需要 -->
        <view v-if="messages.length === 0" class="suggest-section">
          <text class="section-label">你可能需要</text>
          <view class="suggest-grid">
            <view class="suggest-item" @click="goTo('schedule')">
              <view class="suggest-icon blue">
                <uni-icons type="calendar" size="32" color="#409EFF"></uni-icons>
              </view>
              <text class="suggest-text">查看今日日程</text>
            </view>
            <view class="suggest-item" @click="goTo('memorial')">
              <view class="suggest-icon pink">
                <uni-icons type="heart" size="32" color="#FF95CC"></uni-icons>
              </view>
              <text class="suggest-text">纪念日提醒</text>
            </view>
            <view class="suggest-item" @click="goTo('ai')">
              <view class="suggest-icon yellow">
                <uni-icons fontFamily="iconfont" size="16" color="#F5A623">{{'\uea3f'}}</uni-icons>
              </view>
              <text class="suggest-text">AI智能建议</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部输入栏 -->
    <view class="input-bar">
      <view class="input-wrapper">
        <uni-icons type="mic" size="32" color="#C0C0D8"></uni-icons>
        <input 
          class="input-field" 
          placeholder="和 AI 说说你的想法..." 
          v-model="inputText"
          @confirm="sendMessage"
          :disabled="isTyping"
        />
      </view>
      <view class="send-btn" @click="sendMessage" :class="{ disabled: isTyping }">
        <uni-icons type="paperplane-filled" size="16" color="#fff"></uni-icons>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { aiApi } from '@/api/ai'

const inputText = ref('')
const messages = ref([])
const loading = ref(false)
const isTyping = ref(false)
const conversationId = ref('')
const scrollToView = ref('')

onMounted(() => {
  loadConversationHistory()
})

const loadConversationHistory = async () => {
  try {
    loading.value = true
    const history = await aiApi.getRecent(10)
    if (history && history.length > 0) {
      conversationId.value = history[0].conversationId
      // 反转数组，让最早的消息在前面
      messages.value = history.reverse().map(item => ({
        role: item.role,
        content: item.content
      }))
    } else {
      addAssistantMessage('你好！我是 AI 小橘助手 ✨\n\n我可以帮你安排日程、提醒纪念日、制定计划，甚至陪你聊聊生活。今天有什么想让我帮你的吗？')
    }
  } catch (error) {
    console.error('加载对话历史失败:', error)
    addAssistantMessage('你好！我是 AI 小橘助手 ✨\n\n我可以帮你安排日程、提醒纪念日、制定计划，甚至陪你聊聊生活。今天有什么想让我帮你的吗？')
  } finally {
    loading.value = false
  }
}

const sendMessage = () => {
  if (!inputText.value.trim() || isTyping.value) {
    return
  }

  const message = inputText.value.trim()
  inputText.value = ''

  addUserMessage(message)
  isTyping.value = true

  aiApi.chatWebSocket(
    conversationId.value,
    message,
    (chunk) => {
      // 收到流式片段：追加到最后一条AI消息，同时结束"正在输入"状态
      isTyping.value = false
      appendAssistantChunk(chunk)
    },
    (convId) => {
      // 流结束：标记消息完成并更新对话ID
      isTyping.value = false
      markLastComplete()
      if (convId) {
        conversationId.value = convId
      }
    },
    (error) => {
      isTyping.value = false
      console.error('WebSocket请求失败:', error)
      // AI不可用时在消息列表中展示降级提示
      addAssistantMessage(error.message || '请求失败，请稍后重试')
    }
  )
}

/**
 * 格式化消息文本：压缩3个以上连续换行为1个空行、去除首尾空白
 * 避免AI的Markdown分段在text组件中渲染出大片空白行
 */
const formatMessage = (content) => {
  if (!content) return ''
  return content.replace(/\n{3,}/g, '\n\n').trim()
}

/**
 * 将流式片段追加到最后一条AI消息（不存在则新建）
 */
const appendAssistantChunk = (chunk) => {
  const last = messages.value[messages.value.length - 1]
  if (last && last.role === 'assistant' && !last.isComplete) {
    last.content += chunk
  } else {
    messages.value.push({
      role: 'assistant',
      content: chunk,
      isComplete: false
    })
  }
  scrollToBottom()
}

/**
 * 标记最后一条AI消息渲染完成
 */
const markLastComplete = () => {
  const last = messages.value[messages.value.length - 1]
  if (last && last.role === 'assistant') {
    last.isComplete = true
  }
  scrollToBottom()
}

const addUserMessage = (content) => {
  messages.value.push({
    role: 'user',
    content: content
  })
  scrollToBottom()
}

const addAssistantMessage = (content) => {
  messages.value.push({
    role: 'assistant',
    content: content,
    isComplete: true
  })
  scrollToBottom()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messages.value.length > 0) {
      scrollToView.value = 'msg-' + (messages.value.length - 1)
    }
  })
}

const goTo = (page) => {
  uni.switchTab({
    url: `/pages/${page}/${page}`
  })
}
</script>

<style lang="scss" scoped>
.ai-container {
  min-height: 100vh;
  background-color: $bg-page;
  display: flex;
  flex-direction: column;
}

// 顶部导航
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 70rpx 32rpx 24rpx;
  background-color: $bg-white;
}

.header-left {
  display: flex;
  align-items: center;
  gap: $spacing-lg;
}

.avatar-circle {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: $gradient-blue-pink;
  @include flex-center;
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.header-title {
  font-size: $font-xl;
  font-weight: $font-semibold;
  color: $text-primary;
}

.online-status {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
}

.online-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background-color: #52C41A;
}

.online-text {
  font-size: $font-xs;
  color: $text-tertiary;
}

// 聊天区域
.chat-scroll {
  flex: 1;
  padding: $spacing-xl;
  width: 100%;
  box-sizing: border-box;
}

.chat-content {
  padding-bottom: 240rpx;
  width: 100%;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
  gap: $spacing-lg;
}

.loading-text {
  font-size: $font-sm;
  color: $text-tertiary;
}

.message-item {
  display: flex;
  margin-bottom: $spacing-xl;
  width: 100%;
  align-items: flex-start;
}

.message-item.assistant {
  flex-direction: row;
}

.message-item.user {
  justify-content: flex-end;
  
}

.msg-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: $gradient-blue-pink;
  @include flex-center;
  flex-shrink: 0;
}

.user-avatar {
  background: $gradient-purple;
  margin-left: $spacing-lg;
  flex-shrink: 0;
}

.avatar-text {
  font-size: $font-lg;
  font-weight: $font-semibold;
  color: $text-white;
}

.msg-bubble {
  max-width: 80%;
  padding: $spacing-xl;
  @include card-shadow;
  word-break: break-all;
  overflow-wrap: break-word;
}

.ai-bubble {
  background-color: $bg-white;
  border-radius: $radius-xs $radius-lg $radius-lg $radius-lg;
}

.user-bubble {
  background-color: $color-primary;
  border-radius: $radius-lg $radius-xs $radius-lg $radius-lg;
  flex-shrink: 0;
  max-width: calc(100% - 160rpx);
  margin-left: auto;
}

.msg-text {
  font-size: $font-lg;
  line-height: 1.6;
  word-break: break-all;
  overflow-wrap: break-word;
}

.ai-bubble .msg-text {
  color: $text-primary;
}

.user-bubble .msg-text {
  color: $text-white;
}

// 输入指示器
.typing-indicator {
  display: flex;
  gap: 8rpx;
  padding: $spacing-sm 0;
}

.typing-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background-color: #C0C0D8;
  animation: typing 1.4s infinite;
}

.typing-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-8rpx);
  }
}

// 你可能需要
.suggest-section {
  margin-top: $spacing-xl;
}

.section-label {
  display: block;
  font-size: $font-sm;
  color: $text-tertiary;
  margin-bottom: $spacing-lg;
}

.suggest-grid {
  display: flex;
  gap: $spacing-lg;
}

.suggest-item {
  flex: 1;
  background-color: $bg-white;
  border-radius: $radius-md;
  padding: $spacing-xl $spacing-lg;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
}

.suggest-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: $radius-sm;
  @include flex-center;
}

.suggest-icon.blue {
  background-color: $color-blue-bg;
}

.suggest-icon.pink {
  background-color: $color-pink-bg;
}

.suggest-icon.yellow {
  background-color: $color-yellow-bg;
}

.suggest-text {
  font-size: $font-xs;
  color: $text-secondary;
}

// 底部输入栏
.input-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: calc(100rpx + env(safe-area-inset-bottom));
  background-color: $bg-white;
  padding: $spacing-lg $spacing-xl;
  display: flex;
  align-items: center;
  gap: $spacing-lg;
  box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.04);
}

.input-wrapper {
  flex: 1;
  background-color: #f5f5f8;
  border-radius: $radius-full;
  padding: $spacing-lg $spacing-lg;
  display: flex;
  align-items: center;
  gap: $spacing-lg;
}

.input-field {
  flex: 1;
  font-size: $font-lg;
  color: $text-primary;
}

.send-btn {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: $gradient-blue-purple;
  @include flex-center;
}

.send-btn.disabled {
  opacity: 0.5;
}
</style>