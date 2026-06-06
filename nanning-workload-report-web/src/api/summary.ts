import request from './index'
import type { WorkshopSummary, SectionSummary } from '@/types'

/**
 * 汇总统计 API 接口
 */
export const summaryApi = {
  /** 车间汇总：按工区分组统计 */
  getWorkshopSummary(periodId: number, workshopId?: number): Promise<WorkshopSummary[]> {
    return request.get('/summary/workshop', { params: { periodId, workshopId } })
  },

  /** 段级汇总：按车间分组统计 */
  getSectionSummary(periodId: number): Promise<SectionSummary[]> {
    return request.get('/summary/section', { params: { periodId } })
  }
}
