<template>
  <view class="home-container">
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <template v-else>
      <!-- 顶部问候 -->
      <view class="header">
        <view class="greeting-block">
          <text class="greet-text">{{ greeting }}，{{ displayName }} ☀️</text>
          <text class="date-text">{{ currentDate }}</text>
        </view>
        <view class="small-btn" @click="navTo('/pages/profile/profile')">
          <text class="btn-text">{{ displayName?.charAt(0) || '用' }}</text>
        </view>
      </view>

      <!-- 纪念日卡片（带渐变） -->
      <view v-if="anniversary" class="anniversary-card" @click="navTo('/pages/memorial/memorial')">
        <view class="ann-left">
          <text class="ann-title">{{ anniversary.title }}</text>
          <text class="ann-desc">{{ anniversary.date }}</text>
        </view>
        <view class="ann-right">
          <text class="ann-count">{{ anniversary.daysTogether || anniversary.daysUntil }}</text>
          <text class="ann-unit">{{ anniversary.daysTogether ? '天' : '天后' }}</text>
        </view>
      </view>

      <!-- 功能入口 -->
      <view class="section-title">
        <text>功能入口</text>
      </view>
      <view class="func-grid">
        <view
          class="func-item"
          v-for="item in funcList"
          :key="item.id"
          @tap="navTo(item.path)"
        >
          <uni-icons :type="item.icon" :color="item.color" size="30"></uni-icons>
          <text class="item-name">{{ item.name }}</text>
        </view>
      </view>

      <!-- 今日日程 -->
      <view class="section-title schedule-title">
        <text>今日日程</text>
        <text class="view-all" @tap="navTo('/pages/schedule/schedule')">查看全部</text>
      </view>
      <view v-if="todaySchedule.length > 0" class="schedule-list">
        <view class="schedule-item" v-for="item in todaySchedule" :key="item.id">
          <view class="schedule-line" :style="{backgroundColor: item.lineColor}"></view>
          <view class="schedule-info">
            <text class="schedule-name">{{ item.name }}</text>
            <text class="schedule-time">{{ item.time }}</text>
          </view>
          <view class="schedule-tag" :style="{backgroundColor: item.tagBg, color: item.tagColor}">
            <text>{{ item.tag }}</text>
          </view>
        </view>
      </view>
      <view v-else class="empty-schedule">
        <text class="empty-icon">📅</text>
        <text class="empty-text">今日暂无日程</text>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { homeApi } from '@/api/home'
import { getUserInfo } from '@/utils/auth'

const loading = ref(false)
const greeting = ref('早上好')
const username = ref('用户')
const anniversary = ref(null)
const todaySchedule = ref([])

// 登录用户信息（响应式，tab切换时从本地缓存同步）
const userInfo = ref(getUserInfo())

// 显示名：昵称优先，其次用户名
const displayName = computed(() => {
  return userInfo.value?.nickname || userInfo.value?.username || username.value
})

const funcList = ref([
  { id: 1, name: '日程', icon: 'calendar', color: '#409EFF', path: '/pages/schedule/schedule' },
  { id: 2, name: '纪念', icon: 'heart', color: '#FF7B9C', path: '/pages/memorial/memorial' },
  { id: 3, name: 'AI', icon: 'chat', color: '#7940EC', path: '/pages/ai/ai' },
  { id: 4, name: '目标', icon: 'star', color: '#FF9C40', path: '/pages/profile/profile' },
  { id: 5, name: '笔记', icon: 'compose', color: '#36C9A5', path: '/pages/profile/profile' },
  { id: 6, name: '社区', icon: 'pyq', color: '#409EFF', path: '/pages/profile/profile' }
])

const currentDate = computed(() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  const day = now.getDate()
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const weekDay = weekDays[now.getDay()]
  return `${year}年${month}月${day}日 ${weekDay}`
})

// tab页每次切换都触发：同步登录用户信息 + 重新加载数据（如日程页新增后回到首页刷新）
onShow(() => {
  userInfo.value = getUserInfo()
  loadHomeData()
})

const loadHomeData = async () => {
  loading.value = true
  try {
    const res = await homeApi.getHomeData()
    if (res.code === 200 && res.data) {
      const data = res.data
      if (data.greeting) {
        greeting.value = data.greeting
      }
      if (data.anniversary) {
        anniversary.value = data.anniversary
      }
      if (data.todaySchedule) {
        todaySchedule.value = data.todaySchedule
      }
    }
  } catch (error) {
    console.error('加载首页数据失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

const navTo = (url) => {
  // 所有页面都是 tabbar 页面，使用 switchTab 跳转
  uni.switchTab({ url })
}
</script>

<style lang="scss" scoped>
.home-container {
  padding: $spacing-xl;
  padding-bottom: 160rpx;
  background-color: $bg-page;
  min-height: 100vh;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 600rpx;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #f3f3f3;
  border-top: 4rpx solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  margin-top: $spacing-md;
  font-size: $font-sm;
  color: $text-secondary;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-xl;
}
.greet-text {
  font-size: $font-xxxl;
  font-weight: $font-bold;
  color: $text-primary;
  display: block;
  margin-bottom: $spacing-xs;
}
.date-text {
  font-size: $font-lg;
  color: $text-secondary;
}
.small-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background-color: $color-primary;
  @include flex-center;
}
.btn-text {
  color: $text-white;
  font-size: $font-lg;
}

.anniversary-card {
  background: $gradient-blue-pink;
  border-radius: $radius-lg;
  padding: $spacing-xxl $spacing-xl;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-xxl;
  color: $text-white;
}
.ann-left {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}
.ann-title {
  font-size: $font-xxl;
  font-weight: $font-bold;
}
.ann-desc {
  font-size: $font-lg;
  opacity: 0.8;
}
.ann-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}
.ann-count {
  font-size: $font-display;
  font-weight: $font-bold;
  line-height: 1;
}
.ann-unit {
  font-size: $font-lg;
  opacity: 0.8;
}

.section-title {
  font-size: $font-xxl;
  font-weight: $font-bold;
  color: $text-primary;
  margin-bottom: $spacing-lg;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.view-all {
  font-size: $font-lg;
  color: $color-primary;
}
.func-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-lg;
  margin-bottom: $spacing-xxxl;
}
.func-item {
  background-color: $bg-white;
  border-radius: $radius-sm;
  padding: $spacing-xl $spacing-lg;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-md;
}
.item-name {
  font-size: $font-lg;
  color: #333;
}

.schedule-title {
  margin-bottom: $spacing-lg;
}
.schedule-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}
.schedule-item {
  background-color: $bg-white;
  border-radius: $radius-lg;
  padding: $spacing-xl;
  display: flex;
  align-items: center;
  gap: $spacing-lg;
}
.schedule-line {
  width: 8rpx;
  height: 80rpx;
  border-radius: $radius-xs;
}
.schedule-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}
.schedule-name {
  font-size: $font-xl;
  color: $text-primary;
  font-weight: $font-medium;
}
.schedule-time {
  font-size: $font-md;
  color: $text-placeholder;
}
.schedule-tag {
  padding: $spacing-xs $spacing-lg;
  border-radius: $radius-full;
  font-size: $font-sm;
}

.empty-schedule {
  background-color: $bg-white;
  border-radius: $radius-lg;
  padding: $spacing-xxl;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-md;
}
.empty-icon {
  font-size: 80rpx;
}
.empty-text {
  font-size: $font-lg;
  color: $text-placeholder;
}
</style>