import request from './index'
import type { WorkReport, WorkReportItem, PageResult } from '@/types'

/**
 * 工时工分填报 API 接口（核心业务模块）
 */
export const reportApi = {
  /** 分页查询填报记录 */
  getPage(params: Record<string, any>): Promise<PageResult<WorkReport>> {
    return request.get('/reports/page', { params })
  },

  /** 获取填报详情（含明细） */
  getDetail(id: number): Promise<WorkReport> {
    return request.get(`/reports/${id}`)
  },

  /** 创建填报记录（含明细项） */
  create(data: {
    periodId: number
    employeeId: number
    workDate: string
    reportType: string
    items: {
      workItemId: number
      numberValue?: number
      textValue?: string
      remark?: string
    }[]
  }): Promise<WorkReport> {
    return request.post('/reports', data)
  },

  /** 更新填报记录 */
  update(id: number, data: {
    items: {
      workItemId: number
      numberValue?: number
      textValue?: string
      remark?: string
    }[]
    remark?: string
  }): Promise<WorkReport> {
    return request.put(`/reports/${id}`, data)
  },

  /** 删除填报记录 */
  remove(id: number): Promise<void> {
    return request.delete(`/reports/${id}`)
  },

  /** 提交填报数据（草稿 -> 已提交） */
  submit(id: number): Promise<void> {
    return request.post(`/reports/${id}/submit`)
  },

  /** 批量提交填报数据 */
  batchSubmit(ids: number[]): Promise<void> {
    return request.post('/reports/batch-submit', { ids })
  },

  /** 查询某人员某月某类型的填报记录 */
  getByEmployeePeriod(params: {
    periodId: number
    employeeId: number
    reportType: string
  }): Promise<WorkReport[]> {
    return request.get('/reports/by-employee-period', { params })
  }
}
