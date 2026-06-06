<!-- 个人中心页面 - 修改密码 -->
<template>
  <div>
    <h2>个人中心</h2>
    <el-card style="max-width:500px;margin-top:16px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户名">{{ userStore.userInfo?.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ userStore.userInfo?.realName }}</el-descriptions-item>
        <el-descriptions-item label="角色"><el-tag size="small">{{ roleName }}</el-tag></el-descriptions-item>
      </el-descriptions>
    </el-card>
    <el-card style="max-width:500px;margin-top:16px">
      <template #header>修改密码</template>
      <el-form :model="pwdForm" label-width="80px">
        <el-form-item label="原密码"><el-input v-model="pwdForm.oldPassword" type="password" /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" /></el-form-item>
        <el-form-item label="确认密码"><el-input v-model="pwdForm.confirmPassword" type="password" /></el-form-item>
        <el-form-item><el-button type="primary" @click="handleChangePwd" :loading="saving">修改密码</el-button></el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const saving = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const roleName = computed(() => {
  const map: Record<string, string> = { SECTION_ADMIN: '段级管理员', WORKSHOP_ADMIN: '车间管理员', AREA_REPORTER: '工区填报员' }
  return map[userStore.roleCode || ''] || '未知'
})

async function handleChangePwd() {
  if (pwdForm.newPassword !== pwdForm.confirmPassword) { ElMessage.error('两次密码不一致'); return }
  saving.value = true
  try {
    await userStore.changePassword(pwdForm.oldPassword, pwdForm.newPassword)
    ElMessage.success('密码修改成功'); pwdForm.oldPassword = ''; pwdForm.newPassword = ''; pwdForm.confirmPassword = ''
  } catch (e: any) { ElMessage.error(e.message || '修改失败') }
  finally { saving.value = false }
}
</script>
