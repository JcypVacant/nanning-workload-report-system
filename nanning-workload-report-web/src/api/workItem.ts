import request from './index'
import type { WorkItem } from '@/types'

/**
 * 用工项目字典 API 接口
 */
export const workItemApi = {
  /** 获取用工项目树 */
  getTree(): Promise<WorkItem[]> {
    return request.get('/work-items/tree')
  },

  /** 获取所有叶子节点（可填写的最终项目） */
  getLeaves(reportType?: string): Promise<WorkItem[]> {
    return request.get('/work-items/leaves', { params: { reportType } })
  },

  /** 新增用工项目 */
  create(data: Partial<WorkItem>): Promise<WorkItem> {
    return request.post('/work-items', data)
  },

  /** 编辑用工项目 */
  update(id: number, data: Partial<WorkItem>): Promise<WorkItem> {
    return request.put(`/work-items/${id}`, data)
  },

  /** 删除用工项目 */
  remove(id: number): Promise<void> {
    return request.delete(`/work-items/${id}`)
  },

  /** 启停用工项目 */
  toggleEnabled(id: number): Promise<void> {
    return request.patch(`/work-items/${id}/toggle`)
  }
}
