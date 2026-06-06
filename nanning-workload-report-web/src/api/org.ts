import request from './index'
import type { OrgUnit, PageResult, PageQuery } from '@/types'

/**
 * 组织架构相关 API 接口
 */
export const orgApi = {
  /** 获取组织树 */
  getTree(): Promise<OrgUnit[]> {
    return request.get('/org/tree')
  },

  /** 获取车间列表（仅车间级别节点） */
  getWorkshops(): Promise<OrgUnit[]> {
    return request.get('/org/workshops')
  },

  /** 获取车间下属工区列表 */
  getAreasByWorkshopId(workshopId: number): Promise<OrgUnit[]> {
    return request.get(`/org/workshops/${workshopId}/areas`)
  },

  /** 新增组织 */
  create(data: Partial<OrgUnit>): Promise<OrgUnit> {
    return request.post('/org', data)
  },

  /** 编辑组织 */
  update(id: number, data: Partial<OrgUnit>): Promise<OrgUnit> {
    return request.put(`/org/${id}`, data)
  },

  /** 删除组织 */
  remove(id: number): Promise<void> {
    return request.delete(`/org/${id}`)
  },

  /** 启停组织 */
  toggleEnabled(id: number): Promise<void> {
    return request.patch(`/org/${id}/toggle`)
  }
}
