<!--
  账号管理页面
  功能：账号的增删改查、密码重置、启停
  分页：每页10条，支持搜索
-->
<template>
  <div>
    <div class="page-header">
      <h2>账号管理</h2>
      <el-button type="primary" @click="showCreate">新增账号</el-button>
    </div>

    <el-card>
      <!-- 搜索栏 -->
      <el-form :inline="true" class="search-bar">
        <el-form-item label="搜索">
          <el-input v-model="keyword" placeholder="搜索账号或姓名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="username" label="账号" width="150" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="roleCode" label="角色" width="130">
          <template #default="{ row }">
            <el-tag size="small">{{ roleMap[row.roleCode] || row.roleCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="enabled" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ row.createTime?.replace('T', ' ') }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showEdit(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button link :type="row.enabled ? 'danger' : 'success'" size="small" @click="handleToggle(row)">
              {{ row.enabled ? '禁用' : '启用' }}
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
    <el-dialog :title="editingId ? '编辑账号' : '新增账号'" v-model="dialogVisible" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="登录账号">
          <el-input v-model="form.username" :disabled="!!editingId" placeholder="5-20个字符" maxlength="20" show-word-limit />
          <span v-if="!editingId" style="font-size:12px;color:#999">默认密码为 123456（6位），首次登录后需修改</span>
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="form.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="联系电话" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleCode" style="width:100%" @change="onRoleChange">
            <el-option label="段级管理员 — 管理全段数据" value="SECTION_ADMIN" />
            <el-option label="车间管理员 — 管理本车间及下属工区" value="WORKSHOP_ADMIN" />
            <el-option label="工区填报员 — 填报本工区数据" value="AREA_REPORTER" />
          </el-select>
        </el-form-item>
        <!-- 段级管理员：无需绑定组织 -->
        <!-- 车间管理员：绑定一个车间 -->
        <el-form-item v-if="form.roleCode === 'WORKSHOP_ADMIN'" label="绑定车间">
          <el-select v-model="form.orgId" placeholder="选择所属车间" style="width:100%" filterable>
            <el-option v-for="w in workshops" :key="w.id" :label="w.orgName" :value="w.id" />
          </el-select>
        </el-form-item>
        <!-- 工区填报员：绑定一个工区 -->
        <el-form-item v-if="form.roleCode === 'AREA_REPORTER'" label="绑定工区">
          <el-select v-model="form.orgId" placeholder="选择所属工区" style="width:100%" filterable>
            <el-option v-for="a in areas" :key="a.id" :label="a.orgName" :value="a.id" />
          </el-select>
        </el-form-item>
        <!-- 段级管理员：显示提示，无需选择 -->
        <el-form-item v-if="form.roleCode === 'SECTION_ADMIN'" label="数据范围">
          <el-tag type="info">段级管理员可查看和管理全段所有数据</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 账号管理页面逻辑
 * 支持分页查询（每页10条）、搜索、新增、编辑、密码重置、启停
 */
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api/user'
import { orgApi } from '@/api/org'
import type { SysUser, OrgUnit } from '@/types'

/** 角色显示映射 */
const roleMap: Record<string, string> = {
  SECTION_ADMIN: '段级管理员',
  WORKSHOP_ADMIN: '车间管理员',
  AREA_REPORTER: '工区填报员'
}

// ==================== 组织列表（用于绑定选择） ====================
const workshops = ref<OrgUnit[]>([])
const areas = ref<OrgUnit[]>([])

// ==================== 表格与分页状态 ====================
const tableData = ref<SysUser[]>([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)    // 每页10条
const total = ref(0)
const keyword = ref('')

// ==================== 对话框状态 ====================
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const form = ref({ username: '', realName: '', phone: '', roleCode: 'AREA_REPORTER', orgId: 0 })

onMounted(async () => {
  // 加载车间和工区列表用于账号绑定选择
  try {
    const [w, a] = await Promise.all([orgApi.getWorkshops(), orgApi.getAllAreas()])
    workshops.value = w; areas.value = a
  } catch { /* 忽略 */ }
  loadData()
})

/** 角色切换时重置组织绑定 */
function onRoleChange() {
  if (form.value.roleCode === 'SECTION_ADMIN') {
    form.value.orgId = 0
  } else {
    form.value.orgId = 0  // 重置，等待用户选择
  }
}

/**
 * 加载分页数据
 * 调用后端 GET /api/v1/users/page 接口
 */
async function loadData() {
  loading.value = true
  try {
    const res = await userApi.getPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value
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
  pageNum.value = 1
  loadData()
}

/**
 * 每页条数变化时：重置到第1页重新加载
 */
function handleSizeChange() {
  pageNum.value = 1
  loadData()
}

// ==================== 新增/编辑 ====================

function showCreate() {
  editingId.value = null
  form.value = { username: '', realName: '', phone: '', roleCode: 'AREA_REPORTER', orgId: 0 }
  dialogVisible.value = true
}

function showEdit(row: SysUser) {
  editingId.value = row.id
  form.value = {
    username: row.username,
    realName: row.realName,
    phone: row.phone || '',
    roleCode: row.roleCode || 'AREA_REPORTER',
    orgId: row.orgId || 0
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!editingId.value) {
    const u = form.value.username
    if (!u || u.length < 5 || u.length > 20) { ElMessage.warning('账号长度必须为5-20个字符'); return }
  }
  saving.value = true
  try {
    if (editingId.value) {
      await userApi.update(editingId.value, form.value as any)
      ElMessage.success('更新成功')
    } else {
      await userApi.create(form.value as any)
      ElMessage.success('创建成功，默认密码为 123456')
    }
    dialogVisible.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    saving.value = false
  }
}

// ==================== 密码重置 / 启停 ====================

async function handleResetPwd(row: SysUser) {
  try {
    await ElMessageBox.confirm(`确定重置用户「${row.username}」的密码吗？`, '提示', { type: 'warning' })
    await userApi.resetPassword(row.id)
    ElMessage.success('密码已重置为 123456')
  } catch { /* 用户取消 */ }
}

async function handleToggle(row: SysUser) {
  await userApi.toggleEnabled(row.id)
  ElMessage.success(row.enabled ? '已禁用' : '已启用')
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
