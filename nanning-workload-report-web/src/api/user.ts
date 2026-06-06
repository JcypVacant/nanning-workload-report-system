import request from './index'
import type { SysUser, PageResult } from '@/types'

/**
 * 用户账号管理 API 接口
 */
export const userApi = {
  /** 分页查询用户列表 */
  getPage(params: Record<string, any>): Promise<PageResult<SysUser>> {
    return request.get('/users/page', { params })
  },

  /** 新增用户 */
  create(data: Partial<SysUser>): Promise<SysUser> {
    return request.post('/users', data)
  },

  /** 编辑用户 */
  update(id: number, data: Partial<SysUser>): Promise<SysUser> {
    return request.put(`/users/${id}`, data)
  },

  /** 删除用户 */
  remove(id: number): Promise<void> {
    return request.delete(`/users/${id}`)
  },

  /** 重置密码 */
  resetPassword(id: number): Promise<void> {
    return request.patch(`/users/${id}/reset-password`)
  },

  /** 启停用户 */
  toggleEnabled(id: number): Promise<void> {
    return request.patch(`/users/${id}/toggle`)
  }
}
