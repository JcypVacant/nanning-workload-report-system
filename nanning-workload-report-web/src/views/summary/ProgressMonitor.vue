<!-- 填报进度管理 - 段级管理员查看各车间填报进度 -->
<template>
  <div>
    <h2>填报进度管理</h2>
    <el-card style="margin-top:16px">
      <el-form :inline="true" class="search-bar">
        <el-form-item label="统计月份" required>
          <el-select v-model="periodId" placeholder="选择月份" style="width:200px" @change="loadData">
            <el-option v-for="p in periods" :key="p.id" :label="p.periodName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" :loading="loading">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="workshopName" label="车间" min-width="160" />
        <el-table-column prop="totalEmployees" label="总人数" width="80" />
        <el-table-column label="提交进度" width="200">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:8px">
              <el-progress :percentage="row.submittedPercent" :color="row.submittedPercent===100?'#67c23a':'#409eff'" style="flex:1" />
              <span style="font-size:12px;white-space:nowrap">{{ row.submitted }}/{{ row.totalEmployees }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="审核进度" width="200">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:8px">
              <el-progress :percentage="row.approvedPercent" color="#67c23a" style="flex:1" />
              <span style="font-size:12px;white-space:nowrap">{{ row.approved }}/{{ row.totalEmployees }}</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && tableData.length === 0" description="暂无数据" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { summaryApi } from '@/api/summary'
import { periodApi } from '@/api/period'
import type { MonthlyPeriod } from '@/types'

const periods = ref<MonthlyPeriod[]>([])
const periodId = ref<number | null>(null)
const tableData = ref<any[]>([])
const loading = ref(false)

onMounted(async () => {
  try {
    const res = await periodApi.getPage({ pageNum: 1, pageSize: 100 })
    periods.value = res.records
    if (periods.value.length > 0) periodId.value = periods.value[0].id
  } catch { /* 忽略 */ }
  if (periodId.value) loadData()
})

async function loadData() {
  if (!periodId.value) return
  loading.value = true
  try {
    tableData.value = await summaryApi.getProgress(periodId.value)
  } finally { loading.value = false }
}
</script>

<style scoped>
.search-bar { margin-bottom: 8px }
</style>
