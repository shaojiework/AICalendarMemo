<template>
  <view class="memorial-container">
    <view class="header-section">
      <uni-icons type="heart-filled" size="60" color="#fff"></uni-icons>
      <text class="header-title">纪念日</text>
      <text class="header-desc">记录每一个美好时刻</text>
    </view>

    <scroll-view scroll-y class="scroll-box" >
      <view class="page-content">
        <view class="section-title">详情</view>

        <view v-if="loading" class="loading-box">
          <uni-load-more status="loading"></uni-load-more>
        </view>

        <view v-else-if="upcomingMemorials.length === 0" class="empty-box">
          <uni-icons type="calendar" size="80" color="#ccc"></uni-icons>
          <text class="empty-text">暂无纪念日</text>
        </view>

        <view v-else>
          <view 
            v-for="item in upcomingMemorials" 
            :key="item.id" 
            class="gradient-card"
            :style="{ background: getCardBackground(item.color, item.type) }"
            @tap="showActionSheet(item)"
          >
            <view class="card-flex">
              <view class="card-left">
                <view class="card-tag">{{ getTypeName(item.type) }}</view>
                <view class="card-name">{{ item.name }}</view>
                <view class="card-date">{{ formatDate(item.date) }}</view>
              </view>
              <view class="card-right">
                <view v-if="item.daysTogether" class="num">{{ item.daysTogether }}</view>
                <view v-else class="num">{{ item.daysUntil }}</view>
                <view class="unit">{{ getCardUnit(item) }}</view>
              </view>
            </view>
          </view>
        </view>

        <view class="section-title">生日提醒</view>

        <view v-if="loading" class="loading-box">
          <uni-load-more status="loading"></uni-load-more>
        </view>

        <view v-else-if="birthdays.length === 0" class="empty-box">
          <uni-icons type="calendar" size="80" color="#ccc"></uni-icons>
          <text class="empty-text">暂无生日记录</text>
        </view>

        <view v-else class="birthday-box">
          <view 
            v-for="item in birthdays" 
            :key="item.id" 
            class="birthday-card"
            @tap="showActionSheet(item)"
          >
            <view class="avatar" :class="{ 'has-image': item.avatar }">
              <image v-if="item.avatar" :src="getAvatarUrl(item.avatar)" mode="aspectFill"></image>
              <uni-icons v-else type="person" size="50" color="#fff"></uni-icons>
            </view>
            <view class="b-name">{{ item.name }}</view>
            <view class="b-date">{{ formatMonthDay(item.date) }}</view>
            <view class="birthday-tag">
              {{ item.daysTogether ? `已过${item.daysTogether}天` : `${item.daysUntil}天后` }}
            </view>
          </view>
        </view>

        <view class="add-button" @click="showAddModal">
          <uni-icons type="plus-filled" color="#fff" size="28"></uni-icons>
          <text>添加纪念日</text>
        </view>
      </view>
    </scroll-view>

    <uni-popup ref="addPopup" type="center">
      <view class="popup-content">
        <view class="popup-title">{{ isEdit ? '编辑纪念日' : '添加纪念日' }}</view>
        
        <view class="form-item">
          <text class="form-label">名称</text>
          <input 
            v-model="formData.name" 
            class="form-input" 
            placeholder="请输入名称"
          />
        </view>

        <view class="form-item">
          <text class="form-label">类型</text>
          <picker 
            mode="selector" 
            :range="typeOptions" 
            :value="typeIndex"
            @change="onTypeChange"
          >
            <view class="form-input picker-input">
              {{ typeOptions[typeIndex] }}
              <uni-icons type="right" size="16" color="#999"></uni-icons>
            </view>
          </picker>
        </view>

		<view class="form-item">
		  <text class="form-label">头像</text>
		  <view class="avatar-upload" @click="chooseImage">
		    <image v-if="formData.avatar" :src="getAvatarUrl(formData.avatar)" mode="aspectFill" class="avatar-preview"></image>
		    <view v-else class="avatar-placeholder">
		      <uni-icons type="plus" size="30" color="#999"></uni-icons>
		      <text>上传头像</text>
		    </view>
		    <view v-if="formData.avatar" class="avatar-remove" @click.stop="removeAvatar">
		      <uni-icons type="closeempty" size="20" color="#999"></uni-icons>
		    </view>
		  </view>
		</view>

        <view class="form-item">
          <text class="form-label">日期</text>
          <picker 
            mode="date" 
            :value="formData.date" 
            @change="onDateChange"
          >
            <view class="form-input picker-input">
              {{ formData.date || '请选择日期' }}
              <uni-icons type="right" size="16" color="#999"></uni-icons>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="form-label">描述</text>
          <textarea 
            v-model="formData.description" 
            class="form-textarea" 
            placeholder="请输入描述（可选）"
            :maxlength="200"
          />
        </view>

        <view class="form-item">
          <text class="form-label">每年提醒</text>
          <switch 
            :checked="formData.isYearly === 1" 
            @change="onYearlyChange"
            color="blue"
          />
        </view>

        <view class="form-item">
          <text class="form-label">颜色</text>
          <view class="color-picker">
            <view 
              v-for="color in colorOptions" 
              :key="color" 
              class="color-item"
              :class="{ active: formData.color === color }"
              :style="{ backgroundColor: color }"
              @click="formData.color = color"
            ></view>
          </view>
        </view>

        

        <view class="popup-buttons">
          <view class="popup-btn cancel" @click="closePopup">取消</view>
          <view class="popup-btn confirm" @click="submitForm">确定</view>
        </view>
      </view>
    </uni-popup>

    <uni-popup ref="detailPopup" type="center">
      <view class="popup-content">
        <view class="popup-title">纪念日详情</view>
        
        <view class="detail-item">
          <text class="detail-label">名称</text>
          <text class="detail-value">{{ currentItem?.name }}</text>
        </view>

        <view class="detail-item">
          <text class="detail-label">类型</text>
          <text class="detail-value">{{ getTypeName(currentItem?.type) }}</text>
        </view>

        <view class="detail-item">
          <text class="detail-label">日期</text>
          <text class="detail-value">{{ formatDate(currentItem?.date) }}</text>
        </view>

        <view class="detail-item">
          <text class="detail-label">描述</text>
          <text class="detail-value">{{ currentItem?.description || '无' }}</text>
        </view>

        <view class="detail-item">
          <text class="detail-label">每年提醒</text>
          <text class="detail-value">{{ currentItem?.isYearly === 1 ? '是' : '否' }}</text>
        </view>

        <view class="detail-item">
          <text class="detail-label">天数</text>
          <text class="detail-value">
            {{ getDaysText(currentItem) }}
          </text>
        </view>

        <view class="popup-buttons">
          <view class="popup-btn cancel" @click="closeDetailPopup">关闭</view>
          <view class="popup-btn edit" @click="editMemorial">编辑</view>
          <view class="popup-btn delete" @click="deleteMemorial">删除</view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { memorialApi } from '@/api/memorial'
import { ref, onMounted } from 'vue'
import { baseUrl } from '@/config/baseUrl'

const getAvatarUrl = (avatar) => {
  if (!avatar) return null
  return baseUrl + avatar
}

const loading = ref(true)
const upcomingMemorials = ref([])
const birthdays = ref([])
const addPopup = ref(null)
const detailPopup = ref(null)
const isEdit = ref(false)
const currentItem = ref(null)

const formData = ref({
  name: '',
  type: 'normal',
  date: '',
  description: '',
  isYearly: 1,
  color: '#FF7B9C',
  avatar: ''
})

const typeOptions = ['普通', '恋爱', '结婚', '生日', '其他']
const typeValues = ['normal', 'love', 'marriage', 'birthday', 'other']
const typeIndex = ref(0)

const colorOptions = ['#FF7B9C', '#3B82F6', '#10B981', '#F59E0B', '#8B5CF6', '#EC4899']

/**
 * 加载纪念日数据
 * 同时获取即将到来的纪念日和生日列表
 */
const loadMemorials = async () => {
  loading.value = true
  try {
    const [upcomingRes, birthdayRes] = await Promise.all([
      memorialApi.getAll(),
      memorialApi.getBirthdays()
    ])
    upcomingMemorials.value = upcomingRes.data || []
    birthdays.value = birthdayRes.data || []
  } catch (error) {
    console.error('加载纪念日失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 打开新增弹窗
 * 重置表单数据为默认值
 */
const showAddModal = () => {
  isEdit.value = false
  formData.value = {
    name: '',
    type: 'normal',
    date: '',
    description: '',
    isYearly: 1,
    color: '#FF7B9C',
    avatar: ''
  }
  typeIndex.value = 0
  addPopup.value?.open()
}

const closePopup = () => {
  addPopup.value?.close()
}

/**
 * 点击卡片打开详情弹窗
 * @param {Object} item - 当前操作的纪念日对象
 */
const showActionSheet = (item) => {
  currentItem.value = item
  detailPopup.value?.open()
}

const closeDetailPopup = () => {
  detailPopup.value?.close()
}

/**
 * 编辑纪念日
 * 填充当前纪念日数据到表单
 */
const editMemorial = () => {
  isEdit.value = true
  formData.value = {
    name: currentItem.value.name,
    type: currentItem.value.type,
    date: currentItem.value.date,
    description: currentItem.value.description || '',
    isYearly: currentItem.value.isYearly,
    color: currentItem.value.color,
    avatar: currentItem.value.avatar || ''
  }
  typeIndex.value = typeValues.indexOf(currentItem.value.type)
  closeDetailPopup()
  addPopup.value?.open()
}

/**
 * 删除纪念日
 * 显示确认对话框，删除后刷新列表
 */
const deleteMemorial = () => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这个纪念日吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await memorialApi.delete(currentItem.value.id)
          uni.showToast({
            title: '删除成功',
            icon: 'success'
          })
          closeDetailPopup()
          loadMemorials()
        } catch (error) {
          console.error('删除失败:', error)
        }
      }
    }
  })
}

/**
 * 提交表单
 * 根据isEdit状态判断是新增还是更新
 */
const submitForm = async () => {
  if (!formData.value.name) {
    uni.showToast({
      title: '请输入名称',
      icon: 'none'
    })
    return
  }

  if (!formData.value.date) {
    uni.showToast({
      title: '请选择日期',
      icon: 'none'
    })
    return
  }

  try {
    if (isEdit.value) {
      await memorialApi.update(currentItem.value.id, formData.value)
      uni.showToast({
        title: '更新成功',
        icon: 'success'
      })
    } else {
      await memorialApi.create(formData.value)
      uni.showToast({
        title: '创建成功',
        icon: 'success'
      })
    }
    closePopup()
    loadMemorials()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

const onTypeChange = (e) => {
  typeIndex.value = e.detail.value
  formData.value.type = typeValues[e.detail.value]
}

const onDateChange = (e) => {
  formData.value.date = e.detail.value
}

const onYearlyChange = (e) => {
  formData.value.isYearly = e.detail.value ? 1 : 0
}

const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const tempFilePath = res.tempFilePaths[0]
      try {
        const uploadRes = await memorialApi.uploadAvatar(tempFilePath)
        formData.value.avatar = uploadRes.data
        uni.showToast({
          title: '上传成功',
          icon: 'success'
        })
      } catch (error) {
        console.error('上传失败:', error)
      }
    }
  })
}

const removeAvatar = () => {
  formData.value.avatar = ''
}

/**
 * 格式化日期为 Y年M月D日 格式
 */
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

/**
 * 格式化日期为 M月D日 格式
 */
const formatMonthDay = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

/**
 * 获取类型中文名称
 */
const getTypeName = (type) => {
  const map = {
    'normal': '普通',
    'love': '恋爱纪念日',
    'marriage': '结婚纪念日',
    'birthday': '生日',
    'other': '其他'
  }
  return map[type] || '普通'
}

/**
 * 根据类型与日期状态生成天数描述文案
 * 未到日期统一显示"还有 X 天"；已过日期：恋爱/结婚显示"已在一起"，其它类型显示"已度过"
 */
const getDaysText = (item) => {
  if (!item) return ''
  if (item.daysUntil !== null && item.daysUntil !== undefined) {
    return `还有 ${item.daysUntil} 天`
  }
  if ((item.type === 'love' || item.type === 'marriage') && item.daysTogether) {
    return `已在一起 ${item.daysTogether} 天`
  }
  return `已度过 ${item.daysTogether} 天`
}

/**
 * 卡片天数单位文案：恋爱/结婚已过显示"天在一起"，其它类型已过显示"天已过"，未到显示"天"
 */
const getCardUnit = (item) => {
  if (item.daysTogether) {
    return item.type === 'love' || item.type === 'marriage' ? '天在一起' : '天已过'
  }
  return '天后'
}

/**
 * 生成卡片背景渐变色
 * 优先使用自定义颜色，否则按类型使用默认渐变
 * @param {string} color - 自定义颜色值
 * @param {string} type - 纪念日类型
 */
const getCardBackground = (color, type) => {
  if (color && color.startsWith('#')) {
    return `linear-gradient(135deg, ${color}, ${adjustBrightness(color, 20)})`
  }
  const map = {
    'love': 'linear-gradient(135deg, #FF7B9C, #F43F5E)',
    'marriage': 'linear-gradient(135deg, #3B82F6, #1D4ED8)',
    'birthday': 'linear-gradient(135deg, #10B981, #059669)',
    'other': 'linear-gradient(135deg, #8B5CF6, #7C3AED)'
  }
  return map[type] || 'linear-gradient(135deg, #FF7B9C, #F43F5E)'
}

/**
 * 调整颜色亮度
 * @param {string} hex - 十六进制颜色值
 * @param {number} percent - 亮度调整百分比
 */
const adjustBrightness = (hex, percent) => {
  const num = parseInt(hex.replace('#', ''), 16)
  const amt = Math.round(2.55 * percent)
  const R = (num >> 16) + amt
  const G = (num >> 8 & 0x00FF) + amt
  const B = (num & 0x0000FF) + amt
  return '#' + (
    0x1000000 +
    (R < 255 ? R < 1 ? 0 : R : 255) * 0x10000 +
    (G < 255 ? G < 1 ? 0 : G : 255) * 0x100 +
    (B < 255 ? B < 1 ? 0 : B : 255)
  ).toString(16).slice(1)
}



onMounted(() => {
  loadMemorials()
})
</script>

<style lang="scss" scoped>
.memorial-container {
  background-color: $bg-page;
  min-height: 100vh;
}

.header-section {
  background: $gradient-blue-pink;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 50rpx 30rpx 50rpx;
  gap: $spacing-xs;
}

.header-title {
  font-size: 46rpx;
  font-weight: $font-bold;
  color: $text-white;
}

.header-desc {
  font-size: $font-md;
  color: rgba(255, 255, 255, 0.9);
}

.scroll-box {
  height: calc(100vh - 300rpx);
}

.page-content {
  padding: 30rpx;
}

.section-title {
  font-size: $font-xl;
  font-weight: $font-semibold;
  color: $text-primary;
  margin-bottom: $spacing-lg;
  margin-top: $spacing-xs;
}

.loading-box {
  padding: 40rpx 0;
}

.empty-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0;
  gap: $spacing-md;
}

.empty-text {
  font-size: $font-md;
  color: $text-placeholder;
}

.gradient-card {
  border-radius: $radius-lg;
  padding: 30rpx;
  margin-bottom: $spacing-md;
}



.card-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-left {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.card-tag {
  align-self: flex-start;
  padding: $spacing-xs $spacing-lg;
  border-radius: $radius-md;
  background: rgba(255, 255, 255, 0.3);
  font-size: $font-xs;
  color: $text-white;
  font-weight: $font-medium;
}

.card-name {
  font-size: 34rpx;
  font-weight: $font-semibold;
  color: $text-white;
}

.card-date {
  font-size: $font-sm;
  color: rgba(255, 255, 255, 0.9);
}

.card-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  color: $text-white;
}

.num {
  font-size: 70rpx;
  font-weight: $font-bold;
  line-height: 1.1;
}

.unit {
  font-size: $font-xs;
  opacity: 0.9;
}

.tips {
  font-size: $font-xs;
  opacity: 0.9;
}

.birthday-box {
  display: flex;
  justify-content: space-between;
  gap: $spacing-md;
  margin-bottom: 30rpx;
}

.birthday-card {
  flex: 1;
  background-color: $bg-white;
  border-radius: $radius-lg;
  padding: 30rpx 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: $gradient-blue-pink;
  @include flex-center;
  margin-bottom: $spacing-xs;
  overflow: hidden;
  
  image {
    width: 100%;
    height: 100%;
    border-radius: 50%;
  }
}

.b-name {
  font-size: $font-lg;
  font-weight: $font-semibold;
  color: $text-primary;
}

.b-date {
  font-size: $font-xs;
  color: $text-placeholder;
}

.birthday-tag {
  padding: 6rpx 20rpx;
  border-radius: $radius-md;
  background: $gradient-blue;
  font-size: $font-xs;
  color: $text-white;
  font-weight: $font-medium;
}

.add-button {
  background: $gradient-blue-pink;
  border-radius: 50rpx;
  padding: 28rpx 0;
  @include flex-center;
  gap: $spacing-sm;
  color: $text-white;
  font-size: $font-lg;
  font-weight: $font-semibold;
  margin-top: $spacing-md;
}

.popup-content {
  background-color: $bg-white;
  border-radius: $radius-lg;
  padding: 40rpx;
  width: 600rpx;
  max-height: 60vh;
  overflow-y: auto;
}

.popup-title {
  font-size: $font-xl;
  font-weight: $font-bold;
  color: $text-primary;
  text-align: center;
  margin-bottom: $spacing-lg;
}

.form-item {
  margin-bottom: $spacing-lg;
}

.form-label {
  display: block;
  font-size: $font-md;
  color: $text-primary;
  margin-bottom: $spacing-sm;
  font-weight: $font-medium;
}

.form-input {
  width: 100%;
  padding: 20rpx;
  border: 1px solid $border-color;
  border-radius: $radius-md;
  font-size: $font-md;
  color: $text-primary;
}

.picker-input {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-textarea {
  width: 100%;
  min-height: 120rpx;
  padding: 20rpx;
  border: 1px solid $border-color;
  border-radius: $radius-md;
  font-size: $font-md;
  color: $text-primary;
}

.color-picker {
  display: flex;
  gap: $spacing-md;
  flex-wrap: wrap;
}

.color-item {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  border: 3px solid transparent;
  transition: all 0.3s;
}

.color-item.active {
  border-color: $color-primary;
  transform: scale(1.1);
}

.avatar-upload {
  position: relative;
  width: 120rpx;
  height: 120rpx;
  border: 2px dashed $border-color;
  overflow: hidden;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
  color: $text-placeholder;
  font-size: $font-xs;
}

.avatar-preview {
  width: 100%;
  height: 100%;
}

.avatar-remove {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  width: 40rpx;
  height: 40rpx;
  background-color: $bg-white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.popup-buttons {
  display: flex;
  gap: $spacing-md;
  margin-top: $spacing-xl;
}

.popup-btn {
  flex: 1;
  padding: 24rpx 0;
  border-radius: $radius-md;
  text-align: center;
  font-size: $font-md;
  font-weight: $font-semibold;
}

.popup-btn.cancel {
  background-color: $bg-gray;
  color: $text-primary;
}

.popup-btn.confirm {
  background: $gradient-blue-pink;
  color: $text-white;
}

.popup-btn.edit {
  background: $gradient-blue;
  color: $text-white;
}

.popup-btn.delete {
  background: #F43F5E;
  color: $text-white;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1px solid $border-color;
}

.detail-label {
  font-size: $font-md;
  color: $text-secondary;
}

.detail-value {
  font-size: $font-md;
  color: $text-primary;
  text-align: right;
  flex: 1;
  margin-left: $spacing-lg;
}
</style>
