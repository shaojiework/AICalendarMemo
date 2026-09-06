<template>
  <view class="profile-container">
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 顶部渐变头部 -->
    <view v-else class="header-gradient">
      <view class="avatar-box" @click="changeAvatar">
        <image :src="getAvatarUrl(profile.avatar)" mode="aspectFill" class="avatar"></image>
        <view class="avatar-edit">
          <uni-icons type="camera" size="24" color="#fff"></uni-icons>
        </view>
      </view>
      <text class="username">{{ profile.nickname || profile.username }}</text>
      <text class="user-desc">{{ roleText }}</text>

      <view class="stats-row">
        <view class="stat-item">
          <text class="stat-num">{{ profile.scheduleCount || 0 }}</text>
          <text class="stat-label">日程</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-num">{{ profile.memorialCount || 0 }}</text>
          <text class="stat-label">纪念日</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-num">{{ daysUsing }}</text>
          <text class="stat-label">使用天数</text>
        </view>
      </view>
    </view>

    <!-- 第一组菜单 -->
    <view v-if="!loading" class="menu-section">
      <view class="menu-item" @click="goTo('notify')">
        <view class="menu-icon blue">
          <uni-icons type="notification-filled" size="36" color="#409eff"></uni-icons>
        </view>
        <text class="menu-text">通知设置</text>
        <uni-icons type="right" size="32" color="#C0C0D8"></uni-icons>
      </view>

      <view class="menu-item" @click="goTo('theme')">
        <view class="menu-icon pink">
          <uni-icons type="color" size="36" color="#ff7b9c"></uni-icons>
        </view>
        <text class="menu-text">外观主题</text>
        <uni-icons type="right" size="32" color="#C0C0D8"></uni-icons>
      </view>

      <view class="menu-item" @click="goTo('backup')">
        <view class="menu-icon purple">
          <uni-icons type="cloud-upload-filled" size="36" color="#7B5EA7"></uni-icons>
        </view>
        <text class="menu-text">数据备份</text>
        <uni-icons type="right" size="32" color="#C0C0D8"></uni-icons>
      </view>

      <view class="menu-item" @click="goTo('privacy')">
        <view class="menu-icon green">
          <uni-icons type="locked-filled" size="36" color="#36C9A5"></uni-icons>
        </view>
        <text class="menu-text">隐私安全</text>
        <uni-icons type="right" size="32" color="#C0C0D8"></uni-icons>
      </view>
    </view>

    <!-- 第二组菜单 -->
    <view v-if="!loading" class="menu-section">
      <view class="menu-item" @click="goTo('rate')">
        <view class="menu-icon yellow">
          <uni-icons type="star-filled" size="36" color="#FFB74D"></uni-icons>
        </view>
        <text class="menu-text">给我们评分</text>
        <uni-icons type="right" size="32" color="#C0C0D8"></uni-icons>
      </view>

      <view class="menu-item logout" @click="logout">
        <view class="menu-icon red">
          <uni-icons type="undo-filled" size="36" color="#FF6B6B"></uni-icons>
        </view>
        <text class="menu-text red-text">退出登录</text>
        <uni-icons type="right" size="32" color="#C0C0D8"></uni-icons>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { profileApi } from '@/api/profile'
import { userApi } from '@/api/user'
import { baseUrl } from '@/config/baseUrl'
import { clearLoginState, getUserInfo, setUserInfo } from '@/utils/auth'

const getAvatarUrl = (avatar) => {
  if (!avatar) return '/static/avatar.png'
  return baseUrl + avatar
}
const loading = ref(false)
// 登录用户信息：初始取本地缓存避免闪烁，加载后用接口数据覆盖
const profile = reactive({
  id: null,
  username: '',
  nickname: '',
  avatar: null,
  role: 'USER',
  createdAt: null,
  scheduleCount: 0,
  memorialCount: 0
})
Object.assign(profile, getUserInfo())

// 角色展示文案
const roleText = computed(() => profile.role === 'ADMIN' ? '管理员' : '普通用户')

// 使用天数：按注册时间计算（含当天）
const daysUsing = computed(() => {
  if (!profile.createdAt) return 0
  const created = new Date(profile.createdAt)
  return Math.max(1, Math.floor((Date.now() - created.getTime()) / 86400000) + 1)
})

// tab页每次切换都重新加载（刷新日程/纪念日统计）
onShow(() => {
  loadProfile()
})

const loadProfile = async () => {
  loading.value = true
  try {
    // 身份信息走user接口，日程/纪念日统计沿用原profile接口
    const [userRes, statRes] = await Promise.all([
      userApi.getInfo(),
      profileApi.getProfile()
    ])
    if (userRes.code === 200 && userRes.data) {
      Object.assign(profile, userRes.data)
      // 同步本地缓存，保持首页展示一致
      setUserInfo({ ...getUserInfo(), ...userRes.data })
    }
    if (statRes.code === 200 && statRes.data) {
      profile.scheduleCount = statRes.data.scheduleCount || 0
      profile.memorialCount = statRes.data.memorialCount || 0
    }
  } catch (error) {
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

const goTo = (page) => {
  uni.showToast({
    title: '前往' + page,
    icon: 'none'
  })
}

const logout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: function (res) {
      if (res.confirm) {
        // 清除登录状态并返回登录页
        clearLoginState()
        uni.showToast({
          title: '已退出',
          icon: 'none'
        })
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/login/login' })
        }, 800)
      }
    }
  })
}

const changeAvatar = () => {
  uni.showActionSheet({
    itemList: ['从相册选择', '拍照'],
    success: async (res) => {
      const sourceType = res.tapIndex === 0 ? 'album' : 'camera'
      try {
        const imageRes = await chooseImage(sourceType)
        if (imageRes.tempFilePaths.length > 0) {
          await uploadAvatar(imageRes.tempFilePaths[0])
        }
      } catch (error) {
        console.error('更换头像失败:', error)
      }
    }
  })
}

const chooseImage = (sourceType) => {
  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: [sourceType],
      success: resolve,
      fail: reject
    })
  })
}

const uploadAvatar = async (filePath) => {
  uni.showLoading({ title: '上传中...' })
  try {
    const res = await profileApi.uploadAvatar(filePath)
    if (res.code === 200) {
      profile.avatar = res.data
      // 头像路径更新到登录用户信息
      await userApi.updateProfile({ avatar: res.data })
      // 同步本地缓存，保持首页展示一致
      setUserInfo({ ...getUserInfo(), avatar: res.data })
      uni.showToast({
        title: '头像更新成功',
        icon: 'success'
      })
    }
  } catch (error) {
    uni.showToast({
      title: '上传失败',
      icon: 'none'
    })
  } finally {
    uni.hideLoading()
  }
}
</script>

<style lang="scss" scoped>
.profile-container {
  min-height: 100vh;
  background-color: $bg-page;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400rpx;
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

.header-gradient {
  background: $gradient-blue-pink;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-xxl $spacing-xl;
}

.avatar-box {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  overflow: hidden;
  border: 6rpx solid $text-white;
  margin-bottom: $spacing-lg;
  position: relative;
}

.avatar {
  width: 100%;
  height: 100%;
}

.avatar-edit {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 48rpx;
  height: 48rpx;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.username {
  font-size: $font-xxxl;
  font-weight: $font-bold;
  color: $text-white;
  margin-bottom: $spacing-xs;
}

.user-desc {
  font-size: $font-sm;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: $spacing-xl;
}

.stats-row {
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 40rpx;
  padding: $spacing-md $spacing-xxl;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 $spacing-lg;
}

.stat-num {
  font-size: $font-xxl;
  font-weight: $font-bold;
  color: $text-white;
}

.stat-label {
  font-size: $font-xs;
  color: rgba(255, 255, 255, 0.9);
  margin-top: $spacing-xs;
}

.stat-divider {
  width: 2rpx;
  height: 40rpx;
  background: rgba(255, 255, 255, 0.4);
}

.menu-section {
  margin-top: $spacing-xl;
  padding: 0 $spacing-xl;
  background-color: $bg-white;
  border-radius: $radius-lg;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 14rpx $spacing-md;
  border-bottom: 1rpx solid #f0f0f0;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: $radius-md;
  @include flex-center;
  margin-right: $spacing-lg;
}

.menu-icon.blue {
  background-color: $color-blue-bg;
}

.menu-icon.pink {
  background-color: $color-pink-bg;
}

.menu-icon.purple {
  background-color: $color-purple-bg;
}

.menu-icon.green {
  background-color: $color-green-bg;
}

.menu-icon.yellow {
  background-color: $color-yellow-bg;
}

.menu-icon.red {
  background-color: $color-red-bg;
}

.menu-text {
  flex: 1;
  font-size: $font-lg;
  color: $text-primary;
  font-weight: $font-medium;
}

.red-text {
  color: $color-red;
}
</style>