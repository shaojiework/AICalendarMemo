<script setup>
import { ref } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import SearchForm from '@/components/SearchForm/index.vue'

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

// 用户列表静态数据（对接阶段替换为接口返回，字段与后端 UserResponse 对齐）
const tableData = ref([
  { id: 1, username: 'admin', nickname: '管理员', phone: '13800000000', role: 'ADMIN', status: 1, createdAt: '2026-09-01 10:00:00' },
  { id: 2, username: 'xiaoju', nickname: '小橘', phone: '13812345678', role: 'USER', status: 1, createdAt: '2026-09-02 14:23:00' },
  { id: 3, username: 'zhangsan', nickname: '张三', phone: '13987654321', role: 'USER', status: 1, createdAt: '2026-09-03 09:12:00' },
  { id: 4, username: 'lisi', nickname: '李四', phone: '13711112222', role: 'USER', status: 0, createdAt: '2026-09-04 16:45:00' },
  { id: 5, username: 'wangwu', nickname: '王五', phone: '13633334444', role: 'USER', status: 1, createdAt: '2026-09-05 11:30:00' }
])

// 查询条件（搜索组件回传）
const queryParams = ref({ keyword: '', status: '' })
const total = ref(86)
const page = ref(1)
const pageSize = ref(10)

// 查询回调（静态阶段仅记录条件，对接阶段调用列表接口）
const handleSearch = (params) => {
  queryParams.value = params
  page.value = 1
}

// 启用/禁用切换：二次确认
const handleToggleStatus = (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  ElMessageBox.confirm(`确定要${action}用户「${row.nickname}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      row.status = row.status === 1 ? 0 : 1
      ElMessage.success(`${action}成功`)
    })
    .catch(() => {})
}

// 角色文案
const roleText = (role) => (role === 'ADMIN' ? '管理员' : '普通用户')
</script>

<template>
  <div class="page-container">
    <!-- 搜索区 -->
    <SearchForm :fields="searchFields" @search="handleSearch" />

    <!-- 表格区 -->
    <div class="table-card">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="账号" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'">{{ roleText(row.role) }}</el-tag>
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
