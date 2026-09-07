import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import Layout from '@/views/layout/Index.vue'

/**
 * 后台路由表
 * meta.title：菜单名
 * meta.icon：Element Plus 图标组件名
 * meta.roles：允许访问的角色列表（USER/ADMIN），用于路由级权限校验
 */
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Index.vue'),
        meta: { title: '首页仪表盘', icon: 'Odometer', roles: ['ADMIN'] }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/Index.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['ADMIN'] }
      },
      {
        path: 'schedule',
        name: 'Schedule',
        component: () => import('@/views/schedule/Index.vue'),
        meta: { title: '日程管理', icon: 'Calendar', roles: ['ADMIN'] }
      },
      {
        path: 'memorial',
        name: 'Memorial',
        component: () => import('@/views/memorial/Index.vue'),
        meta: { title: '纪念日管理', icon: 'Star', roles: ['ADMIN'] }
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/chat/Index.vue'),
        meta: { title: 'AI聊天记录', icon: 'ChatDotRound', roles: ['ADMIN'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

/**
 * 全局路由守卫
 * 1. 未登录访问受保护页面 → 跳转登录页
 * 2. 已登录访问登录页 → 跳转首页
 * 3. 登录用户角色不在 meta.roles 列表内 → 提示无权限并跳转登录页（清理本地存储）
 */
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (to.path === '/login') {
    // 已登录访问登录页，直接跳转首页
    if (authStore.token) {
      next('/')
    } else {
      next()
    }
    return
  }
  // 其他页面需要登录
  if (!authStore.token) {
    next('/login')
    return
  }
  // 校验角色权限：登录返回的 role 必须在目标路由 meta.roles 中
  const requiredRoles = to.meta?.roles
  const currentRole = authStore.userInfo?.role
  if (requiredRoles && requiredRoles.length > 0) {
    if (!currentRole || !requiredRoles.includes(currentRole)) {
      // 角色不匹配：清理登录态后回登录页
      authStore.logout()
      next('/login')
      return
    }
  }
  next()
})

export default router
