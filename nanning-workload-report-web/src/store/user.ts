import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, RoleCode } from '@/types'
import { authApi } from '@/api/auth'
import router from '@/router'

/**
 * 用户状态管理 Store
 * 管理当前登录用户的信息、Token、角色权限
 * 提供登录、登出、获取用户信息等操作
 */
export const useUserStore = defineStore('user', () => {
  // ==================== 状态 ====================

  /** 当前登录的 Token */
  const token = ref<string>(localStorage.getItem('satoken') || '')

  /** 用户信息 */
  const userInfo = ref<UserInfo | null>(null)

  // ==================== 计算属性 ====================

  /** 用户角色编码 */
  const roleCode = computed<RoleCode | null>(() => userInfo.value?.roleCode || null)

  /** 用户关联的组织ID */
  const orgId = computed<number | null>(() => userInfo.value?.orgId ?? null)

  /** 用户数据范围类型 */
  const scopeType = computed<string>(() => userInfo.value?.scopeType || '')

  /** 是否已登录 */
  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)

  /** 是否为段级管理员 */
  const isSectionAdmin = computed(() => roleCode.value === 'SECTION_ADMIN')

  /** 是否为车间管理员 */
  const isWorkshopAdmin = computed(() => roleCode.value === 'WORKSHOP_ADMIN')

  /** 是否为工区填报员 */
  const isAreaReporter = computed(() => roleCode.value === 'AREA_REPORTER')

  // ==================== 操作 ====================

  /**
   * 登录
   * @param username 用户名
   * @param password 密码
   */
  async function login(username: string, password: string): Promise<void> {
    const result = await authApi.login({ username, password })
    token.value = result.token
    userInfo.value = result.userInfo
    // 持久化存储 Token
    localStorage.setItem('satoken', result.token)
    localStorage.setItem('userInfo', JSON.stringify(result.userInfo))
  }

  /**
   * 从后端获取当前用户信息
   */
  async function fetchUserInfo(): Promise<void> {
    const info = await authApi.getUserInfo()
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  /**
   * 登出
   */
  async function logout(): Promise<void> {
    try {
      await authApi.logout()
    } finally {
      // 清除本地状态
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('satoken')
      localStorage.removeItem('userInfo')
      router.push('/login')
    }
  }

  /**
   * 修改密码
   */
  async function changePassword(oldPassword: string, newPassword: string): Promise<void> {
    await authApi.changePassword({ oldPassword, newPassword })
  }

  return {
    token,
    userInfo,
    roleCode,
    orgId,
    scopeType,
    isLoggedIn,
    isSectionAdmin,
    isWorkshopAdmin,
    isAreaReporter,
    login,
    fetchUserInfo,
    logout,
    changePassword
  }
})
