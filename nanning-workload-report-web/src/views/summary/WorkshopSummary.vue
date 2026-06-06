<!--
  车间汇总页面
  功能：按工区分组汇总指定月份的工时工分数据
  权限：段级管理员（可选车间）、车间管理员（只能看本车间）
  仅统计状态为"已审核"或"已锁定"的填报数据
-->
<template>
  <div>
    <div class="page-header">
      <h2>车间汇总</h2>
    </div>

    <el-card>
      <!-- 筛选条件 -->
      <el-form :inline="true" class="search-bar">
        <el-form-item label="统计月份" required>
          <el-select v-model="periodId" placeholder="选择月份" style="width:200px" @change="loadData">
            <el-option v-for="p in periods" :key="p.id" :label="p.periodName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isSectionAdmin" label="车间">
          <el-select v-model="selectedWorkshopId" placeholder="选择车间" style="width:200px" clearable filterable @change="loadData">
            <el-option v-for="w in workshops" :key="w.id" :label="w.orgName" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" :loading="loading">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 合计卡片 -->
      <el-row :gutter="16" v-if="summaryData.length > 0" style="margin-bottom:16px">
        <el-col :span="6">
          <el-statistic title="工区数量" :value="summaryData.length" suffix="个" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="工时合计" :value="totalStats.totalMinutes" suffix="分钟">
            <template #prefix>{{ formatHours(totalStats.totalMinutes) }}</template>
          </el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="工分合计" :value="totalStats.totalPoints" suffix="分" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="填报人次" :value="totalStats.totalReports" suffix="条" />
        </el-col>
      </el-row>

      <!-- 数据表格 -->
      <el-table :data="summaryData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="areaName" label="工区" min-width="150" />
        <el-table-column prop="reportCount" label="填报记录" width="100" sortable />
        <el-table-column prop="employeeCount" label="涉及人数" width="100" sortable />
        <el-table-column label="施工工时" width="120" sortable prop="constructionMinutes">
          <template #default="{row}">{{ fmt(row.constructionMinutes) }}</template>
        </el-table-column>
        <el-table-column label="施工配合" width="120" sortable prop="cooperationMinutes">
          <template #default="{row}">{{ fmt(row.cooperationMinutes) }}</template>
        </el-table-column>
        <el-table-column label="维修工时" width="120" sortable prop="maintenanceMinutes">
          <template #default="{row}">{{ fmt(row.maintenanceMinutes) }}</template>
        </el-table-column>
        <el-table-column label="其他工时" width="120" sortable prop="otherMinutes">
          <template #default="{row}">{{ fmt(row.otherMinutes) }}</template>
        </el-table-column>
        <el-table-column label="工时合计" width="130" sortable prop="totalMinutes">
          <template #default="{row}">
            <span style="font-weight:bold;color:#409EFF">{{ fmt(row.totalMinutes) }} 分钟</span>
            <div style="font-size:12px;color:#999">≈ {{ formatHours(row.totalMinutes) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="工分合计" width="110" sortable prop="totalPoints">
          <template #default="{row}">
            <span style="font-weight:bold;color:#67C23A">{{ fmt(row.totalPoints) }} 分</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 无数据提示 -->
      <el-empty v-if="!loading && summaryData.length === 0 && periodId" description="该月份暂无已审核的填报数据" />
      <el-empty v-if="!periodId" description="请选择统计月份后点击查询" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { summaryApi } from '@/api/summary'
import { periodApi } from '@/api/period'
import { orgApi } from '@/api/org'
import { useUserStore } from '@/store/user'
import type { WorkshopSummary, MonthlyPeriod, OrgUnit } from '@/types'

const userStore = useUserStore()
const isSectionAdmin = computed(() => userStore.isSectionAdmin)

const periods = ref<MonthlyPeriod[]>([])
const workshops = ref<OrgUnit[]>([])
const summaryData = ref<WorkshopSummary[]>([])
const loading = ref(false)
const periodId = ref<number | null>(null)
const selectedWorkshopId = ref<number | null>(null)

/** 汇总统计 */
const totalStats = computed(() => {
  const s = summaryData.value
  return {
    totalMinutes: s.reduce((a, b) => a + (b.totalMinutes || 0), 0),
    totalPoints: s.reduce((a, b) => a + (b.totalPoints || 0), 0),
    totalReports: s.reduce((a, b) => a + (b.reportCount || 0), 0)
  }
})

/** 数值格式化 */
function fmt(v: number) { return (v || 0).toFixed(1) }
function formatHours(minutes: number) {
  if (!minutes) return '0小时'
  const h = Math.floor(minutes / 60)
  const m = Math.round(minutes % 60)
  return h > 0 ? `${h}小时${m > 0 ? m + '分' : ''}` : `${m}分`
}

onMounted(async () => {
  try {
    const [pList, wList] = await Promise.all([
      periodApi.getPage({ pageNum: 1, pageSize: 100 }),
      orgApi.getWorkshops()
    ])
    periods.value = pList.records
    workshops.value = wList
    // 默认选择最新月份
    if (periods.value.length > 0) periodId.value = periods.value[0].id
  } catch (e: any) {
    ElMessage.error('加载基础数据失败')
  }
  if (periodId.value) loadData()
})

async function loadData() {
  if (!periodId.value) { ElMessage.warning('请选择统计月份'); return }
  loading.value = true
  try {
    summaryData.value = await summaryApi.getWorkshopSummary(
      periodId.value,
      selectedWorkshopId.value || undefined
    )
  } catch (e: any) {
    ElMessage.error(e.message || '查询失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px }
.page-header h2 { margin: 0; font-size: 18px }
.search-bar { margin-bottom: 8px }
</style>
