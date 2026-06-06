import request from './index'
import type { MonthlyPeriod, PageResult } from '@/types'

/**
 * 月度填报期间管理 API 接口
 */
export const periodApi = {
  /** 分页查询月度期间 */
  getPage(params: Record<string, any>): Promise<PageResult<MonthlyPeriod>> {
    return request.get('/periods/page', { params })
  },

  /** 获取当前活跃月份 */
  getActive(): Promise<MonthlyPeriod | null> {
    return request.get('/periods/active')
  },

  /** 新增月度期间 */
  create(data: { year: number; month: number; startDate?: string; endDate?: string; auditDeadline?: string }): Promise<MonthlyPeriod> {
    return request.post('/periods', data)
  },

  /** 更新月度期间 */
  update(id: number, data: Partial<MonthlyPeriod>): Promise<MonthlyPeriod> {
    return request.put(`/periods/${id}`, data)
  },

  /** 修改期间状态 */
  changeStatus(id: number, status: string): Promise<void> {
    return request.patch(`/periods/${id}/status`, { status })
  },

  /** 锁定/解锁月份 */
  toggleLock(id: number): Promise<void> {
    return request.patch(`/periods/${id}/lock`)
  }
}
