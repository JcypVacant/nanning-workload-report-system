<!-- 审核记录列表页面 -->
<template>
  <div>
    <h2>审核记录</h2>
    <el-card style="margin-top:16px">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column label="时间" width="180">
          <template #default="{ row }">{{ formatTime(row.operateTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }"><el-tag size="small">{{ actionMap[row.action] || row.action }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="comment" label="备注" min-width="200" />
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
import { auditApi } from '@/api/audit'
import type { AuditRecord } from '@/types'

const actionMap: Record<string, string> = { SUBMIT: '提交', APPROVE: '通过', RETURN: '退回', LOCK: '锁定', UNLOCK: '解锁' }
const tableData = ref<AuditRecord[]>([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

function formatTime(t: string) {
  if (!t) return '-'
  return t.replace('T', ' ')
}

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await auditApi.getAuditRecords({ pageNum: pageNum.value, pageSize: pageSize.value })
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handleSizeChange() {
  pageNum.value = 1
  loadData()
}
</script>

<style scoped>
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
