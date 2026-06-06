<!-- 操作日志页面 -->
<template>
  <div>
    <h2>操作日志</h2>
    <el-card style="margin-top:16px">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="operateTime" label="时间" width="180"/>
        <el-table-column prop="username" label="用户" width="120"/>
        <el-table-column prop="moduleName" label="模块" width="120"/>
        <el-table-column prop="operationType" label="操作类型" width="100"/>
        <el-table-column prop="summary" label="摘要" min-width="200"/>
        <el-table-column prop="ip" label="IP" width="140"/>
      </el-table>
      <el-pagination v-model:current-page="pageNum" :total="total" :page-size="10" layout="total,prev,pager,next" @change="loadData" style="margin-top:16px;justify-content:flex-end"/>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { logApi } from '@/api/log'
import type { OperationLog } from '@/types'
const tableData=ref<OperationLog[]>([]); const loading=ref(false); const pageNum=ref(1); const total=ref(0)
onMounted(loadData)
async function loadData(){loading.value=true;try{const res=await logApi.getPage({pageNum:pageNum.value,pageSize:10});tableData.value=res.records;total.value=res.total}finally{loading.value=false}}
</script>
