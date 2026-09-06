<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import router from '@/router'

defineProps({
  isCollapse: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const routerInstance = useRouter()

// 菜单数据直接从路由表提取：取根布局路由的 children，过滤 meta.hidden
// 后续新增页面只需在路由表配置 meta，菜单自动渲染
const menuRoutes = computed(() => {
  const layoutRoute = router.options.routes.find((item) => item.path === '/')
  return (layoutRoute?.children || []).filter((item) => !item.meta?.hidden)
})

// 当前激活菜单（取一级路径，如 /user/123 高亮 /user）
const activeMenu = computed(() => '/' + (route.path.split('/')[1] || 'dashboard'))

const handleSelect = (index) => {
  routerInstance.push(index)
}
</script>

<template>
  <div class="sidebar-wrapper">
    <!-- Logo 区 -->
    <div class="logo-container">
      <el-icon :size="22" color="#fff"><Calendar /></el-icon>
      <span v-show="!isCollapse" class="logo-title">AI日历备忘录</span>
    </div>

    <!-- 导航菜单：颜色统一由 SCSS 变量 + :deep 控制 -->
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      :collapse-transition="false"
      @select="handleSelect"
    >
      <el-menu-item v-for="item in menuRoutes" :key="item.path" :index="'/' + item.path">
        <el-icon>
          <component :is="item.meta.icon" />
        </el-icon>
        <template #title>{{ item.meta.title }}</template>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<style scoped lang="scss">
.sidebar-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;

  .logo-container {
    height: $navbar-height;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    background-color: $sidebar-bg-deep;
    flex-shrink: 0;
    overflow: hidden;
    white-space: nowrap;
  }

  .logo-title {
    color: #fff;
    font-size: 15px;
    font-weight: 600;
  }

  /* el-menu 为第三方组件内部结构，需 :deep 穿透 scoped 边界 */
  :deep(.el-menu) {
    flex: 1;
    height: 100%;
    border-right: none;
    background-color: $sidebar-bg;
  }

  :deep(.el-menu-item) {
    color: $sidebar-text;
  }

  /* 选中菜单项：#053f74 高亮 */
  :deep(.el-menu-item.is-active) {
    background-color: $sidebar-active-bg !important;
    color: #ffffff !important;
  }

  /* 悬停菜单项：稍亮深蓝 */
  :deep(.el-menu-item:hover) {
    background-color: $sidebar-hover-bg !important;
    color: #ffffff;
  }
}
</style>
