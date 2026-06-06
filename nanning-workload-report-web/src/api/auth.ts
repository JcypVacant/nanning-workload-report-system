import request from './index'
import type { LoginRequest, LoginResult, UserInfo, ChangePasswordRequest } from '@/types'

/**
 * 认证相关 API 接口
 * 包含登录、登出、获取用户信息、修改密码等功能
 */
export const authApi = {
  /**
   * 用户登录
   * POST /api/v1/auth/login
   */
  login(data: LoginRequest): Promise<LoginResult> {
    return request.post('/auth/login', data)
  },

  /**
   * 用户登出
   * POST /api/v1/auth/logout
   */
  logout(): Promise<void> {
    return request.post('/auth/logout')
  },

  /**
   * 获取当前登录用户信息
   * GET /api/v1/auth/user-info
   */
  getUserInfo(): Promise<UserInfo> {
    return request.get('/auth/user-info')
  },

  /**
   * 修改当前用户密码
   * PUT /api/v1/auth/password
   */
  changePassword(data: ChangePasswordRequest): Promise<void> {
    return request.put('/auth/password', data)
  }
}
