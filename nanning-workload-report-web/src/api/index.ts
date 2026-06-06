import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import type { ApiResult } from '@/types'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * Axios 实例配置
 * 创建统一的 HTTP 请求客户端，配置：
 * 1. 基础 URL（从环境变量读取）
 * 2. 请求超时时间
 * 3. 请求拦截器（自动添加 Token）
 * 4. 响应拦截器（统一错误处理）
 */
const service: AxiosInstance = axios.create({
  // API 基础路径
  baseURL: import.meta.env.VITE_API_BASE || '/api/v1',
  // 请求超时时间（30秒）
  timeout: 30000,
  // 跨域请求时携带 Cookie
  withCredentials: true
})

/**
 * 请求拦截器
 * 在每次请求发出前，自动添加 Sa-Token 认证头
 */
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 从 localStorage 或 Pinia store 获取 Token
    const token = localStorage.getItem('satoken')
    if (token && config.headers) {
      // Sa-Token 使用 satoken 请求头传递 Token
      config.headers['satoken'] = token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 统一处理 API 响应：
 * - 成功：提取 data 字段返回
 * - 未登录(401)：跳转到登录页
 * - 权限不足(403)：提示用户
 * - 其他错误：显示错误消息
 */
service.interceptors.response.use(
  (response: AxiosResponse<ApiResult>) => {
    const res = response.data

    // 如果响应码不是 200，说明业务逻辑有误
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')

      // 401 未授权 -> 清除 Token 并跳转登录页
      if (res.code === 401) {
        localStorage.removeItem('satoken')
        localStorage.removeItem('userInfo')
        router.push('/login')
      }

      return Promise.reject(new Error(res.msg || '请求失败'))
    }

    // 直接返回 data 字段，方便调用方使用
    return res.data as any
  },
  (error) => {
    // HTTP 错误处理
    if (error.response) {
      const { status } = error.response
      switch (status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          localStorage.removeItem('satoken')
          localStorage.removeItem('userInfo')
          router.push('/login')
          break
        case 403:
          ElMessage.error('权限不足，无法执行此操作')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误，请联系管理员')
          break
        default:
          ElMessage.error('网络错误，请稍后重试')
      }
    } else {
      ElMessage.error('网络连接失败，请检查网络')
    }
    return Promise.reject(error)
  }
)

export default service
