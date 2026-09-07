<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import SearchForm from '@/components/SearchForm/index.vue'
import { pageSchedules, getScheduleById, deleteSchedule } from '@/api/schedule'

// 搜索区配置：标题关键字 + 类型 + 日期
const searchFields = [
  { label: '标题', prop: 'keyword', component: 'el-input', placeholder: '请输入日程标题' },
  {
    label: '类型',
    prop: 'type',
    component: 'el-select',
    options: [
      { label: '工作', value: 'work' },
      { label: '个人', value: 'personal' },
      { label: '会议', value: 'meeting' },
      { label: '其他', value: 'normal' }
    ]
  },
  {
    label: '日期',
    prop: 'date',
    component: 'el-date-picker',
    placeholder: '请选择日期',
    attrs: { type: 'date', 'value-format': 'YYYY-MM-DD' }
  }
]

// 日程类型映射（与前端/AI工具 value 约定一致）
const typeMap = {
  work: { label: '工作', color: '#409EFF' },
  personal: { label: '个人', color: '#FF7B9C' },
  meeting: { label: '会议', color: '#7940EC' },
  normal: { label: '其他', color: '#36C9A5' }
}

// 列表与分页
const tableData = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 查询条件
const queryParams = reactive({ keyword: '', type: '', date: '' })

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
      date: queryParams.date || undefined,
      page: page.value,
      pageSize: pageSize.value
    }
    const res = await pageSchedules(params)
    tableData.value = res.list || []
    total.value = res.total || 0
  } catch {
    // 错误提示已由拦截器处理
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
    const data = await getScheduleById(row.id)
    detailData.value = data
  } catch {
    // 错误提示已由拦截器处理
  } finally {
    detailLoading.value = false
  }
}

// 删除日程：二次确认
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除日程「${row.title}」吗？删除后不可恢复。`, '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error'
    })
  } catch {
    return
  }
  try {
    await deleteSchedule(row.id)
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
        <el-table-column prop="title" label="日程标题" min-width="140" align="center" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag
              :color="typeMap[row.type]?.color"
              style="color: #fff; border: none"
            >
              {{ typeMap[row.type]?.label || '其他' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" min-width="160" align="center" />
        <el-table-column prop="location" label="地点" min-width="120" align="center" />
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

    <!-- 日程详情弹窗 -->
    <el-dialog v-model="detailVisible" title="日程详情" width="480px">
      <el-descriptions v-loading="detailLoading" :column="1" border>
        <el-descriptions-item label="日程标题">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ typeMap[detailData.type]?.label }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ detailData.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ detailData.endTime }}</el-descriptions-item>
        <el-descriptions-item label="地点">{{ detailData.location || '无' }}</el-descriptions-item>
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
