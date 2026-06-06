<!-- 用工项目字典管理页面 - 树形字典CRUD -->
<template>
  <div>
    <div class="page-header"><h2>用工项目字典</h2><el-button type="primary" @click="showCreate(null)">新增项目</el-button></div>
    <el-card>
      <el-tree :data="treeData" node-key="id" default-expand-all :props="{label:'itemName',children:'children'}">
        <template #default="{data}">
          <span class="tree-node">
            <el-tag size="small" :type="data.inputType==='CATEGORY'?'info':data.inputType==='NUMBER'?'success':'warning'">{{data.inputType==='CATEGORY'?'分类':data.inputType==='NUMBER'?'数值':'文本'}}</el-tag>
            <span style="margin-left:6px">{{data.itemName}}</span>
            <span v-if="data.unit" style="color:#999">[{{data.unit}}]</span>
            <span class="tree-actions">
              <el-button link type="primary" size="small" @click.stop="showCreate(data)">添加子级</el-button>
              <el-button link type="primary" size="small" @click.stop="showEdit(data)">编辑</el-button>
              <el-button link type="danger" size="small" @click.stop="handleDelete(data)">删除</el-button>
            </span>
          </span>
        </template>
      </el-tree>
    </el-card>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="550px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="项目名称"><el-input v-model="form.itemName"/></el-form-item>
        <el-form-item label="输入类型"><el-select v-model="form.inputType"><el-option label="分类项" value="CATEGORY"/><el-option label="数值项" value="NUMBER"/><el-option label="文本项" value="TEXT"/></el-select></el-form-item>
        <el-form-item label="适用类型"><el-select v-model="form.reportType"><el-option label="工时" value="HOURS"/><el-option label="工分" value="POINTS"/><el-option label="通用" value="BOTH"/></el-select></el-form-item>
        <el-form-item label="单位"><el-input v-model="form.unit" placeholder="分钟/分/文本"/></el-form-item>
        <el-form-item label="是否可填写"><el-switch v-model="form.isInputItem"/></el-form-item>
        <el-form-item label="排序号"><el-input-number v-model="form.sortOrder" :min="0"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave" :loading="saving">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { workItemApi } from '@/api/workItem'
import type { WorkItem } from '@/types'

const treeData = ref<WorkItem[]>([])
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref<number|null>(null)
const form = ref<any>({ inputType:'NUMBER', reportType:'BOTH', isInputItem:true, sortOrder:0, isCategory:false, parentId:0 })

const dialogTitle = computed(() => editingId.value ? '编辑项目' : '新增项目')

onMounted(() => loadTree())
async function loadTree() { treeData.value = await workItemApi.getTree() }

function showCreate(parent: WorkItem|null) {
  editingId.value = null
  form.value = { parentId: parent?.id || 0, inputType: parent ? 'NUMBER' : 'CATEGORY', reportType:'BOTH', isInputItem: !!parent, isCategory: !parent, sortOrder:0 }
  dialogVisible.value = true
}
function showEdit(data: WorkItem) {
  editingId.value = data.id
  form.value = { ...data, isInputItem: data.isInputItem === 1, isCategory: data.isCategory === 1 }
  dialogVisible.value = true
}
async function handleSave() {
  saving.value = true
  try {
    const payload = { ...form.value, isInputItem: form.value.isInputItem ? 1 : 0, isCategory: form.value.isCategory ? 1 : 0 }
    if (editingId.value) { await workItemApi.update(editingId.value, payload); ElMessage.success('更新成功') }
    else { await workItemApi.create(payload); ElMessage.success('新增成功') }
    dialogVisible.value = false; loadTree()
  } catch(e:any) { ElMessage.error(e.message || '操作失败') }
  finally { saving.value = false }
}
async function handleDelete(data: WorkItem) {
  try { await ElMessageBox.confirm('确定删除吗？','提示',{type:'warning'}); await workItemApi.remove(data.id); ElMessage.success('删除成功'); loadTree() }
  catch { /* cancelled */ }
}
</script>
<style scoped>.page-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px}.page-header h2{margin:0;font-size:18px}.tree-node{flex:1;display:flex;align-items:center}.tree-actions{margin-left:auto}</style>
