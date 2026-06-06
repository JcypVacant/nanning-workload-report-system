<!-- 车间审核页面 — 查看本车间下属工区所有状态的填报记录 -->
<template>
  <div>
    <h2>车间审核</h2>
    <el-card style="margin-top:16px">
      <!-- 筛选栏 -->
      <el-form :inline="true" class="search-bar">
        <el-form-item label="月份">
          <el-select v-model="periodId" placeholder="选择月份" style="width:200px" @change="loadData">
            <el-option v-for="p in periods" :key="p.id" :label="p.periodName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width:140px" @change="handleFilterChange">
            <el-option label="已提交" value="已提交" />
            <el-option label="已审核" value="已审核" />
            <el-option label="已退回" value="已退回" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="areaName" label="工区" width="150" />
        <el-table-column prop="employeeName" label="人员" width="100" />
        <el-table-column prop="workDate" label="日期" width="120" />
        <el-table-column prop="reportType" label="类别" width="80">
          <template #default="{ row }">{{ row.reportType === 'HOURS' ? '工时' : '工分' }}</template>
        </el-table-column>
        <el-table-column label="项目明细" min-width="250">
          <template #default="{ row }">{{ (row.items || []).map(i => i.itemPath || i.itemName).join(', ') }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag>
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

      <!-- 分页 -->
      <div class="pagination-wrap" v-if="total > 0">
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
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { auditApi } from '@/api/audit'
import { periodApi } from '@/api/period'
import type { WorkReport, MonthlyPeriod } from '@/types'

const periods = ref<MonthlyPeriod[]>([])
const periodId = ref<number | null>(null)
const filterStatus = ref('')
const tableData = ref<WorkReport[]>([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const saving = ref(false)
const returnVisible = ref(false)
const returnComment = ref('')
const currentReturnId = ref<number | null>(null)

/** 状态标签颜色 */
function statusType(s: string) {
  const m: Record<string, string> = { '已提交': 'warning', '已审核': 'success', '已退回': 'danger' }
  return m[s] || 'info'
}

onMounted(async () => {
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
      status: filterStatus.value || undefined
    })
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handleFilterChange() {
  pageNum.value = 1
  loadData()
}

function handleSizeChange() {
  pageNum.value = 1
  loadData()
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
