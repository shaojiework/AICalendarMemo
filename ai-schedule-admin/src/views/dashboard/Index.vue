<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getStats, getTrend, getScheduleTypeDistribution } from '@/api/dashboard'

// 统计卡片数据
const stats = ref([
  { label: '总用户数', value: 0, icon: 'User', color: '#409EFF' },
  { label: '日程总数', value: 0, icon: 'Calendar', color: '#7940EC' },
  { label: '纪念日总数', value: 0, icon: 'Star', color: '#FF7B9C' },
  { label: 'AI会话总数', value: 0, icon: 'ChatDotRound', color: '#36C9A5' }
])

const cards = computed(() => stats.value)

// 图表实例引用
const trendChartRef = ref(null)
const pieChartRef = ref(null)
let trendChart = null
let pieChart = null

// 饼图配色
const pieColors = ['#409EFF', '#FF7B9C', '#7940EC', '#36C9A5', '#F5A623', '#909399']

// 加载统计数据
const loadStats = async () => {
  try {
    const data = await getStats()
    stats.value[0].value = data.userCount
    stats.value[1].value = data.scheduleCount
    stats.value[2].value = data.memorialCount
    stats.value[3].value = data.conversationCount
  } catch {
    // 错误已由拦截器提示
  }
}

// 加载趋势数据并渲染折线图
const loadTrend = async () => {
  try {
    const data = await getTrend()
    trendChart?.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['日程', '纪念日', 'AI会话'], bottom: 0 },
      grid: { left: '3%', right: '4%', bottom: '12%', top: '5%', containLabel: true },
      xAxis: {
        type: 'category',
        data: data.dates,
        boundaryGap: false
      },
      yAxis: { type: 'value' },
      series: [
        { name: '日程', type: 'line', smooth: true, data: data.scheduleCounts, itemStyle: { color: '#409EFF' } },
        { name: '纪念日', type: 'line', smooth: true, data: data.memorialCounts, itemStyle: { color: '#FF7B9C' } },
        { name: 'AI会话', type: 'line', smooth: true, data: data.chatCounts, itemStyle: { color: '#36C9A5' } }
      ]
    })
  } catch {
    // 错误已由拦截器提示
  }
}

// 加载日程类型分布并渲染饼图
const loadTypeDistribution = async () => {
  try {
    const data = await getScheduleTypeDistribution()
    pieChart?.setOption({
      tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
      legend: { bottom: 0 },
      series: [
        {
          name: '日程类型',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
          label: { show: false, position: 'center' },
          emphasis: {
            label: { show: true, fontSize: 16, fontWeight: 'bold' }
          },
          labelLine: { show: false },
          data: data.items.map((item, index) => ({
            name: item.name,
            value: item.value,
            itemStyle: { color: pieColors[index % pieColors.length] }
          }))
        }
      ]
    })
  } catch {
    // 错误已由拦截器提示
  }
}

// 初始化图表容器
const initCharts = () => {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
  }
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
  }
}

// 窗口缩放时重绘图表
const handleResize = () => {
  trendChart?.resize()
  pieChart?.resize()
}

onMounted(async () => {
  initCharts()
  // 并发加载三项数据
  await Promise.all([loadStats(), loadTrend(), loadTypeDistribution()])
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  pieChart?.dispose()
})
</script>

<template>
  <!-- 根容器复用 page-container 弹性列：统计行固定高，图表区 flex:1 填满剩余 -->
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

    <!-- 图表区：折线图 + 饼图 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="14">
        <div class="table-card chart-card">
          <div class="card-title">最近 7 天新增数据趋势</div>
          <div ref="trendChartRef" class="chart-canvas" />
        </div>
      </el-col>
      <el-col :span="10">
        <div class="table-card chart-card">
          <div class="card-title">日程类型分布</div>
          <div ref="pieChartRef" class="chart-canvas" />
        </div>
      </el-col>
    </el-row>
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

  /* 图表行不参与压缩 */
  .chart-row {
    flex-shrink: 0;
  }

  .chart-card {
    display: flex;
    flex-direction: column;

    .card-title {
      font-weight: 600;
      margin-bottom: 12px;
      flex-shrink: 0;
    }

    .chart-canvas {
      flex: 1;
      min-height: 320px;
    }
  }
}
</style>
