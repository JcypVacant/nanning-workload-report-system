<!-- 审核记录列表页面 -->
<template>
  <div>
    <h2>审核记录</h2>
    <el-card style="margin-top:16px">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="operateTime" label="时间" width="180"/>
        <el-table-column prop="action" label="操作" width="100"><template #default="{row}"><el-tag size="small">{{actionMap[row.action]||row.action}}</el-tag></template></el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="120"/>
        <el-table-column prop="comment" label="备注" min-width="200"/>
      </el-table>
      <el-pagination v-model:current-page="pageNum":total="total":page-size="10" layout="total,prev,pager,next" @change="loadData" style="margin-top:16px;justify-content:flex-end"/>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { auditApi } from '@/api/audit'
import type { AuditRecord } from '@/types'

const actionMap:Record<string,string>={SUBMIT:'提交',APPROVE:'通过',RETURN:'退回',LOCK:'锁定',UNLOCK:'解锁'}
const tableData=ref<AuditRecord[]>([]);const loading=ref(false);const pageNum=ref(1);const total=ref(0)

onMounted(loadData)
async function loadData(){
  loading.value=true
  try{const res=await auditApi.getAuditRecords({pageNum:pageNum.value,pageSize:10});tableData.value=res.records;total.value=res.total}
  finally{loading.value=false}
}
</script>
