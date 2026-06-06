<!--
  人员管理页面
  功能：人员的增删改查、调动、启停
  分页：每页10条，支持按姓名搜索
-->
<template>
  <div>
    <div class="page-header">
      <h2>人员管理</h2>
      <el-button type="primary" @click="showCreate">新增人员</el-button>
    </div>

    <el-card>
      <!-- 搜索栏 -->
      <el-form :inline="true" class="search-bar">
        <el-form-item label="姓名">
          <el-input v-model="keyword" placeholder="搜索人员姓名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="车间">
          <el-select v-model="filterWorkshopId" placeholder="全部车间" clearable style="width:160px">
            <el-option v-for="w in workshops" :key="w.id" :label="w.orgName" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="60" />
        <el-table-column prop="departmentName" label="车间" width="140" />
        <el-table-column prop="teamName" label="工区/班组" width="140" />
        <el-table-column prop="positionName" label="职位" width="120" />
        <el-table-column prop="workType" label="工种" width="100" />
        <el-table-column prop="rankCategory" label="职级" width="80" />
        <el-table-column prop="employeeStatus" label="状态" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.employeeStatus === '在岗' ? 'success' : 'info'">
              {{ row.employeeStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showEdit(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="showTransfer(row)">调动</el-button>
            <el-button link :type="row.enabled ? 'danger' : 'success'" size="small" @click="handleToggle(row)">
              {{ row.enabled ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件：每页10条 -->
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
    <el-dialog :title="editingId ? '编辑人员' : '新增人员'" v-model="dialogVisible" width="620px">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名"><el-input v-model="form.name" placeholder="人员姓名" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="form.gender"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="车间ID"><el-input-number v-model="form.workshopId" :min="1" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工区ID"><el-input-number v-model="form.areaId" :min="1" style="width:100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="班组"><el-input v-model="form.teamName" placeholder="班组/工区名称" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位"><el-input v-model="form.positionName" placeholder="职位名称" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="工种"><el-input v-model="form.workType" placeholder="工种" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职级"><el-input v-model="form.rankCategory" placeholder="职级分类" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-select v-model="form.employeeStatus">
            <el-option label="在岗" value="在岗" /><el-option label="调出" value="调出" /><el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 人员调动对话框 -->
    <el-dialog title="人员调动" v-model="transferVisible" width="480px">
      <el-form label-width="100px">
        <el-form-item label="调动人员">
          <el-tag size="large">{{ transferEmp?.name }}</el-tag>
        </el-form-item>
        <el-form-item label="原属车间">
          <span>{{ transferEmp?.departmentName || '-' }}</span>
        </el-form-item>
        <el-form-item label="调往车间ID">
          <el-input-number v-model="transferForm.afterWorkshopId" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="调往工区ID">
          <el-input-number v-model="transferForm.afterAreaId" :min="1" style="width:100%" />
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
        <el-button type="primary" @click="handleTransfer" :loading="saving">确认调动</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 人员管理页面逻辑
 * 支持分页查询（每页10条）、按姓名/车间搜索、新增、编辑、调动、启停
 */
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { employeeApi } from '@/api/employee'
import { orgApi } from '@/api/org'
import type { Employee, OrgUnit } from '@/types'

// ==================== 搜索条件 ====================
const keyword = ref('')
const filterWorkshopId = ref<number | null>(null)
const workshops = ref<OrgUnit[]>([])

// ==================== 表格与分页状态 ====================
const tableData = ref<Employee[]>([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)    // 每页10条
const total = ref(0)

// ==================== 对话框状态 ====================
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const form = ref<Partial<Employee>>({
  gender: '男', workshopId: 10, areaId: 12, employeeStatus: '在岗'
})

// ==================== 调动对话框状态 ====================
const transferVisible = ref(false)
const transferEmp = ref<Employee | null>(null)
const transferForm = ref({ afterWorkshopId: 0, afterAreaId: 0, afterTeamName: '', transferReason: '' })

onMounted(async () => {
  // 加载车间列表用于搜索下拉
  try { workshops.value = await orgApi.getWorkshops() } catch { /* 忽略 */ }
  loadData()
})

/**
 * 加载分页数据
 * 调用后端 GET /api/v1/employees/page 接口
 * 每页10条，支持按姓名和车间筛选
 */
async function loadData() {
  loading.value = true
  try {
    const res = await employeeApi.getPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value,
      workshopId: filterWorkshopId.value
    })
    tableData.value = res.records
    total.value = res.total
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索：重置到第1页
 */
function handleSearch() {
  pageNum.value = 1
  loadData()
}

/**
 * 重置搜索条件
 */
function handleReset() {
  keyword.value = ''
  filterWorkshopId.value = null
  pageNum.value = 1
  loadData()
}

/**
 * 每页条数变化时：重置到第1页
 */
function handleSizeChange() {
  pageNum.value = 1
  loadData()
}

// ==================== 新增/编辑 ====================

function showCreate() {
  editingId.value = null
  form.value = { gender: '男', workshopId: 10, areaId: 12, employeeStatus: '在岗' }
  dialogVisible.value = true
}

function showEdit(row: Employee) {
  editingId.value = row.id
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSave() {
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

function showTransfer(row: Employee) {
  transferEmp.value = row
  transferForm.value = {
    afterWorkshopId: row.workshopId,
    afterAreaId: row.areaId,
    afterTeamName: row.teamName || '',
    transferReason: ''
  }
  transferVisible.value = true
}

async function handleTransfer() {
  saving.value = true
  try {
    await employeeApi.transfer({
      employeeId: transferEmp.value!.id,
      ...transferForm.value
    })
    ElMessage.success('调动成功')
    transferVisible.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '调动失败')
  } finally {
    saving.value = false
  }
}

// ==================== 启停 ====================

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
