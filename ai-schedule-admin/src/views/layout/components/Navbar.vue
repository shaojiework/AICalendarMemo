<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'

defineProps({
  isCollapse: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggle-collapse'])

const route = useRoute()

// 面包屑：从当前路由 matched 链提取 meta.title，自动随路由变化
const breadcrumbs = computed(() =>
  route.matched.filter((item) => item.meta?.title).map((item) => item.meta.title)
)

// 退出登录：二次确认（静态阶段仅提示，对接阶段清除 token 并跳转登录页）
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      ElMessage.success('已退出登录')
      // TODO 对接阶段：清除本地 token，router.push('/login')
    })
    .catch(() => {})
}
</script>

<template>
  <div class="navbar">
    <div class="navbar-left">
      <!-- 侧边栏折叠/展开按钮 -->
      <el-icon class="collapse-btn" :size="20" @click="emit('toggle-collapse')">
        <Fold v-if="!isCollapse" />
        <Expand v-else />
      </el-icon>
      <!-- 面包屑导航 -->
      <el-breadcrumb separator="/">
        <el-breadcrumb-item v-for="(crumb, index) in breadcrumbs" :key="index">
          {{ crumb }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="navbar-right">
      <el-dropdown trigger="click">
        <div class="admin-info">
          <el-avatar :size="32" class="admin-avatar">管</el-avatar>
          <span class="admin-name">管理员</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item divided @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<style scoped lang="scss">
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 16px;

  .navbar-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .collapse-btn {
    cursor: pointer;
    color: var(--el-text-color-secondary);

    &:hover {
      color: #409eff;
    }
  }

  .navbar-right {
    display: flex;
    align-items: center;
  }

  .admin-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    outline: none;
  }

  .admin-avatar {
    background-color: $sidebar-active-bg;
    font-size: 13px;
  }

  .admin-name {
    font-size: 13px;
    color: var(--el-text-color-primary);
  }
}
</style>
