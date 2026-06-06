<!-- 月度填报管理页面 -->
<template>
  <div>
    <div class="page-header"><h2>月度填报管理</h2><el-button type="primary" @click="showCreate">创建月份</el-button></div>
    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="periodName" label="月份" width="140"/>
        <el-table-column prop="status" label="状态" width="120"><template #default="{row}"><el-tag>{{row.status}}</el-tag></template></el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120"/>
        <el-table-column prop="endDate" label="截止日期" width="120"/>
        <el-table-column prop="locked" label="是否锁定" width="90"><template #default="{row}">{{row.locked?'是':'否'}}</template></el-table-column>
        <el-table-column label="操作" min-width="250">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="changeStatus(row)">变更状态</el-button>
            <el-button link :type="row.locked?'warning':'danger'" size="small" @click="handleToggleLock(row)">{{row.locked?'解锁':'锁定'}}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog title="创建月度期间" v-model="dialogVisible" width="400px">
      <el-form label-width="80px">
        <el-form-item label="年份"><el-input-number v-model="createForm.year" :min="2024":max="2030"/></el-form-item>
        <el-form-item label="月份"><el-select v-model="createForm.month"><el-option v-for="m in 12":key="m":label="m+'月'" :value="m"/></el-select></el-form-item>
        <el-form-item label="开始日期"><el-date-picker v-model="createForm.startDate" type="date"/></el-form-item>
        <el-form-item label="截止日期"><el-date-picker v-model="createForm.endDate" type="date"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleCreate" :loading="saving">创建</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { periodApi } from '@/api/period'
import type { MonthlyPeriod } from '@/types'

const tableData = ref<MonthlyPeriod[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const createForm = reactive({ year: new Date().getFullYear(), month: new Date().getMonth()+1, startDate:'', endDate:'' })

onMounted(loadData)
async function loadData() {
  loading.value=true
  try { const res=await periodApi.getPage({pageNum:1,pageSize:100}); tableData.value=res.records }
  finally { loading.value=false }
}
function showCreate() { dialogVisible.value = true }
async function handleCreate() {
  saving.value=true
  try { await periodApi.create({...createForm, auditDeadline: createForm.endDate}); ElMessage.success('创建成功'); dialogVisible.value=false; loadData() }
  catch(e:any) { ElMessage.error(e.message||'创建失败') }
  finally { saving.value=false }
}
async function changeStatus(row:MonthlyPeriod) {
  // 简化：按流转顺序变更
  const next:Record<string,string>={'未开始':'填报中','填报中':'车间审核中','车间审核中':'段级汇总中','段级汇总中':'已锁定'}
  const ns = next[row.status]
  if(!ns){ElMessage.warning('当前状态无法变更');return}
  try { await periodApi.changeStatus(row.id, ns); ElMessage.success('状态已变更'); loadData() }
  catch(e:any) { ElMessage.error(e.message||'状态变更失败') }
}
async function handleToggleLock(row:MonthlyPeriod) {
  try { await periodApi.toggleLock(row.id); ElMessage.success('操作成功'); loadData() }
  catch(e:any) { ElMessage.error(e.message||'操作失败') }
}
</script>
<style scoped>.page-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px}.page-header h2{margin:0;font-size:18px}</style>
