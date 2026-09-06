import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/views/layout/Index.vue'

/**
 * 后台路由表
 * meta.title：菜单名/面包屑文案
 * meta.icon：Element Plus 图标组件名（已全局注册，Sidebar 用 <component :is> 渲染）
 * meta.hidden：不在侧边栏菜单显示（预留）
 */
const routes = [
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

export default router
