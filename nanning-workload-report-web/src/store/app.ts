import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 应用全局状态管理 Store
 * 管理侧边栏折叠、当前活跃月份等全局状态
 */
export const useAppStore = defineStore('app', () => {
  /** 侧边栏是否折叠 */
  const sidebarCollapsed = ref(false)

  /** 当前选中的月度期间（用于填报和查看） */
  const currentPeriodId = ref<number | null>(null)
  const currentPeriodName = ref<string>('')

  /** 切换侧边栏折叠状态 */
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  /** 设置当前月度期间 */
  function setCurrentPeriod(periodId: number, periodName: string) {
    currentPeriodId.value = periodId
    currentPeriodName.value = periodName
  }

  return {
    sidebarCollapsed,
    currentPeriodId,
    currentPeriodName,
    toggleSidebar,
    setCurrentPeriod
  }
})
