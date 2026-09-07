<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import SearchForm from '@/components/SearchForm/index.vue'
import { pageMemorials, getMemorialById, deleteMemorial } from '@/api/memorial'

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

// 列表与分页
const tableData = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 查询条件
const queryParams = reactive({ keyword: '', type: '' })

// 详情弹窗
const detailVisible = ref(false)
const detailData = ref({})
const detailLoading = ref(false)

// 拉取列表数据
const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      keyword: queryParams.keyword || undefined,
      type: queryParams.type || undefined,
      page: page.value,
      pageSize: pageSize.value
    }
    const res = await pageMemorials(params)
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

// 查看详情：调详情接口
const handleDetail = async (row) => {
  detailLoading.value = true
  detailVisible.value = true
  try {
    const data = await getMemorialById(row.id)
    detailData.value = data
  } catch {
    // 错误提示已由拦截器处理
  } finally {
    detailLoading.value = false
  }
}

// 删除纪念日：二次确认
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除纪念日「${row.name}」吗？删除后不可恢复。`, '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error'
    })
  } catch {
    return
  }
  try {
    await deleteMemorial(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch {
    // 错误提示已由拦截器处理
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
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="name" label="纪念日名称" min-width="140" align="center" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :color="typeMap[row.type]?.color" style="color: #fff; border: none">
              {{ typeMap[row.type]?.label || '其他' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="date" label="日期" min-width="120" align="center" />
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
          @current-change="handlePageChange"
          @size-change="handlePageSizeChange"
        />
      </div>
    </div>

    <!-- 纪念日详情弹窗 -->
    <el-dialog v-model="detailVisible" title="纪念日详情" width="480px">
      <el-descriptions v-loading="detailLoading" :column="1" border>
        <el-descriptions-item label="名称">{{ detailData.name }}</el-descriptions-item>
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
