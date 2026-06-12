<!--
  人员管理页面
  功能：人员的增删改查、调动、启停
  分页：每页10条，支持按姓名搜索
  信息字段：姓名、性别、单位、部门（车间）、班组、出生日期、
            职务名称、聘任专业职务、工种、职级分类、状态
-->
<template>
  <div>
    <div class="page-header">
      <h2>人员管理</h2>
      <div>
        <el-button type="primary" @click="showCreate">新增人员</el-button>
        <el-button @click="toggleTransferRecords">{{ showTransferRecords ? '收起记录' : '调动记录' }}</el-button>
      </div>
    </div>

    <el-card>
      <!-- 搜索栏 -->
      <el-form :inline="true" class="search-bar">
        <el-form-item label="姓名">
          <el-input v-model="keyword" placeholder="搜索人员姓名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="车间" v-if="!userStore.isWorkshopAdmin">
          <el-select v-model="filterWorkshopId" placeholder="全部车间" clearable style="width:180px" @change="onFilterWorkshopChange">
            <el-option v-for="w in workshops" :key="w.id" :label="w.orgName" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="工区" v-if="filterWorkshopId">
          <el-select v-model="filterAreaId" placeholder="全部工区" clearable style="width:150px" @change="handleSearch">
            <el-option v-for="a in filterAreas" :key="a.id" :label="a.orgName" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width:120px">
            <el-option label="在岗" value="在岗" />
            <el-option label="调出" value="调出" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格：包含全部11个人员信息字段 -->
      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="name" label="姓名" width="80" fixed />
        <el-table-column prop="gender" label="性别" width="55" />
        <el-table-column prop="unitName" label="单位" min-width="120" />
        <el-table-column prop="departmentName" label="部门（车间）" min-width="140" />
        <el-table-column prop="teamName" label="班组" min-width="120" />
        <el-table-column prop="birthDate" label="出生日期" width="110" />
        <el-table-column prop="positionName" label="职务名称" min-width="120" />
        <el-table-column prop="professionalPostType" label="聘任专业职务" min-width="130" />
        <el-table-column prop="workType" label="工种" width="100" />
        <el-table-column prop="rankCategory" label="职级分类" width="100" />
        <el-table-column prop="employeeStatus" label="状态" width="75">
          <template #default="{ row }">
            <el-tag size="small" :type="row.employeeStatus === '在岗' ? 'success' : row.employeeStatus === '调出' ? 'warning' : 'info'">
              {{ row.employeeStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showEdit(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="showTransfer(row)">调动</el-button>
            <el-button link :type="row.enabled ? 'danger' : 'success'" size="small" @click="handleToggle(row)">
              {{ row.enabled ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
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

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="editingId ? '编辑人员' : '新增人员'" v-model="dialogVisible" width="750px">
      <el-form :model="form" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名" required><el-input v-model="form.name" placeholder="人员姓名" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" required>
              <el-select v-model="form.gender" style="width:100%">
                <el-option label="男" value="男" /><el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="单位"><el-input v-model="form.unitName" placeholder="如：南宁通信段" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门（车间）"><el-input v-model="form.departmentName" placeholder="车间名称" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="班组"><el-input v-model="form.teamName" placeholder="班组名称" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期">
              <el-date-picker v-model="form.birthDate" type="date" placeholder="选择出生日期" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="职务名称"><el-input v-model="form.positionName" placeholder="职务名称" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="聘任专业职务"><el-input v-model="form.professionalPostType" placeholder="聘任专业职务工种" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="工种"><el-input v-model="form.workType" placeholder="工种" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职级分类"><el-input v-model="form.rankCategory" placeholder="职级分类" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属车间" required>
              <el-select v-model="form.workshopId" placeholder="选择车间" style="width:100%" filterable :disabled="userStore.isWorkshopAdmin" @change="onFormWorkshopChange">
                <el-option v-for="w in workshops" :key="w.id" :label="w.orgName" :value="w.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属工区" required>
              <el-select v-model="form.areaId" placeholder="选择工区" style="width:100%" filterable>
                <el-option v-for="a in dialogAreas" :key="a.id" :label="a.orgName" :value="a.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.employeeStatus" style="width:100%">
                <el-option label="在岗" value="在岗" />
                <el-option label="调出" value="调出" />
                <el-option label="停用" value="停用" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注"><el-input v-model="form.remark" placeholder="备注信息" /></el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 人员调动对话框 -->
    <el-dialog title="人员调动" v-model="transferVisible" width="520px">
      <el-form label-width="100px">
        <el-form-item label="调动人员">
          <el-tag size="large">{{ transferEmp?.name }}</el-tag>
        </el-form-item>
        <el-form-item label="原单位">
          <span>{{ transferEmp?.unitName || '-' }}</span>
        </el-form-item>
        <el-form-item label="原属车间">
          <span>{{ transferEmp?.departmentName || '-' }}</span>
        </el-form-item>
        <el-form-item label="原班组">
          <span>{{ transferEmp?.teamName || '-' }}</span>
        </el-form-item>
        <el-divider />
        <el-form-item label="调往车间" required>
          <el-select v-model="transferForm.afterWorkshopId" placeholder="选择车间" style="width:100%" filterable :disabled="userStore.isWorkshopAdmin">
            <el-option v-for="w in workshops" :key="w.id" :label="w.orgName" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="调往工区" required>
          <el-select v-model="transferForm.afterAreaId" placeholder="选择工区" style="width:100%" filterable>
            <el-option v-for="a in transferAreas" :key="a.id" :label="a.orgName" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="调往班组">
          <el-input v-model="transferForm.afterTeamName" placeholder="新班组名称" />
        </el-form-item>
        <el-form-item label="调动原因">
          <el-input v-model="transferForm.transferReason" type="textarea" :rows="2" placeholder="请填写调动原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTransfer" :loading="saving">{{ userStore.isWorkshopAdmin ? '提交调动申请' : '确认调动' }}</el-button>
      </template>
    </el-dialog>

    <!-- 调动记录列表 -->
    <el-card style="margin-top:16px" v-if="showTransferRecords">
      <template #header>
        <div class="card-header">
          <span>调动记录</span>
          <el-button text size="small" @click="showTransferRecords = false">收起</el-button>
        </div>
      </template>
      <el-form :inline="true" class="search-bar">
        <el-form-item label="状态" v-if="userStore.isSectionAdmin">
          <el-select v-model="recordStatus" placeholder="全部" clearable style="width:120px" @change="loadTransferRecords">
            <el-option label="待审核" value="待审核" />
            <el-option label="已通过" value="已通过" />
            <el-option label="已退回" value="已退回" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="small" @click="loadTransferRecords">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="transferRecords" border stripe v-loading="loadingRecords">
        <el-table-column label="人员" width="100">
          <template #default="{ row }">{{ row.remark || row.employeeId }}</template>
        </el-table-column>
        <el-table-column prop="operateTime" label="申请时间" width="160">
          <template #default="{ row }">{{ row.operateTime?.replace('T', ' ') }}</template>
        </el-table-column>
        <el-table-column label="调动详情" min-width="200">
          <template #default="{ row }">工区{{ row.beforeAreaId }} → 工区{{ row.afterAreaId }}</template>
        </el-table-column>
        <el-table-column prop="transferReason" label="原因" width="150" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status==='待审核'?'warning':row.status==='已通过'?'success':'danger'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" v-if="userStore.isSectionAdmin && recordStatus === '待审核'">
          <template #default="{ row }">
            <template v-if="row.status === '待审核'">
              <el-button link type="success" size="small" @click="approveTransfer(row, true)">通过</el-button>
              <el-button link type="danger" size="small" @click="approveTransfer(row, false)">退回</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="recordPageNum" v-model:page-size="recordPageSize"
          :page-sizes="[10,20,50]" :total="recordTotal"
          layout="total,sizes,prev,pager,next" @current-change="loadTransferRecords" @size-change="handleRecordSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
/**
 * 人员管理页面逻辑
 * 支持分页查询（每页10条）、按姓名/车间/状态搜索、新增、编辑、调动、启停
 */
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { employeeApi } from '@/api/employee'
import { orgApi } from '@/api/org'
import { useUserStore } from '@/store/user'
import type { Employee, OrgUnit } from '@/types'

// ==================== 搜索条件 ====================
const userStore = useUserStore()
const keyword = ref('')
const filterWorkshopId = ref<number | null>(null)
const filterAreaId = ref<number | null>(null)
const filterStatus = ref('')
const workshops = ref<OrgUnit[]>([])
const areas = ref<OrgUnit[]>([])
const filterAreas = ref<OrgUnit[]>([])
const dialogAreas = ref<OrgUnit[]>([])
const transferAreas = ref<OrgUnit[]>([])

// 调动记录
const showTransferRecords = ref(false)
const transferRecords = ref<any[]>([])
const loadingRecords = ref(false)
const recordPageNum = ref(1)
const recordPageSize = ref(10)
const recordTotal = ref(0)
const recordStatus = ref(userStore.isSectionAdmin ? '待审核' : '')

// ==================== 表格与分页状态 ====================
const tableData = ref<Employee[]>([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// ==================== 对话框状态 ====================
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const form = ref<Partial<Employee>>({
  gender: '男',
  unitName: '南宁通信段',
  employeeStatus: '在岗',
  workshopId: undefined,
  areaId: undefined
})

// ==================== 调动对话框状态 ====================
const transferVisible = ref(false)
const transferEmp = ref<Employee | null>(null)
const transferForm = ref({
  afterWorkshopId: 0,
  afterAreaId: 0,
  afterTeamName: '',
  transferReason: ''
})

/** 车间变更时加载下属工区 */
async function onFilterWorkshopChange(val: number | null) {
  filterAreaId.value = null
  if (val) {
    try { filterAreas.value = await orgApi.getAreasByWorkshopId(val) } catch { filterAreas.value = [] }
  } else {
    filterAreas.value = []
  }
  handleSearch()
}

onMounted(async () => {
  try {
    workshops.value = await orgApi.getWorkshops()
    areas.value = await orgApi.getAllAreas()
  } catch { /* 忽略 */ }
  // 车间管理员只能看本车间
  if (userStore.isWorkshopAdmin && userStore.orgId) {
    filterWorkshopId.value = userStore.orgId
    // 加载本车间下属工区
    try { filterAreas.value = await orgApi.getAreasByWorkshopId(userStore.orgId) } catch { /* 忽略 */ }
  }
  loadData()
})

/** 加载分页数据 */
async function loadData() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value,
      workshopId: filterWorkshopId.value,
      status: filterStatus.value || undefined
    }
    if (filterAreaId.value) {
      params.areaId = filterAreaId.value
    }
    const res = await employeeApi.getPage(params)
    tableData.value = res.records
    total.value = res.total
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() { pageNum.value = 1; loadData() }

/** 表单车间变更时加载工区 */
async function onFormWorkshopChange(val: number | undefined) {
  form.areaId = undefined
  dialogAreas.value = []
  if (val) {
    try { dialogAreas.value = await orgApi.getAreasByWorkshopId(val) } catch { /* 忽略 */ }
  }
}

function handleReset() {
  keyword.value = ''
  filterWorkshopId.value = userStore.isWorkshopAdmin ? userStore.orgId : null
  filterAreaId.value = null
  filterAreas.value = []
  filterStatus.value = ''
  pageNum.value = 1
  loadData()
}

function handleSizeChange() { pageNum.value = 1; loadData() }

// ==================== 新增/编辑 ====================

async function showCreate() {
  editingId.value = null
  const wsId = userStore.isWorkshopAdmin ? userStore.orgId! : workshops.value[0]?.id
  form.value = {
    gender: '男',
    unitName: '南宁通信段',
    employeeStatus: '在岗',
    workshopId: wsId,
    areaId: undefined
  }
  dialogAreas.value = []
  if (wsId) {
    try { dialogAreas.value = await orgApi.getAreasByWorkshopId(wsId) } catch { /* 忽略 */ }
  }
  form.areaId = dialogAreas.value[0]?.id
  dialogVisible.value = true
}

async function showEdit(row: Employee) {
  editingId.value = row.id
  form.value = { ...row }
  dialogAreas.value = []
  if (row.workshopId) {
    try { dialogAreas.value = await orgApi.getAreasByWorkshopId(row.workshopId) } catch { /* 忽略 */ }
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.name) { ElMessage.warning('请输入姓名'); return }
  if (!form.value.workshopId) { ElMessage.warning('请选择所属车间'); return }
  if (!form.value.areaId) { ElMessage.warning('请选择所属工区'); return }
  saving.value = true
  try {
    if (editingId.value) {
      await employeeApi.update(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await employeeApi.create(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    saving.value = false
  }
}

// ==================== 调动 ====================

async function showTransfer(row: Employee) {
  transferEmp.value = row
  const wsId = userStore.isWorkshopAdmin ? userStore.orgId! : row.workshopId
  transferForm.value = {
    afterWorkshopId: wsId,
    afterAreaId: row.areaId,
    afterTeamName: row.teamName || '',
    transferReason: ''
  }
  transferAreas.value = []
  if (wsId) {
    try { transferAreas.value = await orgApi.getAreasByWorkshopId(wsId) } catch { /* 忽略 */ }
  }
  transferVisible.value = true
}

async function handleTransfer() {
  if (!transferForm.value.afterWorkshopId) { ElMessage.warning('请选择调往车间'); return }
  if (!transferForm.value.afterAreaId) { ElMessage.warning('请选择调往工区'); return }
  saving.value = true
  try {
    await employeeApi.transfer({
      employeeId: transferEmp.value!.id,
      ...transferForm.value
    })
    if (userStore.isWorkshopAdmin) {
      ElMessage.success('调动申请已提交，等待段级管理员审核')
    } else {
      ElMessage.success('调动成功')
    }
    transferVisible.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '调动失败')
  } finally {
    saving.value = false
  }
}

// ==================== 启停 ====================

function toggleTransferRecords() {
  showTransferRecords.value = !showTransferRecords.value
  if (showTransferRecords.value) { recordPageNum.value = 1; loadTransferRecords() }
}

async function loadTransferRecords() {
  loadingRecords.value = true
  try {
    const res = await employeeApi.getTransferRecordsPage({
      pageNum: recordPageNum.value, pageSize: recordPageSize.value,
      status: recordStatus.value || undefined
    })
    transferRecords.value = res.records
    recordTotal.value = res.total
  } finally { loadingRecords.value = false }
}

function handleRecordSizeChange() { recordPageNum.value = 1; loadTransferRecords() }

async function approveTransfer(row: any, approved: boolean) {
  try {
    await employeeApi.approveTransfer(row.id, approved, approved ? '审核通过' : '审核退回')
    ElMessage.success(approved ? '已通过' : '已退回')
    loadTransferRecords()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
}

async function handleToggle(row: Employee) {
  await employeeApi.toggleEnabled(row.id)
  ElMessage.success(row.enabled ? '已停用' : '已启用')
  loadData()
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-header h2 {
  margin: 0;
  font-size: 18px;
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
