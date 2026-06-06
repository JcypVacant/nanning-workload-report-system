<!-- Excel导出页面 -->
<template>
  <div>
    <h2>Excel 导出</h2>
    <el-card style="margin-top:16px;max-width:600px">
      <el-form label-width="100px">
        <el-form-item label="导出范围"><el-select v-model="scope" style="width:200px"><el-option label="本工区" value="area" v-if="userStore.isAreaReporter"/><el-option label="本车间" value="workshop" v-if="userStore.isWorkshopAdmin"/><el-option label="全段" value="section" v-if="userStore.isSectionAdmin"/></el-select></el-form-item>
        <el-form-item label="月份"><el-select v-model="periodId" placeholder="选择月份"><el-option v-for="p in periods" :key="p.id" :label="p.periodName" :value="p.id"/></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="handleExport" :loading="exporting">导出Excel</el-button></el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { exportApi } from '@/api/export'
import { periodApi } from '@/api/period'
import { useUserStore } from '@/store/user'
import type { MonthlyPeriod } from '@/types'

const userStore = useUserStore()
const scope = ref(userStore.isAreaReporter?'area':userStore.isWorkshopAdmin?'workshop':'section')
const periodId = ref<number|null>(null)
const periods = ref<MonthlyPeriod[]>([])
const exporting = ref(false)

onMounted(async()=>{const res=await periodApi.getPage({pageNum:1,pageSize:100});periods.value=res.records})

async function handleExport(){
  if(!periodId.value){ElMessage.warning('请选择月份');return}
  exporting.value=true
  try{
    const orgId=userStore.orgId||0
    let blob:Blob
    if(scope.value==='area') blob=await exportApi.exportArea(orgId,periodId.value)
    else if(scope.value==='workshop') blob=await exportApi.exportWorkshop(orgId,periodId.value)
    else blob=await exportApi.exportSection(periodId.value)
    const url=URL.createObjectURL(blob)
    const a=document.createElement('a');a.href=url;a.download='export.xlsx';a.click();URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  }catch(e:any){ElMessage.error(e.message||'导出失败')}
  finally{exporting.value=false}
}
</script>
