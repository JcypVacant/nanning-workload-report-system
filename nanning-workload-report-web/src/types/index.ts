/**
 * TypeScript 类型定义文件
 * 定义系统中使用的所有数据接口和类型
 */

// ==================== 通用类型 ====================

/** 统一API响应格式 */
export interface ApiResult<T = any> {
  code: number
  msg: string
  data: T
}

/** 分页查询结果 */
export interface PageResult<T> {
  total: number
  pageNum: number
  pageSize: number
  records: T[]
}

/** 分页查询参数 */
export interface PageQuery {
  pageNum: number
  pageSize: number
  keyword?: string
}

// ==================== 角色与权限 ====================

/** 角色编码常量 */
export type RoleCode = 'SECTION_ADMIN' | 'WORKSHOP_ADMIN' | 'AREA_REPORTER'

/** 角色信息 */
export interface RoleInfo {
  code: RoleCode
  name: string
}

// ==================== 组织架构 ====================

/** 组织类型 */
export type OrgType = 'SECTION' | 'WORKSHOP' | 'WORK_AREA' | 'WORKSHOP_LEVEL'

/** 组织架构节点 */
export interface OrgUnit {
  id: number
  parentId: number
  orgName: string
  orgType: OrgType
  areaCode?: string
  sortOrder: number
  enabled: boolean
  remark?: string
  createTime?: string
  updateTime?: string
  /** 子组织列表（树形结构） */
  children?: OrgUnit[]
}

// ==================== 用户账号 ====================

/** 用户信息 */
export interface SysUser {
  id: number
  username: string
  realName: string
  phone?: string
  enabled: boolean
  firstLogin: boolean
  lastLoginTime?: string
  createTime?: string
  /** 角色列表 */
  roleCode?: RoleCode
  /** 绑定的组织ID */
  orgId?: number
  /** 绑定的组织名称 */
  orgName?: string
}

/** 用户组织范围绑定 */
export interface UserOrgScope {
  id: number
  userId: number
  orgId: number
  roleCode: RoleCode
  scopeType: string
}

// ==================== 人员信息 ====================

/** 人员信息 */
export interface Employee {
  id: number
  name: string
  gender?: string
  unitName?: string
  departmentName?: string
  workshopId: number
  teamName?: string
  areaId: number
  birthDate?: string
  positionName?: string
  professionalPostType?: string
  workType?: string
  rankCategory?: string
  employeeStatus: string
  enabled: boolean
  remark?: string
  /** 关联显示字段 */
  workshopName?: string
  areaName?: string
}

/** 人员调动记录 */
export interface EmployeeTransferRecord {
  id: number
  employeeId: number
  beforeWorkshopId?: number
  beforeAreaId?: number
  beforeTeamName?: string
  afterWorkshopId?: number
  afterAreaId?: number
  afterTeamName?: string
  transferReason?: string
  operatorId: number
  operateTime: string
  remark?: string
}

// ==================== 用工项目字典 ====================

/** 输入类型 */
export type InputType = 'CATEGORY' | 'NUMBER' | 'TEXT'

/** 报表类型 */
export type ReportType = 'HOURS' | 'POINTS' | 'BOTH'

/** 用工项目字典节点 */
export interface WorkItem {
  id: number
  parentId: number
  itemName: string
  itemPath?: string
  itemLevel: number
  inputType: InputType
  reportType?: ReportType
  unit?: string
  isCategory: number | boolean
  isInputItem: number | boolean
  needRemark: boolean
  excelColumn?: string
  sortOrder: number
  enabled: boolean
  /** 子项目列表（树形结构） */
  children?: WorkItem[]
}

// ==================== 月度填报期间 ====================

/** 月份状态 */
export type PeriodStatus = '未开始' | '填报中' | '车间审核中' | '段级汇总中' | '已锁定' | '已归档'

/** 月度填报期间 */
export interface MonthlyPeriod {
  id: number
  periodName: string
  year: number
  month: number
  status: PeriodStatus
  startDate?: string
  endDate?: string
  auditDeadline?: string
  locked: boolean
}

// ==================== 填报数据 ====================

/** 填报数据状态 */
export type ReportStatus = '草稿' | '已提交' | '已退回' | '已审核' | '已锁定'

/** 填报主记录 */
export interface WorkReport {
  id: number
  periodId: number
  workshopId: number
  areaId: number
  employeeId: number
  workDate: string
  reportType: ReportType
  status: ReportStatus
  remark?: string
  createdBy: number
  createdTime?: string
  /** 关联显示字段 */
  employeeName?: string
  areaName?: string
  workshopName?: string
  periodName?: string
  /** 填报明细列表 */
  items?: WorkReportItem[]
}

/** 填报明细 */
export interface WorkReportItem {
  id?: number
  reportId?: number
  workItemId: number
  numberValue?: number
  textValue?: string
  unit?: string
  sortOrder: number
  remark?: string
  /** 关联显示字段 */
  itemPath?: string
  itemName?: string
}

// ==================== 审核记录 ====================

/** 审核操作类型 */
export type AuditAction = 'SUBMIT' | 'APPROVE' | 'RETURN' | 'LOCK' | 'UNLOCK'

/** 审核记录 */
export interface AuditRecord {
  id: number
  periodId: number
  reportId?: number
  orgId: number
  auditLevel: string
  action: AuditAction
  comment?: string
  operatorId: number
  operateTime: string
  /** 关联显示字段 */
  operatorName?: string
  orgName?: string
}

// ==================== 操作日志 ====================

/** 操作日志 */
export interface OperationLog {
  id: number
  userId: number
  username?: string
  moduleName: string
  operationType: string
  targetId?: string
  summary?: string
  ip?: string
  operateTime: string
}

// ==================== 统计分析 ====================

/** 统计数据卡片 */
export interface StatCard {
  title: string
  value: number | string
  unit?: string
  trend?: number
  icon?: string
}

/** 图表数据 */
export interface ChartData {
  name: string
  value: number
  [key: string]: any
}

/** 仪表盘数据 */
export interface DashboardData {
  cards: StatCard[]
  workshopChart?: ChartData[]
  areaChart?: ChartData[]
  trendChart?: ChartData[]
}

// ==================== 登录/认证 ====================

/** 登录请求 */
export interface LoginRequest {
  username: string
  password: string
}

/** 登录响应 */
export interface LoginResult {
  token: string
  userInfo: UserInfo
}

/** 用户信息（从后端获取） */
export interface UserInfo {
  id: number
  username: string
  realName: string
  roleCode: RoleCode
  scopeType: string
  orgId: number | null
  orgName: string
  permissions: string[]
}

/** 修改密码请求 */
export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}
