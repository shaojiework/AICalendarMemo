<script setup>
import { ref } from 'vue'
import Sidebar from './components/Sidebar.vue'
import Navbar from './components/Navbar.vue'
import AiAgentFloat from '@/components/AiAgentFloat.vue'

// 侧边栏折叠状态（由顶栏折叠按钮切换）
const isCollapse = ref(false)
</script>

<template>
  <div class="layout-wrapper">
    <!-- 左侧侧边栏：折叠态通过 is-collapse 类切换宽度 -->
    <aside class="layout-sidebar" :class="{ 'is-collapse': isCollapse }">
      <Sidebar :is-collapse="isCollapse" />
    </aside>

    <!-- 右侧主体列 -->
    <div class="layout-body">
      <!-- 顶部导航栏 -->
      <header class="layout-header">
        <Navbar :is-collapse="isCollapse" @toggle-collapse="isCollapse = !isCollapse" />
      </header>
      <!-- 主内容区：flex:1 + min-height:0 占满剩余高度，为唯一滚动容器 -->
      <main class="layout-main">
        <router-view />
      </main>
    </div>

    <!-- 全局悬浮AI智能体：与 router-view 同级，切换路由不销毁，单例WS复用 -->
    <AiAgentFloat />
  </div>
</template>

<style scoped lang="scss">
/* 高度链：100vh 定高锚点 → 主体列 flex:1 → 主内容区 flex:1 + min-height:0 */
.layout-wrapper {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.layout-sidebar {
  width: $sidebar-width;
  height: 100%;
  flex-shrink: 0;
  background-color: $sidebar-bg;
  overflow: hidden;
  transition: width 0.28s;

  &.is-collapse {
    width: $sidebar-collapse-width;
  }
}

.layout-body {
  flex: 1;
  min-width: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.layout-header {
  height: $navbar-height;
  flex-shrink: 0;
  background-color: $navbar-bg;
  border-bottom: 1px solid $navbar-border;
}

.layout-main {
  flex: 1;
  /* flex 子项默认 min-height:auto 会被内容撑开，置 0 后才能占满剩余高度并内部滚动 */
  min-height: 0;
  overflow-y: auto;
  padding: 16px;
  background-color: #17314c;
}
</style>
