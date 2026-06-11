<!-- 审核记录列表页面 -->
<template>
  <div>
    <h2>审核记录</h2>
    <el-card style="margin-top:16px">
      <!-- 筛选栏 -->
      <el-form :inline="true" class="search-bar">
        <el-form-item label="月份">
          <el-select v-model="periodId" placeholder="选择月份" clearable style="width:180px" @change="handleSearch">
            <el-option v-for="p in periods" :key="p.id" :label="p.periodName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="工区" v-if="areaOptions.length > 0">
          <el-select v-model="filterAreaId" placeholder="全部工区" clearable style="width:150px" @change="handleSearch">
            <el-option v-for="a in areaOptions" :key="a.id" :label="a.orgName" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作">
          <el-select v-model="filterAction" placeholder="全部操作" clearable style="width:120px" @change="handleSearch">
            <el-option label="提交" value="SUBMIT" />
            <el-option label="通过" value="APPROVE" />
            <el-option label="退回" value="RETURN" />
            <el-option label="锁定" value="LOCK" />
            <el-option label="解锁" value="UNLOCK" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column label="时间" width="180">
          <template #default="{ row }">{{ formatTime(row.operateTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }"><el-tag size="small">{{ actionMap[row.action] || row.action }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="comment" label="备注" min-width="200" />
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadData"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { auditApi } from '@/api/audit'
import { periodApi } from '@/api/period'
import { orgApi } from '@/api/org'
import { useUserStore } from '@/store/user'
import type { AuditRecord, MonthlyPeriod, OrgUnit } from '@/types'

const userStore = useUserStore()
const actionMap: Record<string, string> = { SUBMIT: '提交', APPROVE: '通过', RETURN: '退回', LOCK: '锁定', UNLOCK: '解锁' }
const tableData = ref<AuditRecord[]>([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const periods = ref<MonthlyPeriod[]>([])
const periodId = ref<number | null>(null)
const filterAction = ref('')
const filterAreaId = ref<number | null>(null)
const areaOptions = ref<OrgUnit[]>([])

function formatTime(t: string) {
  if (!t) return '-'
  return t.replace('T', ' ')
}

onMounted(async () => {
  try {
    const res = await periodApi.getPage({ pageNum: 1, pageSize: 100 })
    periods.value = res.records
  } catch { /* 忽略 */ }
  // 车间管理员加载本车间工区
  if (userStore.isWorkshopAdmin && userStore.orgId) {
    try { areaOptions.value = await orgApi.getAreasByWorkshopId(userStore.orgId) } catch { /* 忽略 */ }
  }
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await auditApi.getAuditRecords({
      pageNum: pageNum.value, pageSize: pageSize.value,
      periodId: periodId.value || undefined,
      action: filterAction.value || undefined,
      areaId: filterAreaId.value || undefined
    })
    tableData.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

function handleSearch() { pageNum.value = 1; loadData() }
function handleReset() {
  periodId.value = null; filterAction.value = ''; filterAreaId.value = null
  pageNum.value = 1; loadData()
}
function handleSizeChange() { pageNum.value = 1; loadData() }
</script>

<style scoped>
.search-bar { margin-bottom: 8px }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px }
</style>
