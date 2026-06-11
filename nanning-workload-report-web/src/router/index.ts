import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

/**
 * Vue Router 路由配置
 * 定义所有页面的路由映射和权限控制
 *
 * 路由元信息 meta 说明：
 * - title：页面标题（用于面包屑和标签页）
 * - role：允许访问的角色，单个字符串或字符串数组，不设置表示所有角色可访问
 * - noAuth：true 表示不需要登录即可访问（仅登录页）
 */
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', noAuth: true }
  },
  {
    path: '/',
    component: () => import('@/views/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      // 首页看板
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '首页看板' }
      },
      // 组织架构管理（仅段级管理员）
      {
        path: 'org',
        name: 'OrgManage',
        component: () => import('@/views/org/OrgManage.vue'),
        meta: { title: '组织架构管理', role: 'SECTION_ADMIN' }
      },
      // 账号管理（仅段级管理员）
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('@/views/user/UserManage.vue'),
        meta: { title: '账号管理', role: 'SECTION_ADMIN' }
      },
      // 人员管理（段级+车间管理员）
      {
        path: 'employees',
        name: 'EmployeeList',
        component: () => import('@/views/employee/EmployeeList.vue'),
        meta: { title: '人员管理', role: ['SECTION_ADMIN', 'WORKSHOP_ADMIN'] }
      },
      // 用工项目字典（仅段级管理员）
      {
        path: 'work-items',
        name: 'WorkItemManage',
        component: () => import('@/views/workItem/WorkItemManage.vue'),
        meta: { title: '用工项目字典', role: 'SECTION_ADMIN' }
      },
      // 月度填报管理（仅段级管理员）
      {
        path: 'periods',
        name: 'PeriodManage',
        component: () => import('@/views/period/PeriodManage.vue'),
        meta: { title: '月度填报管理', role: 'SECTION_ADMIN' }
      },
      // 工区填报（段级+工区填报员）
      {
        path: 'report/fill',
        name: 'WorkReportFill',
        component: () => import('@/views/report/WorkReportFill.vue'),
        meta: { title: '工区填报', role: ['SECTION_ADMIN', 'AREA_REPORTER'] }
      },
      // 车间本级填报（段级+车间管理员）
      {
        path: 'report/workshop',
        name: 'WorkshopFill',
        component: () => import('@/views/report/WorkshopLevelFill.vue'),
        meta: { title: '车间本级填报', role: ['SECTION_ADMIN', 'WORKSHOP_ADMIN'] }
      },
      // 车间审核（段级+车间管理员）
      {
        path: 'audit/review',
        name: 'WorkshopReview',
        component: () => import('@/views/audit/WorkshopReview.vue'),
        meta: { title: '车间审核', role: ['SECTION_ADMIN', 'WORKSHOP_ADMIN'] }
      },
      // 审核记录（所有角色）
      {
        path: 'audit/records',
        name: 'AuditRecords',
        component: () => import('@/views/audit/AuditRecordList.vue'),
        meta: { title: '审核记录' }
      },
      // 汇总管理（段级+车间管理员）
      {
        path: 'summary',
        name: 'SummaryManage',
        component: () => import('@/views/summary/SummaryManage.vue'),
        meta: { title: '汇总管理', role: ['SECTION_ADMIN', 'WORKSHOP_ADMIN'] }
      },
      // 统计分析（所有角色，不同数据范围）
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/Statistics.vue'),
        meta: { title: '统计分析' }
      },
      // Excel 导出
      {
        path: 'export',
        name: 'ExcelExport',
        component: () => import('@/views/export/ExcelExport.vue'),
        meta: { title: 'Excel导出' }
      },
      // 操作日志（仅段级管理员）
      {
        path: 'logs',
        name: 'OperationLog',
        component: () => import('@/views/log/OperationLog.vue'),
        meta: { title: '操作日志', role: 'SECTION_ADMIN' }
      },
      // 个人中心
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 全局路由守卫
 * 在每次路由跳转前校验：
 * 1. 是否需要登录（检查 Token 是否存在）
 * 2. 是否有访问该路由的角色权限
 */
router.beforeEach(async (to, _from, next) => {
  // 登录页不需要认证
  if (to.meta.noAuth) {
    next()
    return
  }

  const userStore = useUserStore()

  // 检查是否已登录
  const token = localStorage.getItem('satoken')
  if (!token) {
    next('/login')
    return
  }

  // 如果用户信息尚未加载，则从后端获取
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch {
      // 获取用户信息失败，清除 Token 并跳转到登录页
      localStorage.removeItem('satoken')
      next('/login')
      return
    }
  }

  // 角色权限检查
  if (to.meta.role) {
    const requiredRoles = Array.isArray(to.meta.role) ? to.meta.role : [to.meta.role]
    const userRole = userStore.roleCode
    if (userRole && !requiredRoles.includes(userRole)) {
      // 权限不足，重定向到首页
      ElMessage.warning('您没有访问该页面的权限')
      next('/dashboard')
      return
    }
  }

  next()
})

export default router
