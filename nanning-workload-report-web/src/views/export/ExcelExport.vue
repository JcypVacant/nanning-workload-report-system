<!-- Excel导出页面 - 支持工区/车间/全段三级导出 -->
<template>
  <div>
    <h2>Excel 导出</h2>
    <el-card style="margin-top:16px;max-width:700px">
      <el-form label-width="100px">
        <el-form-item label="导出范围">
          <el-select v-model="scope" style="width:240px" @change="onScopeChange">
            <el-option label="导出本工区数据" value="area" v-if="userStore.isAreaReporter" />
            <el-option label="导出指定工区" value="area" v-if="userStore.isSectionAdmin" />
            <el-option label="导出本车间数据" value="workshop" v-if="userStore.isWorkshopAdmin" />
            <el-option label="导出指定车间" value="workshop" v-if="userStore.isSectionAdmin" />
            <el-option label="导出全段数据" value="section" v-if="userStore.isSectionAdmin" />
          </el-select>
        </el-form-item>

        <!-- 段级管理员选择具体工区 -->
        <el-form-item v-if="scope==='area' && userStore.isSectionAdmin" label="选择工区">
          <el-select v-model="selectedAreaId" placeholder="选择工区" style="width:240px" filterable>
            <el-option v-for="a in areas" :key="a.id" :label="a.orgName" :value="a.id" />
          </el-select>
        </el-form-item>

        <!-- 段级管理员选择具体车间 -->
        <el-form-item v-if="scope==='workshop' && userStore.isSectionAdmin" label="选择车间">
          <el-select v-model="selectedWorkshopId" placeholder="选择车间" style="width:240px" filterable>
            <el-option v-for="w in workshops" :key="w.id" :label="w.orgName" :value="w.id" />
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
          <li>导出内容包含：序号、车间、工区、姓名、日期、类别、用工项目、工时、工分、备注、状态</li>
          <li>文件名格式：xxx填报数据_时间戳.xlsx</li>
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
const areas = ref<OrgUnit[]>([])
const periodId = ref<number | null>(null)
const exporting = ref(false)

// 导出范围选择
const scope = ref(userStore.isAreaReporter ? 'area' : userStore.isWorkshopAdmin ? 'workshop' : 'section')
const selectedWorkshopId = ref<number | null>(null)
const selectedAreaId = ref<number | null>(null)

onMounted(async () => {
  try {
    const [pRes] = await Promise.all([
      periodApi.getPage({ pageNum: 1, pageSize: 100 }),
      orgApi.getWorkshops().then(w => { workshops.value = w }),
      orgApi.getAllAreas().then(a => { areas.value = a })
    ])
    periods.value = pRes.records
    if (periods.value.length > 0) periodId.value = periods.value[0].id
  } catch { /* 忽略 */ }
})

function onScopeChange() {
  selectedWorkshopId.value = null
  selectedAreaId.value = null
}

async function handleExport() {
  if (!periodId.value) { ElMessage.warning('请选择统计月份'); return }

  // 确定orgId
  let orgId = userStore.orgId || 0
  if (scope.value === 'area' && userStore.isSectionAdmin) {
    if (!selectedAreaId.value) { ElMessage.warning('请选择工区'); return }
    orgId = selectedAreaId.value
  }
  if (scope.value === 'workshop' && userStore.isSectionAdmin) {
    if (!selectedWorkshopId.value) { ElMessage.warning('请选择车间'); return }
    orgId = selectedWorkshopId.value
  }

  exporting.value = true
  try {
    let blob: Blob
    if (scope.value === 'area') {
      blob = await exportApi.exportArea(orgId, periodId.value)
    } else if (scope.value === 'workshop') {
      blob = await exportApi.exportWorkshop(orgId, periodId.value)
    } else {
      blob = await exportApi.exportSection(periodId.value)
    }
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `填报数据导出_${new Date().toISOString().slice(0,10)}.xlsx`
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
