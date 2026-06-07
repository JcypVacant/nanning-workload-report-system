<!-- 车间本级填报页面 - 与工区填报功能一致，车间管理员可为下属任一工区员工填报 -->
<template>
  <div>
    <h2>车间本级填报</h2>

    <!-- Card 1: 月份 + 工区筛选 -->
    <el-card style="margin-top:16px">
      <el-form :inline="true">
        <el-form-item label="月份">
          <el-select v-model="periodId" placeholder="选择月份" style="width:180px" @change="onPeriodChange">
            <el-option v-for="p in periods" :key="p.id" :label="p.periodName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="工区">
          <el-select v-model="selectedAreaId" placeholder="全部工区" clearable style="width:180px" @change="onAreaChange">
            <el-option label="全部工区" value="" />
            <el-option v-for="a in areas" :key="a.id" :label="a.orgName" :value="a.id" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Card 2: 人员列表 -->
    <el-card style="margin-top:16px">
      <template #header>
        <div class="card-header">
          <span>人员列表</span>
          <div style="display:flex;gap:8px">
            <el-input v-model="empKeyword" placeholder="搜索人员姓名" clearable style="width:200px" @keyup.enter="handleEmpSearch" @clear="handleEmpSearch" />
            <el-button type="primary" @click="handleEmpSearch">搜索</el-button>
          </div>
        </div>
      </template>
      <el-table :data="employeeList" border stripe v-loading="loadingEmployees">
        <el-table-column type="index" label="序号" width="60" :index="(idx: number) => (empPageNum - 1) * empPageSize + idx + 1" />
        <el-table-column prop="name" label="人员姓名" width="120" />
        <el-table-column label="所属工区" min-width="150">
          <template #default="{ row }">{{ row.areaName || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openFillDialog(row)">添加用工明细</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loadingEmployees && employeeList.length === 0" description="暂无人员" />
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="empPageNum"
          v-model:page-size="empPageSize"
          :page-sizes="[10, 20, 50]"
          :total="empTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadEmployeePage"
          @size-change="handleEmpSizeChange"
        />
      </div>
    </el-card>

    <!-- Card 3: 已填报记录 -->
    <el-card style="margin-top:16px">
      <template #header>已填报记录</template>
      <!-- 筛选行 -->
      <el-form :inline="true" class="search-bar">
        <el-form-item label="日期">
          <el-date-picker v-model="filterWorkDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:150px" />
        </el-form-item>
        <el-form-item label="人员">
          <el-input v-model="reportKeyword" placeholder="人员姓名" clearable style="width:140px" @keyup.enter="handleReportSearch" @clear="handleReportSearch" />
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="filterReportType" placeholder="全部" clearable style="width:100px">
            <el-option label="工时" value="HOURS" />
            <el-option label="工分" value="POINTS" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterStatus" placeholder="全部" clearable style="width:110px">
            <el-option label="草稿" value="草稿" />
            <el-option label="已提交" value="已提交" />
            <el-option label="已退回" value="已退回" />
            <el-option label="已审核" value="已审核" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleReportSearch">搜索</el-button>
          <el-button @click="handleReportReset">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="reportList" border stripe v-loading="loadingReports">
        <el-table-column prop="employeeName" label="人员" width="100" />
        <el-table-column prop="workDate" label="日期" width="120" />
        <el-table-column prop="reportType" label="类别" width="80">
          <template #default="{ row }">{{ row.reportType === 'HOURS' ? '工时' : '工分' }}</template>
        </el-table-column>
        <el-table-column label="项目明细" min-width="200">
          <template #default="{ row }">{{ row.items?.map((i: any) => i.itemPath || i.itemName).join(', ') }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="editReport(row)" v-if="row.status === '草稿' || row.status === '已退回'">编辑</el-button>
            <el-button link type="success" size="small" @click="submitReport(row)" v-if="row.status === '草稿' || row.status === '已退回'">提交</el-button>
            <el-button link type="danger" size="small" @click="deleteReport(row)" v-if="row.status === '草稿'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="periodId && reportList.length === 0 && !loadingReports" description="暂无填报记录" />
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="reportPageNum"
          v-model:page-size="reportPageSize"
          :page-sizes="[10, 20, 50]"
          :total="reportTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadReportPage"
          @size-change="handleReportSizeChange"
        />
      </div>
    </el-card>

    <!-- Dialog: 添加用工明细 -->
    <el-dialog
      :title="'添加用工明细 - ' + (currentEmployee?.name || '')"
      v-model="dialogVisible"
      width="900px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :inline="true">
        <el-form-item label="日期">
          <el-date-picker v-model="workDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="reportType" style="width:100px">
            <el-option label="工时" value="HOURS" />
            <el-option label="工分" value="POINTS" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="small" @click="addItem">添加明细行</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="items" border stripe>
        <el-table-column label="用工项目" min-width="220">
          <template #default="{ row }">
            <el-cascader
              v-model="row.workItemId"
              :options="workItemTree"
              :props="{ value: 'id', label: 'itemName', children: 'children', checkStrictly: false, emitPath: false }"
              placeholder="选择项目"
              style="width:100%"
            />
          </template>
        </el-table-column>
        <el-table-column v-if="reportType === 'HOURS'" label="工时(分钟)" width="150">
          <template #default="{ row }">
            <el-input-number v-model="row.numberValue" :min="0" :precision="1" style="width:130px" />
          </template>
        </el-table-column>
        <el-table-column v-if="reportType === 'POINTS'" label="工分" width="130">
          <template #default="{ row }">
            <el-input-number v-model="row.pointsValue" :min="0" :precision="1" style="width:110px" />
          </template>
        </el-table-column>
        <el-table-column label="备注" width="180">
          <template #default="{ row }">
            <el-input v-model="row.remark" placeholder="备注" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ $index }">
            <el-button link type="danger" size="small" @click="items.splice($index, 1)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top:12px;text-align:right">
        <span style="font-size:16px;font-weight:bold">合计：<template v-if="reportType === 'HOURS'">{{ totalSumTime }} 分钟</template><template v-else>{{ totalSumPoints }} 工分</template></span>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button @click="saveDraft" :loading="saving">保存草稿</el-button>
        <el-button type="primary" @click="saveAndSubmit" :loading="saving">保存并提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reportApi } from '@/api/report'
import { periodApi } from '@/api/period'
import { employeeApi } from '@/api/employee'
import { workItemApi } from '@/api/workItem'
import { orgApi } from '@/api/org'
import { useUserStore } from '@/store/user'
import type { WorkReport, MonthlyPeriod, Employee, WorkItem, OrgUnit } from '@/types'

const userStore = useUserStore()

const periods = ref<MonthlyPeriod[]>([])
const areas = ref<OrgUnit[]>([])
const employeeList = ref<Employee[]>([])
const workItemTree = ref<WorkItem[]>([])
const reportList = ref<WorkReport[]>([])
const periodId = ref<number | null>(null)
const selectedAreaId = ref<string | number>('')
const workDate = ref('')
const reportType = ref('HOURS')
const items = ref<{ workItemId: number | null; numberValue: number | null; pointsValue: number | null; remark: string }[]>([
  { workItemId: null, numberValue: null, pointsValue: null, remark: '' }
])
const saving = ref(false)
const editingReportId = ref<number | null>(null)
const dialogVisible = ref(false)
const currentEmployee = ref<Employee | null>(null)
const loadingEmployees = ref(false)
const loadingReports = ref(false)

// 人员表格分页
const empPageNum = ref(1)
const empPageSize = ref(10)
const empTotal = ref(0)
const empKeyword = ref('')

// 填报记录表格分页
const reportPageNum = ref(1)
const reportPageSize = ref(10)
const reportTotal = ref(0)

// 填报记录筛选
const reportKeyword = ref('')
const filterWorkDate = ref('')
const filterReportType = ref('')
const filterStatus = ref('')

const totalSumTime = computed(() => items.value.reduce((s, i) => s + (i.numberValue || 0), 0))
const totalSumPoints = computed(() => items.value.reduce((s, i) => s + (i.pointsValue || 0), 0))

function statusTag(s: string) {
  const m: Record<string, string> = { '草稿': 'info', '已提交': 'warning', '已退回': 'danger', '已审核': 'success', '已锁定': '' }
  return m[s] || ''
}

onMounted(async () => {
  const [pRes, wRes] = await Promise.all([
    periodApi.getActive(),
    workItemApi.getTree()
  ])
  if (pRes) {
    periods.value = [pRes]
    periodId.value = pRes.id
  } else {
    periods.value = (await periodApi.getPage({ pageNum: 1, pageSize: 100 })).records
  }
  workItemTree.value = wRes

  // 加载车间下属工区列表
  if (userStore.orgId) {
    try {
      areas.value = await orgApi.getAreasByWorkshopId(userStore.orgId)
    } catch { /* 忽略 */ }
  }

  // 加载人员列表（首页）
  await loadEmployeePage()
  // 加载填报记录（首页）
  if (periodId.value) {
    await loadReportPage()
  }
})

/** 加载人员分页数据 */
async function loadEmployeePage() {
  if (!userStore.orgId) return
  loadingEmployees.value = true
  try {
    const params: Record<string, any> = {
      pageNum: empPageNum.value,
      pageSize: empPageSize.value,
      workshopId: userStore.orgId,
      keyword: empKeyword.value || undefined
    }
    // 如果选择了具体工区，增加 areaId 过滤
    if (selectedAreaId.value) {
      params.areaId = selectedAreaId.value
    }
    const res = await employeeApi.getPage(params)
    employeeList.value = res.records
    empTotal.value = res.total
  } catch (e: any) {
    ElMessage.error(e.message || '加载人员失败')
  } finally {
    loadingEmployees.value = false
  }
}

function handleEmpSearch() {
  empPageNum.value = 1
  loadEmployeePage()
}

function handleEmpSizeChange() {
  empPageNum.value = 1
  loadEmployeePage()
}

function onPeriodChange() {
  reportPageNum.value = 1
  loadReportPage()
}

function onAreaChange() {
  empPageNum.value = 1
  reportPageNum.value = 1
  loadEmployeePage()
  if (periodId.value) {
    loadReportPage()
  }
}

/** 加载填报记录分页数据 */
async function loadReportPage() {
  if (!periodId.value) return
  loadingReports.value = true
  try {
    const res = await reportApi.getPage({
      pageNum: reportPageNum.value,
      pageSize: reportPageSize.value,
      periodId: periodId.value,
      keyword: reportKeyword.value || undefined,
      workDate: filterWorkDate.value || undefined,
      reportType: filterReportType.value || undefined,
      status: filterStatus.value || undefined
    })
    reportList.value = res.records
    reportTotal.value = res.total
  } catch (e: any) {
    ElMessage.error(e.message || '加载记录失败')
  } finally {
    loadingReports.value = false
  }
}

function handleReportSizeChange() {
  reportPageNum.value = 1
  loadReportPage()
}

function handleReportSearch() {
  reportPageNum.value = 1
  loadReportPage()
}

function handleReportReset() {
  reportKeyword.value = ''
  filterWorkDate.value = ''
  filterReportType.value = ''
  filterStatus.value = ''
  reportPageNum.value = 1
  loadReportPage()
}

/** 打开填报对话框（新增） */
function openFillDialog(row: Employee) {
  currentEmployee.value = row
  resetForm()
  dialogVisible.value = true
}

function addItem() {
  items.value.push({ workItemId: null, numberValue: null, pointsValue: null, remark: '' })
}

async function saveDraft() {
  if (!validate()) return
  saving.value = true
  try {
    const data = {
      periodId: periodId.value!,
      employeeId: currentEmployee.value!.id,
      workDate: workDate.value,
      reportType: reportType.value,
      items: items.value
        .filter(i => i.workItemId)
        .map(i => ({
          workItemId: i.workItemId!,
          numberValue: i.numberValue,
          pointsValue: i.pointsValue,
          remark: i.remark
        }))
    }
    if (editingReportId.value) {
      await reportApi.update(editingReportId.value, { items: data.items })
    } else {
      await reportApi.create(data)
    }
    ElMessage.success('保存草稿成功')
    dialogVisible.value = false
    loadReportPage()
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function saveAndSubmit() {
  if (!validate()) return
  saving.value = true
  try {
    const data = {
      periodId: periodId.value!,
      employeeId: currentEmployee.value!.id,
      workDate: workDate.value,
      reportType: reportType.value,
      items: items.value
        .filter(i => i.workItemId)
        .map(i => ({
          workItemId: i.workItemId!,
          numberValue: i.numberValue,
          pointsValue: i.pointsValue,
          remark: i.remark
        }))
    }
    let report: WorkReport
    if (editingReportId.value) {
      report = await reportApi.update(editingReportId.value, { items: data.items })
    } else {
      report = await reportApi.create(data)
    }
    await reportApi.submit(report.id)
    ElMessage.success('提交成功')
    dialogVisible.value = false
    loadReportPage()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    saving.value = false
  }
}

function validate(): boolean {
  if (!periodId.value) { ElMessage.warning('请选择月份'); return false }
  if (!currentEmployee.value) { ElMessage.warning('人员信息异常'); return false }
  if (!workDate.value) { ElMessage.warning('请选择日期'); return false }
  if (!items.value.some(i => i.workItemId)) { ElMessage.warning('请至少添加一条明细'); return false }
  return true
}

function resetForm() {
  items.value = [{ workItemId: null, numberValue: null, pointsValue: null, remark: '' }]
  workDate.value = ''
  reportType.value = 'HOURS'
  editingReportId.value = null
}

/** 编辑已有填报记录 — 回填表单并打开对话框 */
function editReport(row: WorkReport) {
  const emp = employeeList.value.find(e => e.id === row.employeeId)
  if (!emp) {
    ElMessage.warning('未找到该人员信息')
    return
  }
  currentEmployee.value = emp
  editingReportId.value = row.id
  workDate.value = row.workDate
  reportType.value = row.reportType
  items.value = (row.items || []).map(i => ({
    workItemId: i.workItemId,
    numberValue: i.numberValue,
    pointsValue: i.pointsValue,
    remark: i.remark || ''
  }))
  if (items.value.length === 0) {
    items.value = [{ workItemId: null, numberValue: null, pointsValue: null, remark: '' }]
  }
  dialogVisible.value = true
}

async function submitReport(row: WorkReport) {
  await reportApi.submit(row.id)
  ElMessage.success('提交成功')
  loadReportPage()
}

async function deleteReport(row: WorkReport) {
  try {
    await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
    await reportApi.remove(row.id)
    ElMessage.success('已删除')
    loadReportPage()
  } catch { /* 用户取消 */ }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-bar {
  margin-bottom: 8px;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
