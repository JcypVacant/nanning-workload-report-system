<!-- 车间审核页面 — 查看本车间下属工区所有状态的填报记录 -->
<template>
  <div>
    <h2>车间审核</h2>
    <el-card style="margin-top:16px">
      <template #header>
        <div class="card-header">
          <span>审核列表</span>
          <el-button type="warning" size="small" @click="handleSubmitToSection">提交到段级</el-button>
        </div>
      </template>
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
            <el-option label="已锁定" value="已锁定" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onFilterChange">查询</el-button>
        </el-form-item>
      </el-form>

      <div style="margin-bottom:8px;text-align:right;display:flex;gap:8px;justify-content:flex-end">
        <el-button type="danger" size="small" @click="handleBatchReturn">批量退回</el-button>
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
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === '已提交'">
              <el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button>
              <el-button link type="success" size="small" @click="approve(row)">通过</el-button>
              <el-button link type="danger" size="small" @click="showReturn(row)">退回</el-button>
            </template>
            <template v-else>
              <el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button>
            </template>
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

    <!-- 未填报提醒 -->
    <el-card style="margin-top:16px" v-if="unsubmittedList.length > 0">
      <template #header><span style="color:#e6a23c">⚠ 未填报提醒（{{ unsubmittedList.length }}人）</span></template>
      <el-table :data="unsubmittedPage" size="small" border stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="employeeName" label="人员姓名" width="120" />
        <el-table-column prop="areaName" label="所属工区" min-width="150" />
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="unsubPageNum"
          v-model:page-size="unsubPageSize"
          :page-sizes="[10, 20, 50]"
          :total="unsubmittedList.length"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog title="填报详情" v-model="detailVisible" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="人员">{{ detailReport?.employeeName }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ detailReport?.workDate }}</el-descriptions-item>
        <el-descriptions-item label="类别">{{ detailReport?.reportType === 'HOURS' ? '工时' : '工分' }}</el-descriptions-item>
        <el-descriptions-item label="工区">{{ detailReport?.areaName }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="detailReport?.items || []" border stripe style="margin-top:12px">
        <el-table-column label="用工项目" min-width="200">
          <template #default="{ row }">{{ row.itemPath || row.itemName }}</template>
        </el-table-column>
        <el-table-column label="数值" width="120">
          <template #default="{ row }">
            <template v-if="detailReport?.reportType === 'HOURS'">{{ row.numberValue || 0 }} 分钟</template>
            <template v-else>{{ row.pointsValue || 0 }} 工分</template>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" width="150" />
      </el-table>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 退回原因对话框 -->
    <el-dialog :title="batchReturnMode ? '批量退回原因' : '退回原因'" v-model="returnVisible" width="420px">
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
const batchReturnMode = ref(false)

// 批量选择
const selectedReports = ref<WorkReport[]>([])
const canBatchApprove = computed(() => selectedReports.value.length > 0 && selectedReports.value.every(r => r.status === '已提交'))

// 查看详情
const detailVisible = ref(false)
const detailReport = ref<WorkReport | null>(null)

// 未填报
const unsubmittedList = ref<any[]>([])
const unsubPageNum = ref(1)
const unsubPageSize = ref(10)
const unsubmittedPage = computed(() => {
  const start = (unsubPageNum.value - 1) * unsubPageSize.value
  return unsubmittedList.value.slice(start, start + unsubPageSize.value)
})

function statusType(s: string) {
  const m: Record<string, string> = { '已提交': 'warning', '已审核': 'success', '已退回': 'danger', '已锁定': 'info' }
  return m[s] || 'info'
}

onMounted(async () => {
  if (userStore.orgId) {
    try { areas.value = await orgApi.getAreasByWorkshopId(userStore.orgId) } catch { /* 忽略 */ }
  }
  try {
    const pRes = await periodApi.getActive()
    if (pRes) {
      periods.value = [pRes]; periodId.value = pRes.id
    } else {
      const pageResult = await periodApi.getPage({ pageNum: 1, pageSize: 100 })
      periods.value = pageResult.records
      if (periods.value.length > 0) periodId.value = periods.value[0].id
    }
  } catch { /* 忽略 */ }
  if (periodId.value) {
    loadData()
    loadUnsubmitted()
  }
})

async function loadData() {
  if (!periodId.value) return
  loading.value = true
  try {
    const res = await auditApi.getAllReportsPage({
      pageNum: pageNum.value, pageSize: pageSize.value, periodId: periodId.value,
      status: filterStatus.value || undefined, areaId: filterAreaId.value || undefined
    })
    tableData.value = res.records; total.value = res.total
  } finally { loading.value = false }
}

async function loadUnsubmitted() {
  if (!periodId.value) return
  try { unsubmittedList.value = await auditApi.getUnsubmitted(periodId.value) } catch { /* 忽略 */ }
}

function onPeriodChange() { pageNum.value = 1; loadData(); loadUnsubmitted() }
function onFilterChange() { pageNum.value = 1; loadData() }
function handleSizeChange() { pageNum.value = 1; loadData() }

function onSelectionChange(rows: WorkReport[]) { selectedReports.value = rows }

async function handleBatchApprove() {
  if (selectedReports.value.length === 0) { ElMessage.warning('请先勾选要审核的记录'); return }
  if (!canBatchApprove.value) { ElMessage.warning('只能批量通过"待审核"状态的记录'); return }
  try {
    await ElMessageBox.confirm(`确定要批量通过选中的 ${selectedReports.value.length} 条记录吗？`, '提示', { type: 'warning' })
    await auditApi.batchApprove(selectedReports.value.map(r => r.id))
    ElMessage.success('批量通过成功')
    selectedReports.value = []; loadData(); loadUnsubmitted()
  } catch { /* 用户取消 */ }
}

async function handleBatchReturn() {
  if (selectedReports.value.length === 0) { ElMessage.warning('请先勾选要退回的记录'); return }
  if (!selectedReports.value.every(r => r.status === '已提交')) { ElMessage.warning('只能退回"待审核"状态的记录'); return }
  batchReturnMode.value = true
  currentReturnId.value = null
  returnComment.value = ''
  returnVisible.value = true
}

async function approve(row: WorkReport) {
  try { await auditApi.approve(row.id); ElMessage.success('审核通过'); loadData(); loadUnsubmitted() } catch (e: any) { ElMessage.error(e.message || '操作失败') }
}

function showDetail(row: WorkReport) { detailReport.value = row; detailVisible.value = true }

function showReturn(row: WorkReport) {
  batchReturnMode.value = false
  currentReturnId.value = row.id; returnComment.value = ''; returnVisible.value = true
}

async function handleSubmitToSection() {
  if (!periodId.value) { ElMessage.warning('请先选择月份'); return }
  try {
    await ElMessageBox.confirm('确定要将本车间该月份所有已审核的记录提交到段级吗？', '确认提交', { type: 'warning' })
    await auditApi.submitToSection(periodId.value)
    ElMessage.success('已提交到段级'); loadData()
  } catch { /* 用户取消 */ }
}

async function handleReturn() {
  if (!returnComment.value.trim()) { ElMessage.warning('请填写退回原因'); return }
  saving.value = true
  try {
    if (batchReturnMode.value) {
      await auditApi.batchReturn(selectedReports.value.map(r => r.id), returnComment.value)
      ElMessage.success(`已批量退回 ${selectedReports.value.length} 条记录`)
      selectedReports.value = []
    } else {
      await auditApi.returnReport(currentReturnId.value!, returnComment.value)
      ElMessage.success('已退回')
    }
    returnVisible.value = false; loadData(); loadUnsubmitted()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') } finally { saving.value = false }
}
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center }
.search-bar { margin-bottom: 8px }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px }
</style>
