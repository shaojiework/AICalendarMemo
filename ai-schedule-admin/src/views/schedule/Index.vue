<script setup>
import { ref } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import SearchForm from '@/components/SearchForm/index.vue'

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

// 日程列表静态数据（对接阶段替换为接口返回）
const tableData = ref([
  { id: 1, title: '产品周会', username: 'xiaoju', type: 'meeting', startTime: '2026-09-07 10:00:00', endTime: '2026-09-07 11:00:00', location: '会议室A', description: '同步本周产品进度' },
  { id: 2, title: '提交季度报告', username: 'zhangsan', type: 'work', startTime: '2026-09-08 18:00:00', endTime: '2026-09-08 19:00:00', location: '公司', description: 'Q3 季度总结报告' },
  { id: 3, title: '健身房训练', username: 'lisi', type: 'personal', startTime: '2026-09-09 19:30:00', endTime: '2026-09-09 21:00:00', location: '乐刻健身', description: '背部+有氧' },
  { id: 4, title: '牙医复诊', username: 'wangwu', type: 'normal', startTime: '2026-09-10 14:00:00', endTime: '2026-09-10 15:00:00', location: '口腔医院', description: '' }
])

const total = ref(128)
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

// 删除日程：二次确认
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除日程「${row.title}」吗？删除后不可恢复。`, '删除确认', {
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
        <el-table-column prop="title" label="日程标题" min-width="140" />
        <el-table-column prop="username" label="所属用户" min-width="110" />
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
        <el-table-column prop="startTime" label="开始时间" min-width="160" />
        <el-table-column prop="location" label="地点" min-width="120" />
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

    <!-- 日程详情弹窗（新增/编辑同样使用 el-dialog 形式） -->
    <el-dialog v-model="detailVisible" title="日程详情" width="480px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="日程标题">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="所属用户">{{ detailData.username }}</el-descriptions-item>
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
