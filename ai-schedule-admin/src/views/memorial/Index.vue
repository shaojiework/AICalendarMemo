<script setup>
import { ref } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import SearchForm from '@/components/SearchForm/index.vue'

// 搜索区配置：名称关键字 + 类型
const searchFields = [
  { label: '名称', prop: 'keyword', component: 'el-input', placeholder: '请输入纪念日名称' },
  {
    label: '类型',
    prop: 'type',
    component: 'el-select',
    options: [
      { label: '普通', value: 'normal' },
      { label: '恋爱', value: 'love' },
      { label: '结婚', value: 'marriage' },
      { label: '生日', value: 'birthday' },
      { label: '其他', value: 'other' }
    ]
  }
]

// 纪念日类型映射（与前端/AI工具 value 约定一致）
const typeMap = {
  normal: { label: '普通', color: '#36C9A5' },
  love: { label: '恋爱', color: '#FF4D6D' },
  marriage: { label: '结婚', color: '#FF7B9C' },
  birthday: { label: '生日', color: '#F5A623' },
  other: { label: '其他', color: '#909399' }
}

// 纪念日列表静态数据（对接阶段替换为接口返回）
const tableData = ref([
  { id: 1, name: '和小橘在一起', username: 'xiaoju', type: 'love', date: '2023-05-20', isYearly: 1, description: '恋爱纪念日' },
  { id: 2, name: '妈妈生日', username: 'zhangsan', type: 'birthday', date: '1970-03-08', isYearly: 1, description: '' },
  { id: 3, name: '结婚纪念日', username: 'lisi', type: 'marriage', date: '2020-10-01', isYearly: 1, description: '婚礼当天' },
  { id: 4, name: '项目上线', username: 'wangwu', type: 'normal', date: '2026-09-20', isYearly: 0, description: '一期版本发布' }
])

const total = ref(56)
const page = ref(1)
const pageSize = ref(10)

// 详情弹窗
const detailVisible = ref(false)
const detailData = ref({})

const handleSearch = () => {
  page.value = 1
}

// 查看详情
const handleDetail = (row) => {
  detailData.value = row
  detailVisible.value = true
}

// 删除纪念日：二次确认
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除纪念日「${row.name}」吗？删除后不可恢复。`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'error'
  })
    .then(() => {
      tableData.value = tableData.value.filter((item) => item.id !== row.id)
      ElMessage.success('删除成功')
    })
    .catch(() => {})
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
        <el-table-column prop="name" label="纪念日名称" min-width="140" />
        <el-table-column prop="username" label="所属用户" min-width="110" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :color="typeMap[row.type]?.color" style="color: #fff; border: none">
              {{ typeMap[row.type]?.label || '其他' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="date" label="日期" min-width="120" />
        <el-table-column label="每年重复" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isYearly === 1 ? 'success' : 'info'">
              {{ row.isYearly === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <!-- 纪念日详情弹窗 -->
    <el-dialog v-model="detailVisible" title="纪念日详情" width="480px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="所属用户">{{ detailData.username }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ typeMap[detailData.type]?.label }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ detailData.date }}</el-descriptions-item>
        <el-descriptions-item label="每年重复">{{ detailData.isYearly === 1 ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detailData.description || '无' }}</el-descriptions-item>
      </el-descriptions>
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
}
</style>
