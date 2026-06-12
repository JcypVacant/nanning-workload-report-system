import request from './index'
import type { Employee, EmployeeTransferRecord, PageResult } from '@/types'

/**
 * 人员管理 API 接口
 */
export const employeeApi = {
  /** 分页查询人员列表 */
  getPage(params: Record<string, any>): Promise<PageResult<Employee>> {
    return request.get('/employees/page', { params })
  },

  /** 根据工区ID查询人员 */
  getByArea(areaId: number): Promise<Employee[]> {
    return request.get(`/employees/by-area/${areaId}`)
  },

  /** 新增人员 */
  create(data: Partial<Employee>): Promise<Employee> {
    return request.post('/employees', data)
  },

  /** 编辑人员 */
  update(id: number, data: Partial<Employee>): Promise<Employee> {
    return request.put(`/employees/${id}`, data)
  },

  /** 启停人员 */
  toggleEnabled(id: number): Promise<void> {
    return request.patch(`/employees/${id}/toggle`)
  },

  /** 人员调动 */
  transfer(data: {
    employeeId: number
    afterWorkshopId: number
    afterAreaId: number
    afterTeamName?: string
    transferReason?: string
  }): Promise<void> {
    return request.post('/employees/transfer', data)
  },

  /** 查询人员调动记录 */
  getTransferRecords(employeeId: number): Promise<EmployeeTransferRecord[]> {
    return request.get(`/employees/${employeeId}/transfer-records`)
  },

  /** 分页查询调动记录 */
  getTransferRecordsPage(params: Record<string, any>): Promise<PageResult<EmployeeTransferRecord>> {
    return request.get('/employees/transfer-records/page', { params })
  },

  /** 审核调动申请 */
  approveTransfer(recordId: number, approved: boolean, comment?: string): Promise<void> {
    return request.post(`/employees/transfer/${recordId}/approve`, { approved, comment })
  }
}
