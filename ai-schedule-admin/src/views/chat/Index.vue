<script setup>
import { ref, reactive, onMounted } from 'vue'
import SearchForm from '@/components/SearchForm/index.vue'
import { pageConversations, getMessages } from '@/api/chat'

// 搜索区配置：按对话ID关键字模糊搜索（后端聚合后无用户信息）
const searchFields = [
  { label: '对话ID', prop: 'keyword', component: 'el-input', placeholder: '请输入对话ID关键字' }
]

// 会话列表与分页
const tableData = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 查询条件
const queryParams = reactive({ keyword: '' })

// 对话详情弹窗
const detailVisible = ref(false)
const currentConversation = ref({})
const messageList = ref([])
const detailLoading = ref(false)

// 拉取会话列表
const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      keyword: queryParams.keyword || undefined,
      page: page.value,
      pageSize: pageSize.value
    }
    const res = await pageConversations(params)
    tableData.value = res.list || []
    total.value = res.total || 0
  } catch {
    // 错误提示已由拦截器处理
  } finally {
    loading.value = false
  }
}

const handleSearch = (params) => {
  Object.assign(queryParams, params)
  page.value = 1
  fetchList()
}

const handleReset = () => {
  page.value = 1
}

const handlePageChange = () => fetchList()
const handlePageSizeChange = () => {
  page.value = 1
  fetchList()
}

// 查看对话详情：按 conversationId 拉取全部消息
const handleDetail = async (row) => {
  currentConversation.value = row
  detailVisible.value = true
  detailLoading.value = true
  messageList.value = []
  try {
    const data = await getMessages(row.conversationId)
    messageList.value = data || []
  } catch {
    // 错误提示已由拦截器处理
  } finally {
    detailLoading.value = false
  }
}

onMounted(fetchList)
</script>

<template>
  <div class="page-container">
    <!-- 搜索区 -->
    <SearchForm :fields="searchFields" @search="handleSearch" @reset="handleReset" />

    <!-- 表格区 -->
    <div class="table-card">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column label="会话ID" min-width="280" align="center">
          <template #default="{ row }">
            <span class="conv-id">{{ row.conversationId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="messageCount" label="消息数" width="100" align="center" />
        <el-table-column prop="lastTime" label="最后消息时间" min-width="170" align="center" />
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
          @current-change="handlePageChange"
          @size-change="handlePageSizeChange"
        />
      </div>
    </div>

    <!-- 对话详情弹窗：消息气泡形式展示 -->
    <el-dialog v-model="detailVisible" title="对话详情" width="600px">
      <div class="conv-header">
        <span>对话ID：{{ currentConversation.conversationId }}</span>
        <span>消息数：{{ currentConversation.messageCount }}</span>
      </div>
      <div v-loading="detailLoading" class="chat-box">
        <div
          v-for="(msg, index) in messageList"
          :key="index"
          class="chat-item"
          :class="msg.role"
        >
          <div class="chat-bubble">{{ msg.content }}</div>
        </div>
        <div v-if="!detailLoading && messageList.length === 0" class="empty-tip">暂无消息记录</div>
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
    word-break: break-all;
  }

  .chat-box {
    max-height: 420px;
    overflow-y: auto;
    padding: 8px;
    background-color: var(--el-bg-color-page);
    border-radius: 8px;
    min-height: 200px;
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

  .empty-tip {
    text-align: center;
    color: var(--el-text-color-placeholder);
    padding: 40px 0;
  }
}
</style>
