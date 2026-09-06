<script setup>
import { computed } from 'vue'

// 统计卡片静态数据（对接阶段替换为接口返回）
const stats = [
  { label: '总用户数', value: 128, icon: 'User', color: '#409EFF' },
  { label: '日程总数', value: 356, icon: 'Calendar', color: '#7940EC' },
  { label: '纪念日总数', value: 92, icon: 'Star', color: '#FF7B9C' },
  { label: 'AI会话总数', value: 1024, icon: 'ChatDotRound', color: '#36C9A5' }
]

const cards = computed(() => stats)
</script>

<template>
  <!-- 根容器复用 page-container 弹性列：统计行固定高，概览区 flex:1 填满剩余 -->
  <div class="page-container dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col v-for="item in cards" :key="item.label" :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: item.color }">
              <el-icon :size="26" color="#fff">
                <component :is="item.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统概览：复用 table-card 容器，flex:1 占满剩余高度 -->
    <div class="table-card welcome-card">
      <div class="card-title">系统概览</div>
      <div class="welcome-text">
        欢迎使用 AI 日历备忘录后台管理系统，可在左侧菜单管理平台用户、日程、纪念日及 AI 聊天记录。
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.dashboard {
  /* 统计行不参与压缩，高度由内容决定 */
  .stat-row {
    flex-shrink: 0;
  }

  .stat-card {
    margin-bottom: 16px;
  }

  .stat-content {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .stat-icon {
    width: 52px;
    height: 52px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .stat-value {
    font-size: 22px;
    font-weight: 700;
    color: var(--el-text-color-primary);
    line-height: 1.2;
  }

  .stat-label {
    font-size: 13px;
    color: var(--el-text-color-secondary);
    margin-top: 4px;
  }

  /* 概览区：标题固定，正文区 flex:1 填满卡片剩余高度 */
  .welcome-card {
    .card-title {
      font-weight: 600;
      margin-bottom: 12px;
      flex-shrink: 0;
    }

    .welcome-text {
      flex: 1;
      color: var(--el-text-color-regular);
      line-height: 1.8;
    }
  }
}
</style>
