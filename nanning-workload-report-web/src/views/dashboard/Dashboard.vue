<!-- Dashboard.vue - 首页看板 -->
<template>
  <div class="dashboard-page">
    <h2 class="page-title">首页看板</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :xs="24" :sm="12" :md="6" v-for="card in statCards" :key="card.title">
        <el-card class="stat-card" shadow="hover">
          <div class="card-content">
            <div class="card-info">
              <span class="card-value">{{ card.value }}</span>
              <span class="card-unit" v-if="card.unit">{{ card.unit }}</span>
              <p class="card-title">{{ card.title }}</p>
            </div>
            <div class="card-icon" :style="{ background: card.color }">
              <el-icon :size="24" color="#fff"><Odometer /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表面板 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header><span>{{ workshopChartTitle }}</span></template>
          <div ref="workshopChartRef" style="height: 300px;" v-loading="loading"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header><span>项目工时占比</span></template>
          <div ref="projectChartRef" style="height: 300px;" v-loading="loading"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { useUserStore } from '@/store/user'
import { statisticsApi } from '@/api/statistics'

const userStore = useUserStore()
const loading = ref(false)

const statCards = ref([
  { title: '工时合计', value: '--', unit: '分钟', color: '#409eff' },
  { title: '工分合计', value: '--', unit: '分', color: '#67c23a' },
  { title: '涉及工区', value: '--', unit: '个', color: '#e6a23c' },
  { title: '填报记录', value: '--', unit: '条', color: '#f56c6c' }
])

const workshopChartTitle = computed(() => {
  if (userStore.isSectionAdmin) return '各车间工时对比'
  if (userStore.isWorkshopAdmin) return '各工区工时对比'
  return '工区工时数据'
})

const workshopChartRef = ref<HTMLDivElement>()
const projectChartRef = ref<HTMLDivElement>()
let workshopChart: echarts.ECharts | null = null
let projectChart: echarts.ECharts | null = null

onMounted(async () => {
  loading.value = true
  try {
    const data = await statisticsApi.getDashboard()
    updateCards(data)
    await nextTick()
    initCharts(data)
  } catch { /* 加载失败保持 -- */ }
  finally { loading.value = false }
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  workshopChart?.dispose()
  projectChart?.dispose()
})

function updateCards(data: any) {
  if (data.cards) {
    data.cards.forEach((card: any, idx: number) => {
      if (idx < statCards.value.length) {
        statCards.value[idx].value = card.value
        statCards.value[idx].unit = card.unit
      }
    })
  }
}

function initCharts(data: any) {
  // 工区/车间对比柱状图
  if (workshopChartRef.value) {
    workshopChart = echarts.init(workshopChartRef.value)
    const chartData = data.workshopChart || []
    workshopChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: chartData.map((d: any) => d.name) },
      yAxis: { type: 'value', name: '分钟' },
      series: [{
        data: chartData.map((d: any) => d.value),
        type: 'bar',
        itemStyle: { color: '#409eff' }
      }]
    })
  }

  // 项目占比饼图
  if (projectChartRef.value) {
    projectChart = echarts.init(projectChartRef.value)
    const chartData = data.projectChart || []
    projectChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: chartData.map((d: any) => ({ name: d.name, value: d.value }))
      }]
    })
  }
}

function handleResize() {
  workshopChart?.resize()
  projectChart?.resize()
}
</script>

<style scoped lang="scss">
.dashboard-page {
  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: #1a3a52;
    margin: 0 0 20px;
  }
}

.stat-cards { margin-bottom: 0; }

.stat-card {
  margin-bottom: 16px;
  .card-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .card-info {
      .card-value { font-size: 28px; font-weight: 700; color: #333; }
      .card-unit { font-size: 14px; color: #999; margin-left: 4px; }
      .card-title { margin: 4px 0 0; font-size: 13px; color: #999; }
    }
    .card-icon {
      width: 48px; height: 48px; border-radius: 8px;
      display: flex; align-items: center; justify-content: center;
    }
  }
}

.chart-card { margin-bottom: 16px; }
</style>
