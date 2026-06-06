<!-- 车间审核页面 -->
<template>
  <div>
    <h2>车间审核</h2>
    <el-card style="margin-top:16px">
      <el-form :inline="true"><el-form-item label="月份"><el-select v-model="periodId" placeholder="选择月份"><el-option v-for="p in periods" :key="p.id" :label="p.periodName" :value="p.id"/></el-select></el-form-item></el-form>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="areaName" label="工区" width="150"/>
        <el-table-column prop="employeeName" label="人员" width="100"/>
        <el-table-column prop="workDate" label="日期" width="120"/>
        <el-table-column prop="reportType" label="类别" width="80"><template #default="{row}">{{row.reportType==='HOURS'?'工时':'工分'}}</template></el-table-column>
        <el-table-column label="项目" min-width="200"><template #default="{row}">{{row.items?.map((i:any)=>i.itemPath||i.itemName).join(', ')}}</template></el-table-column>
        <el-table-column prop="status" label="状态" width="90"><template #default="{row}"><el-tag size="small">{{row.status}}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{row}">
            <el-button link type="success" size="small" @click="approve(row)">通过</el-button>
            <el-button link type="danger" size="small" @click="showReturn(row)">退回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog title="退回原因" v-model="returnVisible" width="400px">
      <el-input v-model="returnComment" type="textarea" placeholder="请填写退回原因（必填）"/>
      <template #footer><el-button @click="returnVisible=false">取消</el-button><el-button type="primary" @click="handleReturn" :loading="saving">确认退回</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { auditApi } from '@/api/audit'
import { periodApi } from '@/api/period'
import type { WorkReport, MonthlyPeriod } from '@/types'

const periods = ref<MonthlyPeriod[]>([])
const periodId = ref<number|null>(null)
const tableData = ref<WorkReport[]>([])
const loading = ref(false)
const saving = ref(false)
const returnVisible = ref(false)
const returnComment = ref('')
const currentReturnId = ref<number|null>(null)

onMounted(async()=>{
  const pRes=await periodApi.getActive()
  if(pRes){periods.value=[pRes];periodId.value=pRes.id}else periods.value=(await periodApi.getPage({pageNum:1,pageSize:100})).records
})

watch(periodId,loadData)

async function loadData(){
  if(!periodId.value)return
  loading.value=true
  try{tableData.value=await auditApi.getPending({periodId:periodId.value})}
  finally{loading.value=false}
}

async function approve(row:WorkReport){
  try{await auditApi.approve(row.id);ElMessage.success('审核通过');loadData()}
  catch(e:any){ElMessage.error(e.message||'操作失败')}
}

function showReturn(row:WorkReport){currentReturnId.value=row.id;returnComment.value='';returnVisible.value=true}

async function handleReturn(){
  if(!returnComment.value.trim()){ElMessage.warning('请填写退回原因');return}
  saving.value=true
  try{await auditApi.returnReport(currentReturnId.value!,returnComment.value);ElMessage.success('已退回');returnVisible.value=false;loadData()}
  catch(e:any){ElMessage.error(e.message||'操作失败')}
  finally{saving.value=false}
}
</script>
