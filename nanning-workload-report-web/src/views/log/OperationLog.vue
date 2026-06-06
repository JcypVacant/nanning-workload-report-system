<!-- 操作日志页面 - 查看系统所有关键操作记录 -->
<template>
  <div>
    <h2>操作日志</h2>
    <el-card style="margin-top:16px">
      <!-- 筛选栏 -->
      <el-form :inline="true" class="search-bar">
        <el-form-item label="操作模块">
          <el-select v-model="filterModule" placeholder="全部模块" clearable style="width:160px" @change="handleSearch">
            <el-option label="用户认证" value="用户认证" />
            <el-option label="账号管理" value="账号管理" />
            <el-option label="人员管理" value="人员管理" />
            <el-option label="组织管理" value="组织管理" />
            <el-option label="用工项目字典" value="用工项目字典" />
            <el-option label="月度期间管理" value="月度期间管理" />
            <el-option label="工区填报" value="工区填报" />
            <el-option label="车间审核" value="车间审核" />
            <el-option label="Excel导出" value="Excel导出" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="operateTime" label="操作时间" width="175" sortable />
        <el-table-column prop="username" label="操作用户" width="120" />
        <el-table-column prop="moduleName" label="操作模块" width="120">
          <template #default="{row}">
            <el-tag size="small" type="info">{{ row.moduleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="{row}">
            <el-tag size="small" :type="typeColor(row.operationType)">{{ row.operationType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="summary" label="操作摘要" min-width="250" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="140" />
      </el-table>

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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { logApi } from '@/api/log'
import type { OperationLog } from '@/types'

const tableData = ref<OperationLog[]>([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterModule = ref('')

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await logApi.getPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      moduleName: filterModule.value || undefined
    })
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handleSearch() { pageNum.value = 1; loadData() }
function handleReset() { filterModule.value = ''; pageNum.value = 1; loadData() }
function handleSizeChange() { pageNum.value = 1; loadData() }

function typeColor(type: string) {
  const m: Record<string, string> = {
    LOGIN: 'success', CREATE: 'primary', UPDATE: 'warning',
    DELETE: 'danger', SUBMIT: '', APPROVE: 'success',
    RETURN: 'danger', EXPORT: 'info', TRANSFER: 'warning'
  }
  return m[type] || 'info'
}
</script>

<style scoped>
.search-bar { margin-bottom: 8px }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px }
</style>
