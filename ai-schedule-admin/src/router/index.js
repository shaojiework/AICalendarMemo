import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import Layout from '@/views/layout/Index.vue'

/**
 * 后台路由表
 * meta.title：菜单名
 * meta.icon：Element Plus 图标组件名
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
        meta: { title: '首页仪表盘', icon: 'Odometer' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/Index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'schedule',
        name: 'Schedule',
        component: () => import('@/views/schedule/Index.vue'),
        meta: { title: '日程管理', icon: 'Calendar' }
      },
      {
        path: 'memorial',
        name: 'Memorial',
        component: () => import('@/views/memorial/Index.vue'),
        meta: { title: '纪念日管理', icon: 'Star' }
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/chat/Index.vue'),
        meta: { title: 'AI聊天记录', icon: 'ChatDotRound' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

/**
 * 全局路由守卫：未登录时强制跳转登录页
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
  next()
})

export default router
