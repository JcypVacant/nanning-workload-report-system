<!-- 工区填报页面 - 核心填报功能 -->
<template>
  <div>
    <h2>工区填报</h2>
    <el-card style="margin-top:16px">
      <el-form :inline="true">
        <el-form-item label="月份"><el-select v-model="periodId" placeholder="选择月份" style="width:180px"><el-option v-for="p in periods":key="p.id":label="p.periodName":value="p.id"/></el-select></el-form-item>
        <el-form-item label="人员"><el-select v-model="employeeId" placeholder="选择人员" style="width:180px" filterable><el-option v-for="e in employees":key="e.id":label="e.name":value="e.id"/></el-select></el-form-item>
        <el-form-item label="日期"><el-date-picker v-model="workDate" type="date" placeholder="选择日期"/></el-form-item>
        <el-form-item label="类别"><el-select v-model="reportType" style="width:100px"><el-option label="工时" value="HOURS"/><el-option label="工分" value="POINTS"/></el-select></el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:16px">
      <template #header><span>填报明细</span><el-button type="primary" size="small" style="float:right" @click="addItem">添加明细行</el-button></template>
      <el-table :data="items" border stripe>
        <el-table-column label="用工项目" min-width="220">
          <template #default="{row}">
            <el-cascader v-model="row.workItemId" :options="workItemTree" :props="{value:'id',label:'itemName',children:'children',checkStrictly:false,emitPath:false}" placeholder="选择项目" style="width:100%"/>
          </template>
        </el-table-column>
        <el-table-column label="工时(分钟)" width="150">
          <template #default="{row}"><el-input-number v-model="row.numberValue" :min="0" :precision="1" style="width:130px" /></template>
        </el-table-column>
        <el-table-column label="工分" width="130">
          <template #default="{row}"><el-input-number v-model="row.pointsValue" :min="0" :precision="1" style="width:110px" /></template>
        </el-table-column>
        <el-table-column label="备注" width="180"><template #default="{row}"><el-input v-model="row.remark" placeholder="备注"/></template></el-table-column>
        <el-table-column label="操作" width="80"><template #default="{$index}"><el-button link type="danger" size="small" @click="items.splice($index,1)">删除</el-button></template></el-table-column>
      </el-table>
      <div style="margin-top:12px;text-align:right">
        <span style="font-size:16px;font-weight:bold">合计：{{ totalSumTime }} 分钟 / {{ totalSumPoints }} 工分</span>
      </div>
      <div style="margin-top:16px">
        <el-button @click="saveDraft" :loading="saving">保存草稿</el-button>
        <el-button type="primary" @click="saveAndSubmit" :loading="saving">保存并提交</el-button>
      </div>
    </el-card>

    <!-- 已有填报记录 -->
    <el-card style="margin-top:16px">
      <template #header>已填报记录</template>
      <el-table :data="reportList" border stripe>
        <el-table-column prop="employeeName" label="人员" width="100"/>
        <el-table-column prop="workDate" label="日期" width="120"/>
        <el-table-column prop="reportType" label="类别" width="80"><template #default="{row}">{{row.reportType==='HOURS'?'工时':'工分'}}</template></el-table-column>
        <el-table-column label="项目明细" min-width="200">
          <template #default="{row}">{{row.items?.map((i:any)=>i.itemPath||i.itemName).join(', ')}}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90"><template #default="{row}"><el-tag :type="statusTag(row.status)" size="small">{{row.status}}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{row}">
            <el-button link type="primary" size="small" @click="editReport(row)" v-if="row.status==='草稿'||row.status==='已退回'">编辑</el-button>
            <el-button link type="success" size="small" @click="submitReport(row)" v-if="row.status==='草稿'||row.status==='已退回'">提交</el-button>
            <el-button link type="danger" size="small" @click="deleteReport(row)" v-if="row.status==='草稿'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reportApi } from '@/api/report'
import { periodApi } from '@/api/period'
import { employeeApi } from '@/api/employee'
import { workItemApi } from '@/api/workItem'
import type { WorkReport, WorkReportItem, MonthlyPeriod, Employee, WorkItem } from '@/types'

const periods = ref<MonthlyPeriod[]>([])
const employees = ref<Employee[]>([])
const workItemTree = ref<WorkItem[]>([])
const reportList = ref<WorkReport[]>([])
const periodId = ref<number|null>(null)
const employeeId = ref<number|null>(null)
const workDate = ref('')
const reportType = ref('HOURS')
const items = ref<{workItemId:number|null,numberValue:number|null,pointsValue:number|null,remark:string}[]>([{workItemId:null,numberValue:null,pointsValue:null,remark:''}])
const saving = ref(false)
const editingReportId = ref<number|null>(null)

const totalSumTime = computed(() => items.value.reduce((s,i)=>s+(i.numberValue||0),0))
const totalSumPoints = computed(() => items.value.reduce((s,i)=>s+(i.pointsValue||0),0))

function statusTag(s:string) {
  const m:Record<string,string>={'草稿':'info','已提交':'warning','已退回':'danger','已审核':'success','已锁定':''}
  return m[s]||''
}

onMounted(async()=>{
  const [pRes,eRes,wRes]=await Promise.all([periodApi.getActive(),employeeApi.getPage({pageNum:1,pageSize:100}),workItemApi.getTree()])
  if(pRes){periods.value=[pRes];periodId.value=pRes.id}else periods.value=(await periodApi.getPage({pageNum:1,pageSize:100})).records
  employees.value=eRes.records;workItemTree.value=wRes
})

watch(employeeId,loadReports)
watch(periodId,loadReports)

async function loadReports(){
  if(!periodId.value||!employeeId.value)return
  const res=await reportApi.getPage({pageNum:1,pageSize:50,periodId:periodId.value,employeeId:employeeId.value})
  reportList.value=res.records
}

function addItem(){items.value.push({workItemId:null,numberValue:null,pointsValue:null,remark:''})}

async function saveDraft(){
  if(!validate())return
  saving.value=true
  try{
    const data={periodId:periodId.value!,employeeId:employeeId.value!,workDate:workDate.value,reportType:reportType.value,
      items:items.value.filter(i=>i.workItemId).map(i=>({workItemId:i.workItemId!,numberValue:i.numberValue,pointsValue:i.pointsValue,remark:i.remark}))}
    if(editingReportId.value){await reportApi.update(editingReportId.value,{items:data.items})}
    else await reportApi.create(data)
    ElMessage.success('保存草稿成功');resetForm();loadReports()
  }catch(e:any){ElMessage.error(e.message||'保存失败')}
  finally{saving.value=false}
}

async function saveAndSubmit(){
  if(!validate())return
  saving.value=true
  try{
    const data={periodId:periodId.value!,employeeId:employeeId.value!,workDate:workDate.value,reportType:reportType.value,
      items:items.value.filter(i=>i.workItemId).map(i=>({workItemId:i.workItemId!,numberValue:i.numberValue,pointsValue:i.pointsValue,remark:i.remark}))}
    let report:WorkReport
    if(editingReportId.value){report=await reportApi.update(editingReportId.value,{items:data.items})}
    else report=await reportApi.create(data)
    await reportApi.submit(report.id)
    ElMessage.success('提交成功');resetForm();loadReports()
  }catch(e:any){ElMessage.error(e.message||'操作失败')}
  finally{saving.value=false}
}

function validate():boolean{
  if(!periodId.value){ElMessage.warning('请选择月份');return false}
  if(!employeeId.value){ElMessage.warning('请选择人员');return false}
  if(!workDate.value){ElMessage.warning('请选择日期');return false}
  if(!items.value.some(i=>i.workItemId)){ElMessage.warning('请至少添加一条明细');return false}
  return true
}

function resetForm(){items.value=[{workItemId:null,numberValue:null,pointsValue:null,remark:''}];editingReportId.value=null}
function editReport(row:WorkReport){editingReportId.value=row.id;items.value=(row.items||[]).map(i=>({workItemId:i.workItemId,numberValue:i.numberValue,pointsValue:i.pointsValue,remark:i.remark||''}))}
async function submitReport(row:WorkReport){await reportApi.submit(row.id);ElMessage.success('提交成功');loadReports()}
async function deleteReport(row:WorkReport){
  try{await ElMessageBox.confirm('确定删除吗？','提示',{type:'warning'});await reportApi.remove(row.id);ElMessage.success('已删除');loadReports()}
  catch{}
}
</script>
