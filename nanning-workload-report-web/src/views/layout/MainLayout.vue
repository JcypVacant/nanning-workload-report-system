<!--
  MainLayout.vue - 主布局组件
  包含侧边栏导航、顶部工具栏和主内容区域
  根据不同角色动态显示菜单项
-->
<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="sidebarWidth" class="layout-aside">
      <div class="logo-area">
        <el-icon :size="24" color="#fff"><Odometer /></el-icon>
        <span v-show="!appStore.sidebarCollapsed" class="logo-text">南宁通信段工时工分系统</span>
      </div>

      <div class="menu-scroll-area">
        <el-menu
          :default-active="activeMenu"
          :collapse="appStore.sidebarCollapsed"
          :collapse-transition="false"
          background-color="#1a3a52"
          text-color="#c0cdd9"
          active-text-color="#ffffff"
          router
        >
        <!-- 首页看板 -->
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>首页看板</template>
        </el-menu-item>

        <!-- 组织架构管理（仅段级管理员） -->
        <el-menu-item v-if="userStore.isSectionAdmin" index="/org">
          <el-icon><OfficeBuilding /></el-icon>
          <template #title>组织架构管理</template>
        </el-menu-item>

        <!-- 账号管理（仅段级管理员） -->
        <el-menu-item v-if="userStore.isSectionAdmin" index="/users">
          <el-icon><UserFilled /></el-icon>
          <template #title>账号管理</template>
        </el-menu-item>

        <!-- 人员管理（段级+车间管理员） -->
        <el-menu-item v-if="!userStore.isAreaReporter" index="/employees">
          <el-icon><Avatar /></el-icon>
          <template #title>人员管理</template>
        </el-menu-item>

        <!-- 用工项目字典（仅段级管理员） -->
        <el-menu-item v-if="userStore.isSectionAdmin" index="/work-items">
          <el-icon><Notebook /></el-icon>
          <template #title>用工项目字典</template>
        </el-menu-item>

        <!-- 月度填报管理（仅段级管理员） -->
        <el-menu-item v-if="userStore.isSectionAdmin" index="/periods">
          <el-icon><Calendar /></el-icon>
          <template #title>月度填报管理</template>
        </el-menu-item>

        <!-- 工区填报（工区填报员） -->
        <el-menu-item v-if="userStore.isAreaReporter" index="/report/fill">
          <el-icon><EditPen /></el-icon>
          <template #title>工区填报</template>
        </el-menu-item>

        <!-- 车间本级填报（车间管理员） -->
        <el-menu-item v-if="userStore.isWorkshopAdmin" index="/report/workshop">
          <el-icon><Edit /></el-icon>
          <template #title>车间本级填报</template>
        </el-menu-item>

        <!-- 段级审核（仅段级管理员） -->
        <el-menu-item v-if="userStore.isSectionAdmin" index="/audit/section">
          <el-icon><Checked /></el-icon>
          <template #title>段级审核</template>
        </el-menu-item>

        <!-- 填报进度管理（仅段级管理员） -->
        <el-menu-item v-if="userStore.isSectionAdmin" index="/progress">
          <el-icon><DataBoard /></el-icon>
          <template #title>填报进度管理</template>
        </el-menu-item>

        <!-- 车间审核（车间管理员） -->
        <el-menu-item v-if="userStore.isWorkshopAdmin" index="/audit/review">
          <el-icon><Checked /></el-icon>
          <template #title>车间审核</template>
        </el-menu-item>

        <!-- 汇总管理（段级+车间管理员） -->
        <el-menu-item v-if="!userStore.isAreaReporter" index="/summary">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>汇总管理</template>
        </el-menu-item>

        <!-- 统计分析 -->
        <el-menu-item index="/statistics">
          <el-icon><PieChart /></el-icon>
          <template #title>统计分析</template>
        </el-menu-item>

        <!-- Excel导出 -->
        <el-menu-item index="/export">
          <el-icon><Download /></el-icon>
          <template #title>Excel导出</template>
        </el-menu-item>

        <!-- 审核记录 -->
        <el-menu-item index="/audit/records">
          <el-icon><Tickets /></el-icon>
          <template #title>审核记录</template>
        </el-menu-item>

        <!-- 操作日志（仅段级管理员） -->
        <el-menu-item v-if="userStore.isSectionAdmin" index="/logs">
          <el-icon><Document /></el-icon>
          <template #title>操作日志</template>
        </el-menu-item>

        <!-- 个人中心 -->
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <template #title>个人中心</template>
        </el-menu-item>
      </el-menu>
      </div>
    </el-aside>

    <!-- 右侧内容区域 -->
    <el-container class="layout-right">
      <!-- 顶部工具栏 -->
      <el-header class="layout-header">
        <div class="header-left">
          <!-- 折叠按钮 -->
          <el-icon class="collapse-btn" @click="appStore.toggleSidebar()">
            <Fold v-if="!appStore.sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
          <!-- 面包屑 -->
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <span class="user-info">
            {{ userStore.userInfo?.realName || '用户' }}
            <el-tag size="small" type="info" class="role-tag">
              {{ roleName }}
            </el-tag>
          </span>
          <el-dropdown @command="handleCommand">
            <el-avatar :size="32" style="cursor: pointer; margin-left: 12px;">
              {{ (userStore.userInfo?.realName || 'U').charAt(0) }}
            </el-avatar>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区域 -->
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

/** 当前活跃菜单项 */
const activeMenu = computed(() => route.path)

/** 当前页面标题 */
const currentTitle = computed(() => route.meta?.title as string || '')

/** 侧边栏宽度 */
const sidebarWidth = computed(() =>
  appStore.sidebarCollapsed ? '64px' : '220px'
)

/** 角色中文名称 */
const roleName = computed(() => {
  const map: Record<string, string> = {
    SECTION_ADMIN: '段级管理员',
    WORKSHOP_ADMIN: '车间管理员',
    AREA_REPORTER: '工区填报员'
  }
  return map[userStore.roleCode || ''] || '未知'
})

/**
 * 处理用户下拉菜单命令
 */
function handleCommand(command: string) {
  if (command === 'logout') {
    userStore.logout()
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped lang="scss">
.main-layout {
  height: 100vh;
}

.layout-aside {
  display: flex;
  flex-direction: column;
  background-color: #1a3a52;
  overflow: hidden;
  transition: width 0.3s;

  .logo-area {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 56px;
    min-height: 56px;
    padding: 0 16px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);

    .logo-icon {
      width: 28px;
      height: 28px;
    }

    .logo-text {
      margin-left: 8px;
      font-size: 15px;
      font-weight: 600;
      color: #fff;
      white-space: nowrap;
    }
  }

  .menu-scroll-area {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;

    /* 美化滚动条 */
    &::-webkit-scrollbar {
      width: 5px;
    }
    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.2);
      border-radius: 3px;
    }
    &::-webkit-scrollbar-track {
      background: transparent;
    }

    /* el-menu 撑满高度，保持折叠动画正常 */
    .el-menu {
      border-right: none;
    }
  }
}

.layout-right {
  flex-direction: column;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);

  .header-left {
    display: flex;
    align-items: center;

    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      margin-right: 16px;
      color: #666;

      &:hover {
        color: var(--el-color-primary);
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;

    .user-info {
      font-size: 14px;
      color: #333;

      .role-tag {
        margin-left: 8px;
      }
    }
  }
}

.layout-main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
