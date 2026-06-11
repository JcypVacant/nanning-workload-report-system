<!-- Excel导出页面 -->
<template>
  <div>
    <h2>Excel 导出</h2>
    <el-card style="margin-top:16px;max-width:700px">
      <el-form label-width="100px">
        <!-- 段级管理员：选车间 -->
        <el-form-item v-if="userStore.isSectionAdmin" label="选择车间">
          <el-select v-model="selectedWorkshopId" placeholder="选择车间" style="width:240px" filterable @change="onWorkshopChange">
            <el-option v-for="w in workshops" :key="w.id" :label="w.orgName" :value="w.id" />
          </el-select>
        </el-form-item>

        <!-- 选择工区（段级/车间管理员） -->
        <el-form-item v-if="!userStore.isAreaReporter" label="选择工区">
          <el-select v-model="selectedAreaId" placeholder="选择工区" style="width:240px" filterable>
            <el-option v-for="a in areaOptions" :key="a.id" :label="a.orgName" :value="a.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="统计月份" required>
          <el-select v-model="periodId" placeholder="选择月份" style="width:240px">
            <el-option v-for="p in periods" :key="p.id" :label="p.periodName" :value="p.id" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleExport" :loading="exporting">
            <el-icon><Download /></el-icon> 导出 Excel
          </el-button>
        </el-form-item>
      </el-form>

      <el-divider />
      <el-alert type="info" :closable="false" show-icon>
        <template #title>导出说明</template>
        <ul style="margin:4px 0;padding-left:18px;font-size:13px">
          <li>仅导出状态为「已审核」或「已锁定」的填报数据</li>
          <li>文件名格式：XX工区填报数据_日期.xlsx</li>
        </ul>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { exportApi } from '@/api/export'
import { periodApi } from '@/api/period'
import { orgApi } from '@/api/org'
import { useUserStore } from '@/store/user'
import type { MonthlyPeriod, OrgUnit } from '@/types'

const userStore = useUserStore()
const periods = ref<MonthlyPeriod[]>([])
const workshops = ref<OrgUnit[]>([])
const areaOptions = ref<OrgUnit[]>([])
const periodId = ref<number | null>(null)
const exporting = ref(false)
const selectedWorkshopId = ref<number | null>(null)
const selectedAreaId = ref<number | null>(null)

onMounted(async () => {
  try {
    const [pRes] = await Promise.all([
      periodApi.getPage({ pageNum: 1, pageSize: 100 }),
      userStore.isSectionAdmin ? orgApi.getWorkshops().then(w => { workshops.value = w }) : Promise.resolve()
    ])
    periods.value = pRes.records
    if (periods.value.length > 0) periodId.value = periods.value[0].id
  } catch { /* 忽略 */ }

  // 车间管理员：加载本车间工区
  if (userStore.isWorkshopAdmin && userStore.orgId) {
    try {
      areaOptions.value = await orgApi.getAreasByWorkshopId(userStore.orgId)
      const defaultArea = areaOptions.value.find(a => a.orgName.includes('本级'))
      if (defaultArea) selectedAreaId.value = defaultArea.id
    } catch { /* 忽略 */ }
  }
})

async function onWorkshopChange(val: number | null) {
  selectedAreaId.value = null
  areaOptions.value = []
  if (val) {
    try { areaOptions.value = await orgApi.getAreasByWorkshopId(val) } catch { /* 忽略 */ }
  }
}

async function handleExport() {
  if (!periodId.value) { ElMessage.warning('请选择统计月份'); return }

  let areaId: number
  let areaName = ''
  if (userStore.isAreaReporter) {
    areaId = userStore.orgId!
    areaName = userStore.userInfo?.orgName || '工区'
  } else {
    if (!selectedAreaId.value) { ElMessage.warning('请选择工区'); return }
    areaId = selectedAreaId.value
    const a = areaOptions.value.find(o => o.id === selectedAreaId.value)
    areaName = a?.orgName || '工区'
  }

  exporting.value = true
  try {
    const blob = await exportApi.exportArea(areaId, periodId.value)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    const d = new Date()
    const ts = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
    a.download = `${areaName}填报数据_${ts}.xlsx`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e: any) {
    ElMessage.error(e.message || '导出失败')
  } finally {
    exporting.value = false
  }
}
</script>
