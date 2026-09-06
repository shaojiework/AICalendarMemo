<template>
  <view class="schedule-container">
    <!-- 顶部标题栏 -->
    <view class="header">
      <text class="header-title">日程</text>
      <view class="header-right">
        <uni-icons type="search" size="44rpx" color="#6B6B8A" @tap="openSearch" />
        <uni-icons fontFamily="iconfont" size="44rpx" color="#6B6B8A" @tap="openFilter">{{'\ue90a'}}</uni-icons>
      </view>
    </view>

    <!-- 周历栏 -->
    <view class="week-calendar">
      <view
        class="week-item"
        v-for="(item, index) in weekList"
        :key="index"
        :class="{ active: item.active, highlight: item.highlight }"
        @tap="selectDate(item)"
      >
        <text class="week-day">{{ item.day }}</text>
        <text class="week-date">{{ item.date }}</text>
      </view>
    </view>

    <!-- 日程列表区 -->
    <scroll-view class="schedule-scroll" scroll-y :scroll-with-animation="true">
      <view class="schedule-content">
        <!-- 加载状态 -->
        <view v-if="loading" class="loading-container">
          <uni-icons type="loading" size="48rpx" color="#409EFF" />
          <text class="loading-text">加载中...</text>
        </view>

        <!-- 空状态 -->
        <view v-else-if="scheduleList.length === 0" class="empty-container">
          <uni-icons type="calendar" size="96rpx" color="#A0A0B8" />
          <text class="empty-text">暂无日程</text>
          <text class="empty-desc">点击右下角按钮添加新日程</text>
        </view>

        <!-- 日程列表 -->
        <template v-else>
          <!-- 上午日程 -->
          <view class="time-block">
            <text class="time-title">上午</text>
            <view
              v-for="item in morningSchedules"
              :key="item.id"
              class="schedule-card"
              @tap="showDetail(item)"
              @longpress="showActions(item)"
            >
              <view class="card-top">
                <view class="title-box">
                  <view class="dot" :style="{ backgroundColor: item.color }"></view>
                  <text class="card-title">{{ item.title }}</text>
                </view>
                <view class="time-badge" :class="getBadgeClass(item.color)">
                  <text class="badge-text">{{ formatTime(item.startTime) }}</text>
                </view>
              </view>
              <text class="card-desc">{{ item.description || '暂无描述' }}</text>
              <view class="card-footer">
                <uni-icons type="clock" size="32rpx" color="#A0A0B8" />
                <text class="footer-text">{{ formatTime(item.startTime) }} - {{ formatTime(item.endTime) }}</text>
                <uni-icons type="location" size="32rpx" color="#A0A0B8" />
                <text class="footer-text">{{ item.location || '未设置' }}</text>
              </view>
            </view>
          </view>

          <!-- 下午日程 -->
          <view class="time-block">
            <text class="time-title">下午</text>
            <view
              v-for="item in afternoonSchedules"
              :key="item.id"
              class="schedule-card"
              @tap="showDetail(item)"
              @longpress="showActions(item)"
            >
              <view class="card-top">
                <view class="title-box">
                  <view class="dot" :style="{ backgroundColor: item.color }"></view>
                  <text class="card-title">{{ item.title }}</text>
                </view>
                <view class="time-badge" :class="getBadgeClass(item.color)">
                  <text class="badge-text">{{ formatTime(item.startTime) }}</text>
                </view>
              </view>
              <text class="card-desc">{{ item.description || '暂无描述' }}</text>
              <view class="card-footer">
                <uni-icons type="clock" size="32rpx" color="#A0A0B8" />
                <text class="footer-text">{{ formatTime(item.startTime) }} - {{ formatTime(item.endTime) }}</text>
                <uni-icons type="location" size="32rpx" color="#A0A0B8" />
                <text class="footer-text">{{ item.location || '未设置' }}</text>
              </view>
            </view>
          </view>
        </template>
      </view>
    </scroll-view>

    <!-- 右下角添加按钮 -->
    <view class="add-btn" @tap="openAddModal">+</view>

    <!-- 搜索弹窗 -->
    <uni-popup ref="searchPopup" type="center">
      <view class="popup-content">
        <view class="popup-header">
          <text class="popup-title">搜索日程</text>
          <uni-icons type="close" size="40rpx" color="#6B6B8A" @tap="closeSearch" />
        </view>
        <view class="popup-body">
          <uni-easyinput
            v-model="searchKeyword"
            placeholder="输入日程标题搜索"
            @confirm="handleSearch"
          />
        </view>
        <view class="popup-footer">
          <view class="btn-cancel" @tap="closeSearch">取消</view>
          <view class="btn-confirm" @tap="handleSearch">搜索</view>
        </view>
      </view>
    </uni-popup>

    <!-- 添加/编辑弹窗 -->
    <uni-popup ref="formPopup" type="center">
      <view class="popup-content large">
        <view class="popup-header">
          <text class="popup-title">{{ editSchedule ? '编辑日程' : '添加日程' }}</text>
          <uni-icons type="close" size="40rpx" color="#6B6B8A" @tap="closeFormModal" />
        </view>
        <view class="popup-body form-body">
          <view class="form-item">
            <text class="form-label">标题</text>
            <uni-easyinput
              v-model="formData.title"
              placeholder="请输入日程标题"
            />
          </view>
          <view class="form-item">
            <text class="form-label">描述</text>
            <uni-easyinput
              v-model="formData.description"
              placeholder="请输入日程描述"
              type="textarea"
              :maxlength="200"
            />
          </view>
          <view class="form-item">
            <text class="form-label">开始日期</text>
            <picker mode="date" :value="formData.startDate" @change="onStartDateChange">
              <view class="picker-value">{{ formData.startDate || '选择日期' }}</view>
            </picker>
          </view>
          <view class="form-item">
            <text class="form-label">开始时间</text>
            <picker mode="time" :value="formData.startTime" @change="onStartTimeChange">
              <view class="picker-value">{{ formData.startTime || '选择时间' }}</view>
            </picker>
          </view>
          <view class="form-item">
            <text class="form-label">结束时间</text>
            <picker mode="time" :value="formData.endTime" @change="onEndTimeChange">
              <view class="picker-value">{{ formData.endTime || '选择时间' }}</view>
            </picker>
          </view>
          <view class="form-item">
            <text class="form-label">地点</text>
            <uni-easyinput
              v-model="formData.location"
              placeholder="请输入地点"
            />
          </view>
          <view class="form-item">
            <text class="form-label">类型</text>
            <view class="type-options">
              <view
                v-for="item in typeOptions"
                :key="item.value"
                class="type-option"
                :class=" { active: formData.type === item.value } "
                @tap="formData.type = item.value">
                <view class="type-dot" :style="{ backgroundColor: item.color }"></view>
                <text>{{ item.label }}</text>
              </view>
            </view>
          </view>
        </view>
        <view class="popup-footer">
          <view class="btn-cancel" @tap="closeFormModal">取消</view>
          <view class="btn-confirm" @tap="submitForm">保存</view>
        </view>
      </view>
    </uni-popup>

    <!-- 详情弹窗 -->
    <uni-popup ref="detailPopup" type="center">
      <view class="popup-content medium" v-if="currentSchedule">
        <view class="popup-header">
          <text class="popup-title">日程详情</text>
          <uni-icons type="close" size="40rpx" color="#6B6B8A" @tap="closeDetail" />
        </view>
        <view class="popup-body">
          <view class="detail-item">
            <view class="detail-dot" :style="{ backgroundColor: currentSchedule.color }"></view>
            <text class="detail-title">{{ currentSchedule.title }}</text>
          </view>
          <view class="detail-info">
            <view class="info-row">
              <uni-icons type="clock" size="32rpx" color="#A0A0B8" />
              <text class="info-text">{{ formatDateTime(currentSchedule.startTime) }} - {{ formatDateTime(currentSchedule.endTime) }}</text>
            </view>
            <view class="info-row">
              <uni-icons type="location" size="32rpx" color="#A0A0B8" />
              <text class="info-text">{{ currentSchedule.location || '未设置' }}</text>
            </view>
            <view class="info-row">
              <uni-icons type="tag" size="32rpx" color="#A0A0B8" />
              <text class="info-text">{{ getTypeLabel(currentSchedule.type) }}</text>
            </view>
          </view>
          <view class="detail-desc">
            <text>{{ currentSchedule.description || '暂无描述' }}</text>
          </view>
        </view>
        <view class="popup-footer">
          <view class="btn-edit" @tap="editCurrentSchedule(currentSchedule)">编辑</view>
          <view class="btn-delete" @tap="deleteCurrentSchedule">删除</view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { scheduleApi } from '@/api/schedule'

const weekList = ref([])
const scheduleList = ref([])
const loading = ref(false)

const searchPopup = ref(null)
const formPopup = ref(null)
const detailPopup = ref(null)

const searchKeyword = ref('')
const editSchedule = ref(null)
const currentSchedule = ref(null)

const formData = ref({
  title: '',
  description: '',
  startDate: '',
  startTime: '',
  endTime: '',
  location: '',
  type: 'normal'
})

const typeOptions = [
  { label: '工作', value: 'work', color: '#409EFF' },
  { label: '个人', value: 'personal', color: '#FF7B9C' },
  { label: '会议', value: 'meeting', color: '#7940EC' },
  { label: '其他', value: 'normal', color: '#36C9A5' }
]

// 选中日期（yyyy-MM-dd格式），初始为今天——保证onShow先于initWeekList执行时也能正确拼URL
const formatToday = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
const selectedDate = ref(formatToday())

const morningSchedules = computed(() => {
  return scheduleList.value.filter(item => {
    // 上午块包含凌晨与上午（0点-12点）
    const hour = new Date(item.startTime).getHours()
    return hour < 12
  })
})

const afternoonSchedules = computed(() => {
  return scheduleList.value.filter(item => {
    const hour = new Date(item.startTime).getHours()
    return hour >= 12 || hour < 6
  })
})

const initWeekList = () => {
  const today = new Date()
  const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']
  const week = []
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(today)
    date.setDate(today.getDate() + i)
    
    const isToday = i === 0
    const hasSchedule = false
    
    week.push({
      day: days[date.getDay()],
      date: date.getDate(),
      fullDate: `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`,
      active: isToday,
      highlight: hasSchedule
    })
  }
  
  weekList.value = week
  selectedDate.value = week[0].fullDate
}

const loadSchedules = async (date = null) => {
  loading.value = true
  try {
    const targetDate = date || selectedDate.value
    const result = await scheduleApi.getByDate(targetDate)
    scheduleList.value = result.data || []
    
    weekList.value.forEach(item => {
      item.highlight = scheduleList.value.some(s => s.startTime.includes(item.fullDate))
    })
  } catch (error) {
    console.error('加载日程失败:', error)
  } finally {
    loading.value = false
  }
}

const selectDate = (item) => {
  weekList.value.forEach(w => w.active = false)
  item.active = true
  selectedDate.value = item.fullDate
  loadSchedules(item.fullDate)
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日 ${formatTime(dateStr)}`
}

const getBadgeClass = (color) => {
  if (color === '#FF7B9C' || color === '#FF95CC') return 'pink'
  return 'blue'
}

const getTypeLabel = (type) => {
  const option = typeOptions.find(t => t.value === type)
  return option ? option.label : '其他'
}

const onStartDateChange = (e) => {
  formData.value.startDate = e.detail.value
}

const onStartTimeChange = (e) => {
  formData.value.startTime = e.detail.value
}

const onEndTimeChange = (e) => {
  formData.value.endTime = e.detail.value
}

const openSearch = () => {
  if (searchPopup.value) {
    searchPopup.value.open()
  }
}

const closeSearch = () => {
  if (searchPopup.value) {
    searchPopup.value.close()
  }
}

const openAddModal = () => {
  editSchedule.value = null
  formData.value = {
    title: '',
    description: '',
    startDate: '',
    startTime: '',
    endTime: '',
    location: '',
    type: 'normal'
  }
  if (formPopup.value) {
    formPopup.value.open()
  }
}

const openFilter = () => {
  uni.showToast({ title: '筛选功能开发中', icon: 'none' })
}

const closeDetail = () => {
  if (detailPopup.value) {
    detailPopup.value.close()
  }
}

const closeFormModal = () => {
  if (formPopup.value) {
    formPopup.value.close()
  }
  editSchedule.value = null
  formData.value = {
    title: '',
    description: '',
    startDate: '',
    startTime: '',
    endTime: '',
    location: '',
    type: 'normal'
  }
}

const submitForm = async () => {
  if (!formData.value.title) {
    uni.showToast({ title: '请输入标题', icon: 'none' })
    return
  }
  if (!formData.value.startDate) {
    uni.showToast({ title: '请选择日期', icon: 'none' })
    return
  }
  if (!formData.value.startTime) {
    uni.showToast({ title: '请选择开始时间', icon: 'none' })
    return
  }
  if (!formData.value.endTime) {
    uni.showToast({ title: '请选择结束时间', icon: 'none' })
    return
  }

  const data = {
    title: formData.value.title,
    description: formData.value.description,
    startTime: `${formData.value.startDate}T${formData.value.startTime}:00`,
    endTime: `${formData.value.startDate}T${formData.value.endTime}:00`,
    location: formData.value.location,
    type: formData.value.type,
    color: typeOptions.find(t => t.value === formData.value.type)?.color || '#3B82F6'
  }

  loading.value = true
  try {
    if (editSchedule.value) {
      await scheduleApi.update(editSchedule.value.id, data)
      uni.showToast({ title: '更新成功', icon: 'success' })
    } else {
      await scheduleApi.create(data)
      uni.showToast({ title: '创建成功', icon: 'success' })
    }
    closeFormModal()
    loadSchedules(selectedDate.value)
  } catch (error) {
    uni.showToast({ title: editSchedule.value ? '更新失败' : '创建失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const showDetail = (item) => {
  currentSchedule.value = item
  if (detailPopup.value) {
    detailPopup.value.open()
  }
}

const showActions = (item) => {
  uni.showActionSheet({
    itemList: ['编辑', '删除'],
    success: (res) => {
      if (res.tapIndex === 0) {
        editCurrentSchedule(item)
      } else if (res.tapIndex === 1) {
        deleteCurrentSchedule(item)
      }
    }
  })
}



const editCurrentSchedule = (item = null) => {
  const schedule =  item||currentSchedule.value
  if (!schedule) return
  
  editSchedule.value = schedule
  if (detailPopup.value) {
    detailPopup.value.close()
  }
  
  const startTime = new Date(schedule.startTime)
  const endTime = new Date(schedule.endTime)
  const year = startTime.getFullYear()
  const month = String(startTime.getMonth() + 1).padStart(2, '0')
  const day = String(startTime.getDate()).padStart(2, '0')
  
  formData.value = {
    title: schedule.title || '',
    description: schedule.description || '',
    startDate: `${year}-${month}-${day}`,
    startTime: `${String(startTime.getHours()).padStart(2, '0')}:${String(startTime.getMinutes()).padStart(2, '0')}`,
    endTime: `${String(endTime.getHours()).padStart(2, '0')}:${String(endTime.getMinutes()).padStart(2, '0')}`,
    location: schedule.location || '',
    type: schedule.type || 'normal'
  }
  
  if (formPopup.value) {
    formPopup.value.open()
  }
}

const deleteCurrentSchedule = (item = null) => {
  const schedule = item || currentSchedule.value
  if (!schedule) return

  uni.showModal({
    title: '确认删除',
    content: `确定要删除「${schedule.title}」吗？`,
    success: async (res) => {
      if (res.confirm) {
        loading.value = true
        try {
          await scheduleApi.delete(schedule.id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          if (detailPopup.value) {
            detailPopup.value.close()
          }
          loadSchedules(selectedDate.value)
        } catch (error) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        } finally {
          loading.value = false
        }
      }
    }
  })
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    loadSchedules(selectedDate.value)
  } else {
    loading.value = true
    try {
      const result = await scheduleApi.search(searchKeyword.value)
      scheduleList.value = result.data || []
    } catch (error) {
      console.error('搜索失败:', error)
    } finally {
      loading.value = false
      if (searchPopup.value) {
        searchPopup.value.close()
      }
    }
  }
}

// 周列表只在首次进入时初始化
onMounted(() => {
  initWeekList()
})

// tab页每次切换都重新加载（如AI创建日程后回到本页能立即看到）
onShow(() => {
  loadSchedules()
})
</script>

<style lang="scss" scoped>
.schedule-container {
  min-height: 100vh;
  background-color: $bg-page;
  padding: $spacing-xl;
  padding-bottom: 160rpx;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-xl;
}
.header-title {
  font-size: 48rpx;
  font-weight: $font-bold;
  color: $text-primary;
}
.header-right {
  display: flex;
  gap: $spacing-xl;
}

.week-calendar {
  display: flex;
  justify-content: space-around;
  background-color: $bg-white;
  border-radius: $radius-lg;
  padding: $spacing-xl $spacing-lg;
  margin-bottom: $spacing-xl;
}
.week-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-md $spacing-sm;
  border-radius: $radius-md;
}
.week-day {
  font-size: $font-sm;
  color: $text-secondary;
}
.week-date {
  font-size: $font-lg;
  font-weight: $font-semibold;
  color: $text-secondary;
}
.week-item.active {
  background-color: $color-primary;
  .week-day, .week-date {
    color: $text-white;
  }
}
.week-item.highlight {
  background-color: $color-pink-bg;
  .week-day, .week-date {
    color: $color-pink-light;
  }
}

.schedule-scroll {
  height: calc(100vh - 320rpx);
}
.time-block {
  margin-bottom: $spacing-xl;
}
.time-title {
  display: block;
  font-size: $font-lg;
  font-weight: $font-semibold;
  color: $text-tertiary;
  margin-bottom: $spacing-md;
}
.schedule-card {
  background-color: $bg-white;
  border-radius: $radius-lg;
  padding: $spacing-xl;
  @include card-shadow-lg;
  margin-bottom: $spacing-md;
}
.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-md;
}
.title-box {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}
.dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
}
.card-title {
  font-size: $font-xl;
  font-weight: $font-semibold;
  color: $text-primary;
}
.time-badge {
  padding: $spacing-xs $spacing-lg;
  border-radius: $radius-full;
  font-size: $font-sm;
}
.time-badge.blue {
  background-color: $color-blue-bg;
  .badge-text { color: $color-primary; }
}
.time-badge.pink {
  background-color: $color-pink-bg;
  .badge-text { color: $color-pink-light; }
}
.badge-text {
  font-size: $font-sm;
  font-weight: $font-semibold;
}
.card-desc {
  font-size: $font-md;
  color: $text-secondary;
  line-height: 1.5;
  margin-bottom: $spacing-lg;
}
.card-footer {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
}
.footer-text {
  font-size: $font-sm;
  color: $text-tertiary;
  margin-right: $spacing-lg;
}

.add-btn {
  position: fixed;
  right: 40rpx;
  bottom: 160rpx;
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: linear-gradient(90deg, $color-primary, $color-secondary);
  @include flex-center;
  color: $text-white;
  font-size: 48rpx;
  @include card-shadow-lg;
}

.loading-container, .empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
  gap: $spacing-lg;
}
.loading-text, .empty-text {
  font-size: $font-lg;
  color: $text-tertiary;
}
.empty-desc {
  font-size: $font-md;
  color: $text-placeholder;
}

.popup-content {
  width: 600rpx;
  background-color: $bg-white;
  border-radius: $radius-lg;
  overflow: hidden;
}
.popup-content.medium {
  width: 650rpx;
}
.popup-content.large {
  width: 700rpx;
  max-height: 80vh;
}
.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-xl;
  border-bottom: 1rpx solid #f0f0f0;
}
.popup-title {
  font-size: $font-xxl;
  font-weight: $font-bold;
  color: $text-primary;
}
.popup-body {
  padding: $spacing-xl;
  max-height: 60vh;
  overflow-y: auto;
}
.form-body {
  padding: $spacing-lg;
}
.form-item {
  margin-bottom: $spacing-lg;
}
.form-label {
  display: block;
  font-size: $font-lg;
  color: $text-primary;
  margin-bottom: $spacing-sm;
}
.picker-value {
  padding: $spacing-md;
  background-color: $bg-page;
  border-radius: $radius-md;
  font-size: $font-lg;
  color: $text-secondary;
}
.type-options {
  display: flex;
  gap: $spacing-md;
}
.type-option {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-sm $spacing-lg;
  border-radius: $radius-full;
  background-color: $bg-page;
}
.type-option.active {
  background-color: $color-blue-bg;
}
.type-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
}
.popup-footer {
  display: flex;
  border-top: 1rpx solid #f0f0f0;
}
.btn-cancel, .btn-confirm, .btn-edit, .btn-delete {
  flex: 1;
  padding: $spacing-xl;
  text-align: center;
  font-size: $font-lg;
}
.btn-cancel {
  color: $text-secondary;
  border-right: 1rpx solid #f0f0f0;
}
.btn-confirm {
  color: $color-primary;
}
.btn-edit {
  color: $color-primary;
  border-right: 1rpx solid #f0f0f0;
}
.btn-delete {
  color: $color-red;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-xl;
}
.detail-dot {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
}
.detail-title {
  font-size: $font-xxl;
  font-weight: $font-bold;
  color: $text-primary;
}
.detail-info {
  margin-bottom: $spacing-xl;
}
.info-row {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
}
.info-text {
  font-size: $font-lg;
  color: $text-secondary;
}
.detail-desc {
  padding: $spacing-lg;
  background-color: $bg-page;
  border-radius: $radius-md;
  font-size: $font-lg;
  color: $text-secondary;
  line-height: 1.6;
}
</style>
