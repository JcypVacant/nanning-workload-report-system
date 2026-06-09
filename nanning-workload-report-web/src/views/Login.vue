<!--
  Login.vue - 系统登录页面
  用户通过账号和密码登录系统，系统根据用户角色加载对应菜单
  所有账号由段级管理员统一创建，不提供用户自主注册功能
-->
<template>
  <div class="login-container">
    <div class="login-card">
      <!-- 系统标题 -->
      <div class="login-header">
        <h1 class="system-title">南宁通信段</h1>
        <p class="system-subtitle">劳动用工工时工分统计管理系统</p>
      </div>

      <!-- 登录表单 -->
      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入账号"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <!-- 记住密码 -->
        <el-form-item>
          <el-checkbox v-model="rememberPwd">记住密码</el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 底部提示 -->
      <div class="login-footer">
        <span>内部管理系统 · 账号由段级管理员统一分配</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/store/user'

/** 路由实例 */
const router = useRouter()

/** 用户 Store */
const userStore = useUserStore()

/** 表单组件引用 */
const formRef = ref<FormInstance>()

/** 登录加载状态 */
const loading = ref(false)

/** 记住密码 */
const rememberPwd = ref(true)

/** 登录表单数据 */
const loginForm = reactive({
  username: '',
  password: ''
})

// 页面加载时恢复保存的账号密码
onMounted(() => {
  const savedUser = localStorage.getItem('remembered_username')
  const savedPwd = localStorage.getItem('remembered_password')
  if (savedUser) loginForm.username = savedUser
  if (savedPwd) loginForm.password = savedPwd
})

/** 表单验证规则 */
const rules: FormRules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 5, max: 20, message: '账号长度为5-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ]
}

/**
 * 处理登录操作
 * 1. 表单验证
 * 2. 调用登录API
 * 3. 跳转到首页
 */
async function handleLogin() {
  // 表单验证
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    // 调用登录API
    await userStore.login(loginForm.username, loginForm.password)
    // 记住密码
    if (rememberPwd.value) {
      localStorage.setItem('remembered_username', loginForm.username)
      localStorage.setItem('remembered_password', loginForm.password)
    } else {
      localStorage.removeItem('remembered_username')
      localStorage.removeItem('remembered_password')
    }
    ElMessage.success('登录成功')
    // 跳转到首页看板
    router.push('/dashboard')
  } catch {
    // axios 拦截器已统一显示错误消息，此处无需重复提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1a3a52 0%, #2c5f8a 50%, #4a90b8 100%);
  padding: 20px;
}

.login-card {
  width: 420px;
  padding: 40px 36px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 36px;

  .system-title {
    font-size: 24px;
    font-weight: 700;
    color: #1a3a52;
    margin: 0 0 8px;
    letter-spacing: 2px;
  }

  .system-subtitle {
    font-size: 14px;
    color: #666;
    margin: 0;
  }
}

.login-form {
  .login-button {
    width: 100%;
    height: 44px;
    font-size: 16px;
    letter-spacing: 4px;
  }
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 12px;
  color: #999;
}
</style>
