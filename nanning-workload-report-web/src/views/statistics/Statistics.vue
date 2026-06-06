<!-- 统计分析页面 - 使用ECharts展示实时多维度统计数据 -->
<template>
  <div>
    <div class="page-header">
      <h2>统计分析</h2>
      <el-select v-model="periodId" placeholder="选择月份" clearable style="width:200px" @change="loadAll">
        <el-option v-for="p in periods" :key="p.id" :label="p.periodName" :value="p.id" />
      </el-select>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="6">
        <el-card><div class="stat-card">
          <div class="stat-value" style="color:#409eff">{{ statusStats.approved || 0 }}</div>
          <div class="stat-label">已审核(条)</div>
        </div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card><div class="stat-card">
          <div class="stat-value" style="color:#67c23a">{{ statusStats.submitted || 0 }}</div>
          <div class="stat-label">已提交(条)</div>
        </div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card><div class="stat-card">
          <div class="stat-value" style="color:#e6a23c">{{ statusStats.draft || 0 }}</div>
          <div class="stat-label">草稿(条)</div>
        </div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card><div class="stat-card">
          <div class="stat-value" style="color:#f56c6c">{{ statusStats.returned || 0 }}</div>
          <div class="stat-label">已退回(条)</div>
        </div></el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card>
          <template #header>车间/工区工时对比</template>
          <div ref="barChartRef" style="height:320px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>用工项目占比</template>
          <div ref="pieChartRef" style="height:320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card>
          <template #header>人员工时排名（TOP 10）</template>
          <div ref="employeeChartRef" style="height:320px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>月度趋势</template>
          <div ref="trendChartRef" style="height:320px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { statisticsApi } from '@/api/statistics'
import { periodApi } from '@/api/period'
import type { MonthlyPeriod } from '@/types'

const periods = ref<MonthlyPeriod[]>([])
const periodId = ref<number | null>(null)
const statusStats = ref<Record<string,number>>({})

// Charts
const barChartRef = ref(); const pieChartRef = ref()
const employeeChartRef = ref(); const trendChartRef = ref()
let barChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null
let employeeChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null

onMounted(async () => {
  try {
    const res = await periodApi.getPage({ pageNum: 1, pageSize: 100 })
    periods.value = res.records
    if (periods.value.length > 0) periodId.value = periods.value[0].id
  } catch { /* 忽略 */ }
  initCharts()
  loadAll()
})

onUnmounted(() => {
  barChart?.dispose(); pieChart?.dispose()
  employeeChart?.dispose(); trendChart?.dispose()
})

function initCharts() {
  barChart = echarts.init(barChartRef.value)
  pieChart = echarts.init(pieChartRef.value)
  employeeChart = echarts.init(employeeChartRef.value)
  trendChart = echarts.init(trendChartRef.value)
}

async function loadAll() {
  const params = periodId.value ? { periodId: periodId.value } : {}
  try {
    // 状态统计
    const statusData = await statisticsApi.getStatusStats(params)
    statusStats.value = statusData

    // 工区统计 → 柱状图
    const areaData = await statisticsApi.getByArea(params)
    barChart?.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: (areaData as any[]).map((a:any) => a.name), axisLabel: { rotate: 30 } },
      yAxis: { type: 'value', name: '分钟' },
      series: [{
        data: (areaData as any[]).map((a:any) => a.hours), type: 'bar',
        itemStyle: { color: '#409eff' }, barMaxWidth: 40
      }]
    }, true)

    // 项目统计 → 饼图
    const projData = await statisticsApi.getByProject(params)
    pieChart?.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie', radius: ['40%', '70%'],
        data: (projData as any[]).map((p:any) => ({ name: p.name, value: p.hours })),
        emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } }
      }]
    }, true)

    // 人员排名 → 横向柱状图
    const empData = await statisticsApi.getByEmployee(params)
    employeeChart?.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'value', name: '分钟' },
      yAxis: { type: 'category', data: (empData as any[]).map((e:any) => e.name).reverse(), inverse: true },
      series: [{
        data: (empData as any[]).map((e:any) => e.hours).reverse(), type: 'bar',
        itemStyle: { color: '#67c23a' }, barMaxWidth: 20
      }]
    }, true)

    // 趋势 → 折线图
    const trendData: any = await statisticsApi.getTrend(params)
    trendChart?.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['工时', '工分'] },
      xAxis: { type: 'category', data: trendData.months || [] },
      yAxis: { type: 'value' },
      series: [
        { name: '工时', data: trendData.hours || [], type: 'line', smooth: true, itemStyle: { color: '#409eff' } },
        { name: '工分', data: trendData.points || [], type: 'line', smooth: true, itemStyle: { color: '#67c23a' } }
      ]
    }, true)
  } catch { /* 数据为空时图表保持空白 */ }
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px }
.page-header h2 { margin: 0; font-size: 18px }
.stat-card { text-align: center }
.stat-value { font-size: 28px; font-weight: bold }
.stat-label { color: #999; margin-top: 4px }
</style>
