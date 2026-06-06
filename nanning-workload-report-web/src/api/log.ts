import request from './index'
import type { OperationLog, PageResult } from '@/types'

/**
 * 操作日志 API 接口
 */
export const logApi = {
  /** 分页查询操作日志 */
  getPage(params: Record<string, any>): Promise<PageResult<OperationLog>> {
    return request.get('/operation-logs/page', { params })
  }
}
