<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 登录提交
const handleLogin = () => {
  loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await authStore.login(loginForm)
      ElMessage.success('登录成功')
      router.push('/')
    } catch {
      // 错误已由拦截器提示
    } finally {
      loading.value = false
    }
  })
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>AI 日历备忘录</h2>
        <p>后台管理系统</p>
      </div>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            :prefix-icon="'User'"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="'Lock'"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped lang="scss">
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #09172d 0%, #0c2444 100%);
}

.login-card {
  width: 380px;
  padding: 40px 36px;
  background-color: $card-bg;
  border: 1px solid $navbar-border;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;

  h2 {
    font-size: 22px;
    font-weight: 700;
    color: $text-primary;
    margin: 0 0 8px;
  }

  p {
    font-size: 13px;
    color: $text-secondary;
    margin: 0;
  }
}

.login-btn {
  width: 100%;
}
</style>
