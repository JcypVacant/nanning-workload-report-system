import request from './index'
import type { AuditRecord, PageResult } from '@/types'

/**
 * 车间审核相关 API 接口
 */
export const auditApi = {
  /** 查询待审核的工区数据 */
  getPending(params: Record<string, any>): Promise<any[]> {
    return request.get('/audit/pending', { params })
  },

  /** 查询所有状态的填报记录（已提交 + 已审核 + 已退回） */
  getAllReports(params: Record<string, any>): Promise<any[]> {
    return request.get('/audit/reports', { params })
  },

  /** 分页查询所有状态的填报记录 */
  getAllReportsPage(params: Record<string, any>): Promise<any> {
    return request.get('/audit/reports/page', { params })
  },

  /** 审核通过 */
  approve(reportId: number, comment?: string): Promise<void> {
    return request.post(`/audit/${reportId}/approve`, { comment })
  },

  /** 批量审核通过 */
  batchApprove(reportIds: number[], comment?: string): Promise<void> {
    return request.post('/audit/batch-approve', { reportIds, comment })
  },

  /** 批量退回修改 */
  batchReturn(reportIds: number[], comment: string): Promise<void> {
    return request.post('/audit/batch-return', { reportIds, comment })
  },

  /** 查询未填报人员 */
  getUnsubmitted(periodId: number): Promise<any[]> {
    return request.get('/audit/unsubmitted', { params: { periodId } })
  },

  /** 退回修改 */
  returnReport(reportId: number, comment: string): Promise<void> {
    return request.post(`/audit/${reportId}/return`, { comment })
  },

  /** 提交车间数据到段级 */
  submitToSection(periodId: number): Promise<void> {
    return request.post(`/audit/submit-to-section/${periodId}`)
  },

  /** 查询审核记录 */
  getAuditRecords(params: Record<string, any>): Promise<PageResult<AuditRecord>> {
    return request.get('/audit/records', { params })
  }
}
