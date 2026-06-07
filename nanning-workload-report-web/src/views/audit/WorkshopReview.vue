<!-- 车间审核页面 — 查看本车间下属工区所有状态的填报记录 -->
<template>
  <div>
    <h2>车间审核</h2>
    <el-card style="margin-top:16px">
      <!-- 筛选栏 -->
      <el-form :inline="true" class="search-bar">
        <el-form-item label="月份">
          <el-select v-model="periodId" placeholder="选择月份" style="width:180px" @change="onPeriodChange">
            <el-option v-for="p in periods" :key="p.id" :label="p.periodName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="工区">
          <el-select v-model="filterAreaId" placeholder="全部工区" clearable style="width:180px" @change="onFilterChange">
            <el-option label="全部工区" value="" />
            <el-option v-for="a in areas" :key="a.id" :label="a.orgName" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterStatus" style="width:120px" @change="onFilterChange">
            <el-option label="待审核" value="已提交" />
            <el-option label="已审核" value="已审核" />
            <el-option label="已退回" value="已退回" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onFilterChange">查询</el-button>
        </el-form-item>
      </el-form>

      <div style="margin-bottom:8px;text-align:right">
        <el-button type="success" size="small" @click="handleBatchApprove">批量通过</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%" @selection-change="onSelectionChange">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="areaName" label="工区" width="150" />
        <el-table-column prop="employeeName" label="人员" width="100" />
        <el-table-column prop="workDate" label="日期" width="120" />
        <el-table-column prop="reportType" label="类别" width="80">
          <template #default="{ row }">{{ row.reportType === 'HOURS' ? '工时' : '工分' }}</template>
        </el-table-column>
        <el-table-column label="项目明细" min-width="200">
          <template #default="{ row }">{{ (row.items || []).map((i: any) => i.itemPath || i.itemName).join(', ') }}</template>
        </el-table-column>
        <el-table-column label="数值" width="100">
          <template #default="{ row }">
            <template v-if="row.reportType === 'HOURS'">{{ (row.items || []).reduce((s: number, i: any) => s + (i.numberValue || 0), 0) }} 分钟</template>
            <template v-else>{{ (row.items || []).reduce((s: number, i: any) => s + (i.pointsValue || 0), 0) }} 工分</template>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="statusType(row.status)">{{ row.status === '已提交' ? '待审核' : row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === '已提交'">
              <el-button link type="success" size="small" @click="approve(row)">通过</el-button>
              <el-button link type="danger" size="small" @click="showReturn(row)">退回</el-button>
            </template>
            <span v-else style="color:#999;font-size:12px">—</span>
          </template>
        </el-table-column>
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

      <el-empty v-if="!loading && tableData.length === 0" description="该月份暂无填报数据" />
    </el-card>

    <!-- 退回原因对话框 -->
    <el-dialog title="退回原因" v-model="returnVisible" width="420px">
      <el-input v-model="returnComment" type="textarea" :rows="3" placeholder="请填写退回原因（必填）" />
      <template #footer>
        <el-button @click="returnVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReturn" :loading="saving">确认退回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditApi } from '@/api/audit'
import { periodApi } from '@/api/period'
import { orgApi } from '@/api/org'
import { useUserStore } from '@/store/user'
import type { WorkReport, MonthlyPeriod, OrgUnit } from '@/types'

const userStore = useUserStore()

const periods = ref<MonthlyPeriod[]>([])
const areas = ref<OrgUnit[]>([])
const periodId = ref<number | null>(null)
const filterAreaId = ref<string | number>('')
const filterStatus = ref('已提交')
const tableData = ref<WorkReport[]>([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const saving = ref(false)
const returnVisible = ref(false)
const returnComment = ref('')
const currentReturnId = ref<number | null>(null)

// 批量选择
const selectedReports = ref<WorkReport[]>([])
const canBatchApprove = computed(() => selectedReports.value.length > 0 && selectedReports.value.every(r => r.status === '已提交'))

function statusType(s: string) {
  const m: Record<string, string> = { '已提交': 'warning', '已审核': 'success', '已退回': 'danger' }
  return m[s] || 'info'
}

onMounted(async () => {
  // 加载工区列表
  if (userStore.orgId) {
    try {
      areas.value = await orgApi.getAreasByWorkshopId(userStore.orgId)
    } catch { /* 忽略 */ }
  }

  // 加载月份
  try {
    const pRes = await periodApi.getActive()
    if (pRes) {
      periods.value = [pRes]
      periodId.value = pRes.id
    } else {
      const pageResult = await periodApi.getPage({ pageNum: 1, pageSize: 100 })
      periods.value = pageResult.records
      if (periods.value.length > 0) periodId.value = periods.value[0].id
    }
  } catch { /* 忽略 */ }

  if (periodId.value) loadData()
})

async function loadData() {
  if (!periodId.value) return
  loading.value = true
  try {
    const res = await auditApi.getAllReportsPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      periodId: periodId.value,
      status: filterStatus.value || undefined,
      areaId: filterAreaId.value || undefined
    })
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function onPeriodChange() {
  pageNum.value = 1
  loadData()
}

function onFilterChange() {
  pageNum.value = 1
  loadData()
}

function handleSizeChange() {
  pageNum.value = 1
  loadData()
}

function onSelectionChange(rows: WorkReport[]) {
  selectedReports.value = rows
}

async function handleBatchApprove() {
  if (selectedReports.value.length === 0) {
    ElMessage.warning('请先勾选要审核的记录')
    return
  }
  if (!canBatchApprove.value) {
    ElMessage.warning('只能批量通过"待审核"状态的记录')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要批量通过选中的 ${selectedReports.value.length} 条记录吗？`, '提示', { type: 'warning' })
    const ids = selectedReports.value.map(r => r.id)
    await auditApi.batchApprove(ids)
    ElMessage.success(`成功通过 ${ids.length} 条记录`)
    selectedReports.value = []
    loadData()
  } catch { /* 用户取消 */ }
}

async function approve(row: WorkReport) {
  try {
    await auditApi.approve(row.id)
    ElMessage.success('审核通过')
    loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

function showReturn(row: WorkReport) {
  currentReturnId.value = row.id
  returnComment.value = ''
  returnVisible.value = true
}

async function handleReturn() {
  if (!returnComment.value.trim()) { ElMessage.warning('请填写退回原因'); return }
  saving.value = true
  try {
    await auditApi.returnReport(currentReturnId.value!, returnComment.value)
    ElMessage.success('已退回')
    returnVisible.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.search-bar { margin-bottom: 8px }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px }
</style>
