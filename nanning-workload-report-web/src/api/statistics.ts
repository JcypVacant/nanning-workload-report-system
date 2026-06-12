import request from './index'
import type { DashboardData } from '@/types'

/**
 * 统计分析和仪表盘 API 接口
 */
export const statisticsApi = {
  /** 获取仪表盘数据 */
  getDashboard(): Promise<DashboardData> {
    return request.get('/dashboard')
  },

  /** 获取待办提醒 */
  getNotifications(): Promise<any[]> {
    return request.get('/dashboard/notifications')
  },

  /** 获取车间对比数据 */
  getByWorkshop(params: Record<string, any>): Promise<any> {
    return request.get('/statistics/by-workshop', { params })
  },

  /** 获取工区对比数据 */
  getByArea(params: Record<string, any>): Promise<any> {
    return request.get('/statistics/by-area', { params })
  },

  /** 获取人员排名数据 */
  getByEmployee(params: Record<string, any>): Promise<any> {
    return request.get('/statistics/by-employee', { params })
  },

  /** 获取用工项目占比数据 */
  getByProject(params: Record<string, any>): Promise<any> {
    return request.get('/statistics/by-project', { params })
  },

  /** 获取月份趋势数据 */
  getTrend(params: Record<string, any>): Promise<any> {
    return request.get('/statistics/trend', { params })
  },

  /** 获取填报状态统计 */
  getStatusStats(params: Record<string, any>): Promise<any> {
    return request.get('/statistics/status', { params })
  }
}
