<!--
  Dashboard.vue - 首页看板
  展示当前月份的填报进度和汇总情况
  不同角色看到不同范围的数据
-->
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

    <!-- 第二个卡片行 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :xs="24" :sm="12" :md="6" v-for="card in statusCards" :key="card.title">
        <el-card class="stat-card" shadow="hover">
          <div class="card-content">
            <div class="card-info">
              <span class="card-value">{{ card.value }}</span>
              <p class="card-title">{{ card.title }}</p>
            </div>
            <div class="card-icon" :style="{ background: card.color }">
              <el-icon :size="24" color="#fff"><Checked /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表面板 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header>
            <span>{{ workshopChartTitle }}</span>
          </template>
          <div ref="workshopChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header>
            <span>项目工时占比</span>
          </template>
          <div ref="projectChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 未提交列表 -->
    <el-row :gutter="16" style="margin-top: 16px;" v-if="unsubmittedList.length > 0">
      <el-col :span="24">
        <el-card class="chart-card">
          <template #header>
            <span style="color: #e6a23c;">⚠ 未提交提醒</span>
          </template>
          <el-table :data="unsubmittedList" size="small" border stripe>
            <el-table-column prop="name" label="组织名称" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="row.tagType" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { useUserStore } from '@/store/user'
import { statisticsApi } from '@/api/statistics'

const userStore = useUserStore()

/** 统计卡片数据 */
const statCards = ref([
  { title: '当前月份总工时', value: '--', unit: '分钟', color: '#409eff' },
  { title: '当前月份总工分', value: '--', unit: '分', color: '#67c23a' },
  { title: '已提交工区数', value: '--', unit: '个', color: '#e6a23c' },
  { title: '已审核通过数', value: '--', unit: '条', color: '#f56c6c' }
])

/** 状态统计卡片 */
const statusCards = ref([
  { title: '未提交', value: '--', color: '#909399' },
  { title: '待审核', value: '--', color: '#e6a23c' },
  { title: '已退回', value: '--', color: '#f56c6c' },
  { title: '填报进度', value: '--', color: '#409eff' }
])

/** 未提交列表 */
const unsubmittedList = ref<any[]>([])

/** 车间图标题 */
const workshopChartTitle = computed(() =>
  userStore.isSectionAdmin ? '各车间工时对比' : '下属工区工时对比'
)

/** ECharts 图表容器引用 */
const workshopChartRef = ref<HTMLDivElement>()
const projectChartRef = ref<HTMLDivElement>()

let workshopChart: echarts.ECharts | null = null
let projectChart: echarts.ECharts | null = null

/**
 * 加载仪表盘数据
 */
async function loadDashboard() {
  try {
    const data = await statisticsApi.getDashboard()
    if (data.cards) {
      data.cards.forEach((card: any, idx: number) => {
        if (idx < statCards.value.length) {
          statCards.value[idx].value = card.value
        }
      })
    }
  } catch {
    // 使用演示数据
    statCards.value[0].value = '12,580'
    statCards.value[1].value = '3,260'
    statCards.value[2].value = '6'
    statCards.value[3].value = '45'
    statusCards.value[0].value = '2个工区'
    statusCards.value[1].value = '8条'
    statusCards.value[2].value = '3条'
    statusCards.value[3].value = '75%'
  }
}

/**
 * 初始化图表
 */
function initCharts() {
  if (workshopChartRef.value) {
    workshopChart = echarts.init(workshopChartRef.value)
    workshopChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: ['车间A', '车间B', '车间C'] },
      yAxis: { type: 'value', name: '分钟' },
      series: [{ data: [3200, 2800, 1900], type: 'bar', itemStyle: { color: '#409eff' } }]
    })
  }

  if (projectChartRef.value) {
    projectChart = echarts.init(projectChartRef.value)
    projectChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { name: '施工', value: 3500 },
          { name: '培训', value: 1800 },
          { name: '维修', value: 2200 },
          { name: '故障处理', value: 1500 },
          { name: '其他', value: 2800 }
        ]
      }]
    })
  }
}

/**
 * 窗口尺寸变化时重绘图表
 */
function handleResize() {
  workshopChart?.resize()
  projectChart?.resize()
}

onMounted(() => {
  loadDashboard()
  initCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  workshopChart?.dispose()
  projectChart?.dispose()
})
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

.stat-cards {
  margin-bottom: 0;
}

.stat-card {
  margin-bottom: 16px;

  .card-content {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .card-info {
      .card-value {
        font-size: 28px;
        font-weight: 700;
        color: #333;
      }

      .card-unit {
        font-size: 14px;
        color: #999;
        margin-left: 4px;
      }

      .card-title {
        margin: 4px 0 0;
        font-size: 13px;
        color: #999;
      }
    }

    .card-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}

.chart-card {
  margin-bottom: 16px;
}
</style>
