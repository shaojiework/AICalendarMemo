<script setup>
import { ref } from 'vue'
import SearchForm from '@/components/SearchForm/index.vue'

// 搜索区配置：用户账号/昵称
const searchFields = [
  { label: '用户', prop: 'keyword', component: 'el-input', placeholder: '请输入用户账号/昵称' }
]

// 会话列表静态数据（对接阶段替换为接口返回）
const tableData = ref([
  { id: 1, conversationId: 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', username: 'xiaoju', nickname: '小橘', messageCount: 24, lastTime: '2026-09-06 15:20:00' },
  { id: 2, conversationId: 'b2c3d4e5-f6a7-8901-bcde-f12345678901', username: 'zhangsan', nickname: '张三', messageCount: 8, lastTime: '2026-09-06 11:05:00' },
  { id: 3, conversationId: 'c3d4e5f6-a7b8-9012-cdef-123456789012', username: 'lisi', nickname: '李四', messageCount: 36, lastTime: '2026-09-05 21:43:00' }
])

const total = ref(210)
const page = ref(1)
const pageSize = ref(10)

// 对话详情弹窗
const detailVisible = ref(false)
const currentConversation = ref({})

// 会话消息静态数据（对接阶段按 conversationId 查询 ai_chat）
const messageList = ref([
  { role: 'user', content: '帮我看看明天有什么日程' },
  { role: 'assistant', content: '明天（9月7日）你有 1 个日程：\n10:00-11:00 产品周会，地点：会议室A。\n记得提前准备本周进度材料哦～' },
  { role: 'user', content: '帮我记一下，9月10日下午3点去看牙医' },
  { role: 'assistant', content: '已为你创建日程：\n📅 牙医复诊\n时间：9月10日 15:00\n地点：口腔医院\n到时候我会提醒你～' }
])

const handleSearch = () => {
  page.value = 1
}

// 查看对话详情
const handleDetail = (row) => {
  currentConversation.value = row
  detailVisible.value = true
}
</script>

<template>
  <div class="page-container">
    <!-- 搜索区 -->
    <SearchForm :fields="searchFields" @search="handleSearch" />

    <!-- 表格区 -->
    <div class="table-card">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="会话ID" min-width="260">
          <template #default="{ row }">
            <span class="conv-id">{{ row.conversationId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用户" min-width="140">
          <template #default="{ row }">
            {{ row.nickname }}（{{ row.username }}）
          </template>
        </el-table-column>
        <el-table-column prop="messageCount" label="消息数" width="90" align="center" />
        <el-table-column prop="lastTime" label="最后消息时间" min-width="170" />
        <el-table-column label="操作" width="110" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">查看对话</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </div>

    <!-- 对话详情弹窗：消息气泡形式展示 -->
    <el-dialog v-model="detailVisible" title="对话详情" width="600px">
      <div class="conv-header">
        <span>用户：{{ currentConversation.nickname }}（{{ currentConversation.username }}）</span>
        <span>消息数：{{ currentConversation.messageCount }}</span>
      </div>
      <div class="chat-box">
        <div
          v-for="(msg, index) in messageList"
          :key="index"
          class="chat-item"
          :class="msg.role"
        >
          <div class="chat-bubble">{{ msg.content }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.page-container {
  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  .conv-id {
    font-family: monospace;
    color: $sidebar-text;
    font-size: 12px;
  }

  .conv-header {
    display: flex;
    justify-content: space-between;
    color: var(--el-text-color-regular);
    margin-bottom: 12px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .chat-box {
    max-height: 420px;
    overflow-y: auto;
    padding: 8px;
    background-color: var(--el-bg-color-page);
    border-radius: 8px;
  }

  .chat-item {
    display: flex;
    margin-bottom: 14px;

    &.user {
      justify-content: flex-end;
    }
  }

  .chat-bubble {
    max-width: 75%;
    padding: 10px 14px;
    border-radius: 10px;
    font-size: 13px;
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-all;
  }

  .chat-item.assistant .chat-bubble {
    background-color: var(--el-fill-color);
    color: var(--el-text-color-primary);
    border-top-left-radius: 2px;
  }

  .chat-item.user .chat-bubble {
    background-color: $sidebar-active-bg;
    color: #fff;
    border-top-right-radius: 2px;
  }
}
</style>
