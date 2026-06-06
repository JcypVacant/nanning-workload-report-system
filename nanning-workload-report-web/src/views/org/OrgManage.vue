<!-- 组织架构管理页面 - 树形结构的组织CRUD -->
<template>
  <div class="page-container">
    <div class="page-header">
      <h2>组织架构管理</h2>
      <el-button type="primary" @click="showCreateDialog(null)">新增组织</el-button>
    </div>
    <el-card>
      <el-tree :data="treeData" node-key="id" default-expand-all :props="{ label: 'orgName', children: 'children' }">
        <template #default="{ data }">
          <span class="tree-node">
            <el-tag :type="orgTypeTag(data.orgType)" size="small">{{ data.orgType === 'SECTION' ? '段' : data.orgType === 'WORKSHOP' ? '车间' : data.orgType === 'WORK_AREA' ? '工区' : '车间本级' }}</el-tag>
            <span style="margin-left:8px">{{ data.orgName }}</span>
            <span v-if="data.areaCode" style="color:#999;margin-left:8px">[工作表:{{ data.areaCode }}]</span>
            <span class="tree-actions">
              <el-button link type="primary" size="small" @click.stop="showCreateDialog(data)">添加子级</el-button>
              <el-button link type="primary" size="small" @click.stop="showEditDialog(data)">编辑</el-button>
              <el-button link type="danger" size="small" @click.stop="handleDelete(data)">删除</el-button>
            </span>
          </span>
        </template>
      </el-tree>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="组织名称"><el-input v-model="form.orgName" /></el-form-item>
        <el-form-item label="组织类型">
          <el-select v-model="form.orgType" :disabled="isEdit">
            <el-option label="车间" value="WORKSHOP" />
            <el-option label="工区" value="WORK_AREA" />
            <el-option label="车间本级" value="WORKSHOP_LEVEL" />
          </el-select>
        </el-form-item>
        <el-form-item label="工作表代码"><el-input v-model="form.areaCode" placeholder="A/B/C等" /></el-form-item>
        <el-form-item label="排序号"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orgApi } from '@/api/org'
import type { OrgUnit } from '@/types'

const treeData = ref<OrgUnit[]>([])
const dialogVisible = ref(false)
const saving = ref(false)
const form = ref<Partial<OrgUnit>>({ orgType: 'WORK_AREA', sortOrder: 0 })
const editingId = ref<number | null>(null)

const isEdit = computed(() => editingId.value !== null)
const dialogTitle = computed(() => isEdit.value ? '编辑组织' : '新增组织')

onMounted(() => loadTree())

async function loadTree() {
  treeData.value = await orgApi.getTree()
}

function orgTypeTag(type: string) {
  const map: Record<string, string> = { SECTION: 'info', WORKSHOP: 'success', WORK_AREA: 'warning', WORKSHOP_LEVEL: '' }
  return map[type] || ''
}

function showCreateDialog(parent: OrgUnit | null) {
  editingId.value = null
  form.value = { parentId: parent?.id || 0, orgType: parent ? 'WORK_AREA' : 'WORKSHOP', sortOrder: 0, areaCode: '' }
  dialogVisible.value = true
}

function showEditDialog(data: OrgUnit) {
  editingId.value = data.id
  form.value = { orgName: data.orgName, orgType: data.orgType, areaCode: data.areaCode, sortOrder: data.sortOrder }
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    if (isEdit.value) {
      await orgApi.update(editingId.value!, { orgName: form.value.orgName, areaCode: form.value.areaCode, sortOrder: form.value.sortOrder } as any)
      ElMessage.success('更新成功')
    } else {
      await orgApi.create({ parentId: form.value.parentId, orgName: form.value.orgName!, orgType: form.value.orgType!, areaCode: form.value.areaCode, sortOrder: form.value.sortOrder } as any)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadTree()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
  finally { saving.value = false }
}

async function handleDelete(data: OrgUnit) {
  try {
    await ElMessageBox.confirm('确定删除该组织吗？', '提示', { type: 'warning' })
    await orgApi.remove(data.id)
    ElMessage.success('删除成功')
    loadTree()
  } catch { /* cancelled */ }
}
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.tree-node { flex: 1; display: flex; align-items: center; }
.tree-actions { margin-left: auto; }
</style>
