<!-- 段级审核页面 — 审核车间本级提交的数据 -->
<template>
  <div>
    <h2>段级审核</h2>
    <el-card style="margin-top:16px">
      <!-- 筛选栏 -->
      <el-form :inline="true" class="search-bar">
        <el-form-item label="月份">
          <el-select v-model="periodId" placeholder="选择月份" style="width:180px" @change="onPeriodChange">
            <el-option v-for="p in periods" :key="p.id" :label="p.periodName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="车间">
          <el-select v-model="filterWorkshopId" placeholder="全部车间" clearable style="width:200px" @change="onFilterChange">
            <el-option v-for="w in workshops" :key="w.id" :label="w.orgName" :value="w.id" />
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

      <div style="margin-bottom:8px;text-align:right;display:flex;gap:8px;justify-content:flex-end">
        <el-button type="danger" size="small" @click="handleBatchReturn">批量退回</el-button>
        <el-button type="success" size="small" @click="handleBatchApprove">批量通过</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%" @selection-change="onSelectionChange">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="workshopName" label="车间" width="150" />
        <el-table-column label="工区" width="150">
          <template #default="{ row }">{{ row.areaName || '车间本级' }}</template>
        </el-table-column>
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
            <el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button>
            <el-button link type="success" size="small" @click="approve(row)">通过</el-button>
            <el-button link type="danger" size="small" @click="showReturn(row)">退回</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum" v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]" :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadData" @size-change="handleSizeChange"
        />
      </div>
      <el-empty v-if="!loading && tableData.length === 0" description="暂无需审核的记录" />
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog title="填报详情" v-model="detailVisible" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="人员">{{ detailReport?.employeeName }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ detailReport?.workDate }}</el-descriptions-item>
        <el-descriptions-item label="类别">{{ detailReport?.reportType === 'HOURS' ? '工时' : '工分' }}</el-descriptions-item>
        <el-descriptions-item label="车间">{{ detailReport?.workshopName }}</el-descriptions-item>
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
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
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
import type { WorkReport, MonthlyPeriod, OrgUnit } from '@/types'

const periods = ref<MonthlyPeriod[]>([])
const workshops = ref<OrgUnit[]>([])
const periodId = ref<number | null>(null)
const filterWorkshopId = ref<number | null>(null)
const filterStatus = ref('已提交')
const tableData = ref<WorkReport[]>([])
const loading = ref(false)
const pageNum = ref(1); const pageSize = ref(10); const total = ref(0)
const saving = ref(false); const returnVisible = ref(false); const returnComment = ref('')
const currentReturnId = ref<number | null>(null); const batchReturnMode = ref(false)
const selectedReports = ref<WorkReport[]>([])
// 查看详情
const detailVisible = ref(false)
const detailReport = ref<WorkReport | null>(null)
const canBatchApprove = computed(() => selectedReports.value.length > 0 && selectedReports.value.every(r => r.status === '已提交'))
function statusType(s: string) {
  const m: Record<string, string> = { '已提交': 'warning', '已审核': 'success', '已退回': 'danger' }
  return m[s] || 'info'
}

onMounted(async () => {
  try {
    const [pRes] = await Promise.all([
      periodApi.getPage({ pageNum: 1, pageSize: 100 }),
      orgApi.getWorkshops().then(w => { workshops.value = w })
    ])
    periods.value = pRes.records
    if (periods.value.length > 0) periodId.value = periods.value[0].id
  } catch { /* 忽略 */ }
  if (periodId.value) loadData()
})

async function loadData() {
  if (!periodId.value) return
  loading.value = true
  try {
    const res = await auditApi.getSectionPendingPage({
      pageNum: pageNum.value, pageSize: pageSize.value, periodId: periodId.value,
      workshopId: filterWorkshopId.value || undefined,
      status: filterStatus.value || undefined
    })
    tableData.value = res.records; total.value = res.total
  } finally { loading.value = false }
}

function onPeriodChange() { pageNum.value = 1; loadData() }
function onFilterChange() { pageNum.value = 1; loadData() }
function handleSizeChange() { pageNum.value = 1; loadData() }
function onSelectionChange(rows: WorkReport[]) { selectedReports.value = rows }

async function handleBatchApprove() {
  if (selectedReports.value.length === 0) { ElMessage.warning('请先勾选要审核的记录'); return }
  try {
    await ElMessageBox.confirm(`确定要批量通过选中的 ${selectedReports.value.length} 条记录吗？`, '提示', { type: 'warning' })
    await auditApi.batchApprove(selectedReports.value.map(r => r.id))
    ElMessage.success('批量通过成功')
    selectedReports.value = []; loadData()
  } catch { /* 用户取消 */ }
}

async function handleBatchReturn() {
  if (selectedReports.value.length === 0) { ElMessage.warning('请先勾选要退回的记录'); return }
  batchReturnMode.value = true; currentReturnId.value = null; returnComment.value = ''; returnVisible.value = true
}

async function approve(row: WorkReport) {
  try { await auditApi.approve(row.id); ElMessage.success('审核通过'); loadData() } catch (e: any) { ElMessage.error(e.message || '操作失败') }
}
function showDetail(row: WorkReport) { detailReport.value = row; detailVisible.value = true }

function showReturn(row: WorkReport) {
  batchReturnMode.value = false; currentReturnId.value = row.id; returnComment.value = ''; returnVisible.value = true
}
async function handleReturn() {
  if (!returnComment.value.trim()) { ElMessage.warning('请填写退回原因'); return }
  saving.value = true
  try {
    if (batchReturnMode.value) {
      await auditApi.batchReturn(selectedReports.value.map(r => r.id), returnComment.value)
      ElMessage.success(`已批量退回 ${selectedReports.value.length} 条记录`); selectedReports.value = []
    } else {
      await auditApi.returnReport(currentReturnId.value!, returnComment.value)
      ElMessage.success('已退回')
    }
    returnVisible.value = false; loadData()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') } finally { saving.value = false }
}
</script>

<style scoped>
.search-bar { margin-bottom: 8px }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px }
</style>
