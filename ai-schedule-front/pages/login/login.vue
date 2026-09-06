<template>
  <view class="login-page">
    <!-- 顶部品牌区 -->
    <view class="brand-area">
      <view class="brand-logo">
        <uni-icons type="calendar-filled" size="56" color="#ffffff"></uni-icons>
      </view>
      <text class="brand-title">AI 日历备忘</text>
      <text class="brand-subtitle">记录每一天的重要时刻</text>
    </view>

    <!-- 表单卡片 -->
    <view class="form-card">
      <!-- 模式切换 -->
      <view class="mode-tabs">
        <view class="mode-tab" :class="{ active: mode === 'login' }" @tap="switchMode('login')">
          <text>登录</text>
        </view>
        <view class="mode-tab" :class="{ active: mode === 'register' }" @tap="switchMode('register')">
          <text>注册</text>
        </view>
      </view>

      <!-- 登录表单 -->
      <view v-if="mode === 'login'" class="form-area">
        <view class="input-item">
          <uni-icons type="person" size="22" color="#a0a0b8"></uni-icons>
          <input v-model="loginForm.username" class="input" placeholder="请输入用户名" placeholder-class="placeholder" />
        </view>
        <view class="input-item">
          <uni-icons type="locked-filled" size="22" color="#a0a0b8"></uni-icons>
          <input v-model="loginForm.password" class="input" type="password" password placeholder="请输入密码" placeholder-class="placeholder" />
        </view>
        <button class="submit-btn" :disabled="loading" @tap="handleLogin">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </view>

      <!-- 注册表单 -->
      <view v-else class="form-area">
        <view class="input-item">
          <uni-icons type="person" size="22" color="#a0a0b8"></uni-icons>
          <input v-model="registerForm.username" class="input" placeholder="请输入用户名" placeholder-class="placeholder" />
        </view>
        <view class="input-item">
          <uni-icons type="chatboxes" size="22" color="#a0a0b8"></uni-icons>
          <input v-model="registerForm.nickname" class="input" placeholder="昵称（选填）" placeholder-class="placeholder" />
        </view>
        <view class="input-item">
          <uni-icons type="locked-filled" size="22" color="#a0a0b8"></uni-icons>
          <input v-model="registerForm.password" class="input" type="password" password placeholder="请输入密码（6-32位）" placeholder-class="placeholder" />
        </view>
        <view class="input-item">
          <uni-icons type="checkmarkempty" size="22" color="#a0a0b8"></uni-icons>
          <input v-model="registerForm.confirmPassword" class="input" type="password" password placeholder="请再次输入密码" placeholder-class="placeholder" />
        </view>
        <button class="submit-btn" :disabled="loading" @tap="handleRegister">
          {{ loading ? '注册中...' : '注 册' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { authApi } from '@/api/auth'
import { setLoginState } from '@/utils/auth'

// 当前模式：login登录 / register注册
const mode = ref('login')
// 提交加载状态
const loading = ref(false)

// 登录表单
const loginForm = reactive({ username: '', password: '' })
// 注册表单
const registerForm = reactive({ username: '', nickname: '', password: '', confirmPassword: '' })

/**
 * 切换登录/注册模式
 */
const switchMode = (target) => {
  mode.value = target
}

/**
 * 处理登录：USER进入首页，ADMIN弹窗提示前往后台登录，不做跨项目跳转
 */
const handleLogin = async () => {
  if (!loginForm.username.trim()) {
    return uni.showToast({ title: '请输入用户名', icon: 'none' })
  }
  if (!loginForm.password) {
    return uni.showToast({ title: '请输入密码', icon: 'none' })
  }
  loading.value = true
  try {
    const res = await authApi.login({
      username: loginForm.username.trim(),
      password: loginForm.password
    })
    const { token, role, nickname, username, avatar, userId } = res.data
    // ADMIN账号专用于后台管理系统，前台弹窗提示后留在登录页
    if (role === 'ADMIN') {
      uni.showModal({
        title: '提示',
        content: '管理员请前往后台管理地址登录',
        showCancel: false
      })
      return
    }
    // 普通用户：保存登录状态并进入首页
    setLoginState(token, { userId, username, nickname, avatar, role })
    uni.reLaunch({ url: '/pages/home/home' })
  } catch (err) {
    // 错误提示已由request.js统一处理
  } finally {
    loading.value = false
  }
}

/**
 * 处理注册：校验通过后自动切回登录模式并预填用户名
 */
const handleRegister = async () => {
  const { username, nickname, password, confirmPassword } = registerForm
  if (!username.trim()) {
    return uni.showToast({ title: '请输入用户名', icon: 'none' })
  }
  if (password.length < 6 || password.length > 32) {
    return uni.showToast({ title: '密码长度须为6-32位', icon: 'none' })
  }
  if (password !== confirmPassword) {
    return uni.showToast({ title: '两次输入的密码不一致', icon: 'none' })
  }
  loading.value = true
  try {
    await authApi.register({
      username: username.trim(),
      nickname: nickname.trim() || undefined,
      password
    })
    uni.showToast({ title: '注册成功，请登录', icon: 'success' })
    // 预填用户名并切回登录模式
    loginForm.username = username.trim()
    loginForm.password = ''
    switchMode('login')
  } catch (err) {
    // 错误提示已由request.js统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  height: 100vh;
  background: linear-gradient(160deg, #7940EC 0%, #7B5EA7 45%, #F8F8F8 100%);
  padding: 120rpx 48rpx 0;
  box-sizing: border-box;
}

/* 品牌区 */
.brand-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 72rpx;
}

.brand-logo {
  width: 136rpx;
  height: 136rpx;
  border-radius: 40rpx;
  background: rgba(255, 255, 255, 0.22);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28rpx;
}

.brand-title {
  font-size: 44rpx;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 12rpx;
}

.brand-subtitle {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.75);
}

/* 表单卡片 */
.form-card {
  background: #ffffff;
  border-radius: 32rpx;
  padding: 40rpx 44rpx 56rpx;
  box-shadow: 0 16rpx 48rpx rgba(121, 64, 236, 0.18);
}

/* 模式切换tabs */
.mode-tabs {
  display: flex;
  background: #F5F5FA;
  border-radius: 20rpx;
  padding: 8rpx;
  margin-bottom: 44rpx;
}

.mode-tab {
  flex: 1;
  text-align: center;
  padding: 18rpx 0;
  border-radius: 14rpx;
  font-size: 30rpx;
  color: #6b6b8a;
  transition: all 0.25s;

  &.active {
    background: #ffffff;
    color: #7940EC;
    font-weight: 600;
    box-shadow: 0 4rpx 12rpx rgba(121, 64, 236, 0.15);
  }
}

/* 表单区 */
.form-area {
  display: flex;
  flex-direction: column;
  gap: 28rpx;
}

.input-item {
  display: flex;
  align-items: center;
  gap: 18rpx;
  background: #F8F8FC;
  border-radius: 20rpx;
  padding: 26rpx 28rpx;
}

.input {
  flex: 1;
  font-size: 30rpx;
  color: #1a1a2e;
}

.placeholder {
  color: #a0a0b8;
}

/* 提交按钮 */
.submit-btn {
  width: 50%;
  margin-top: 16rpx;
  background: linear-gradient(135deg, #7940EC 0%, #7B5EA7 100%);
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 600;
  border-radius: 20rpx;
  padding: 10rpx 0;
  border: none;

  &[disabled] {
    opacity: 0.6;
    color: #ffffff;
    background: linear-gradient(135deg, #7940EC 0%, #7B5EA7 100%);
  }
}
</style>
