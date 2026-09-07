<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import SearchForm from '@/components/SearchForm/index.vue'
import { pageUsers, updateUserStatus } from '@/api/user'

// 搜索区配置（账号/昵称关键字 + 状态筛选）
const searchFields = [
  { label: '关键字', prop: 'keyword', component: 'el-input', placeholder: '请输入账号/昵称' },
  {
    label: '状态',
    prop: 'status',
    component: 'el-select',
    options: [
      { label: '启用', value: 1 },
      { label: '禁用', value: 0 }
    ]
  }
]

// 用户列表与分页
const tableData = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 查询条件（搜索组件回传）
const queryParams = reactive({ keyword: '', status: '' })

// 拉取列表数据（每次进入页面均重新请求）
const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      keyword: queryParams.keyword || undefined,
      status: queryParams.status !== '' ? queryParams.status : undefined,
      page: page.value,
      pageSize: pageSize.value
    }
    const res = await pageUsers(params)
    tableData.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    loading.value = false
  }
}

// 查询回调：重置页码后拉取
const handleSearch = (params) => {
  Object.assign(queryParams, params)
  page.value = 1
  fetchList()
}

// 重置回调：搜索组件已清空条件并触发 search，无需额外处理
const handleReset = () => {
  page.value = 1
}

// 分页变化
const handlePageChange = () => fetchList()
const handlePageSizeChange = () => {
  page.value = 1
  fetchList()
}

// 启用/禁用切换：二次确认后调接口
const handleToggleStatus = async (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}用户「${row.nickname || row.username}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  // 调接口切换状态；目标状态 = 当前状态取反
  const targetStatus = row.status === 1 ? 0 : 1
  try {
    await updateUserStatus(row.id, targetStatus)
    ElMessage.success(`${action}成功`)
    // 刷新列表
    fetchList()
  } catch {
    // 错误提示已由拦截器处理
  }
}

// 角色文案
const roleText = (role) => (role === 'ADMIN' ? '管理员' : '普通用户')

// 首次进入页面拉取数据
onMounted(fetchList)
</script>

<template>
  <div class="page-container">
    <!-- 搜索区 -->
    <SearchForm :fields="searchFields" @search="handleSearch" @reset="handleReset" />

    <!-- 表格区 -->
    <div class="table-card">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="username" label="账号" min-width="120" align="center" />
        <el-table-column prop="nickname" label="昵称" min-width="120" align="center" />
        <el-table-column prop="phone" label="手机号" min-width="130" align="center" />
        <el-table-column prop="phone" label="手机号" min-width="130" align="center" />
        <el-table-column label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'">{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <!-- 启用绿色 / 禁用红色 -->
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" min-width="170" />
        <el-table-column label="操作" width="110" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              :type="row.status === 1 ? 'danger' : 'success'"
              link
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
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
  </div>
</template>

<style scoped lang="scss">
.page-container {
  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
