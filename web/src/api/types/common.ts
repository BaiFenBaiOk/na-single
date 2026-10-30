// 后端统一响应包装与分页类型

/** 后端统一响应壳：HTTP 基本恒为 200，业务成败看 body.code */
export interface ApiResponse<T = unknown> {
  /** 状态码：0 默认成功（R.successNotMsg），1 成功（兼容），-1 失败，-2 业务异常，401 未登录，403 无权限 */
  code: number
  msg: string | null
  data: T
}

/** 分页请求基类字段（对应后端 BasePagesParam） */
export interface PageQuery {
  currentPage: number
  pageSize: number
  searchVal?: string
}

/**
 * 后端原始分页壳（BasePagesDomain）。
 * 警告：na-tool 3.0.0 的两参 pages(IPage,list) 工厂存在字段错位——
 * 响应.pageSize 实际是「总条数」，响应.total 实际是「每页条数」。
 * 业务代码禁止直接使用本类型，统一经 normalizePage() 转成 PageResult。
 * 且 Long/primitive long 经 Jackson 可能序列化为字符串，这里宽松标注。
 */
export interface RawPage<T> {
  currentPage: number | string
  total: number | string
  totalPage: number | string
  pageSize: number | string
  totalList: T[]
}

/** 归一化后的分页结果（语义正确，业务层专用） */
export interface PageResult<T> {
  records: T[]
  current: number
  pageSize: number
  total: number
  totalPage: number
}

/** 后端数字主键序列化为字符串，统一用类型别名提醒不可用 number 承接 */
export type IdStr = string
