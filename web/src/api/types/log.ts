// 操作日志相关类型（对应后端 ResLogPagingVO / ReqLogPagingVO）
import type { IdStr, PageQuery } from './common'

/** 日志行（对应 ResLogPagingVO） */
export interface LogRecord {
  id: string
  /** 请求 IP */
  requestIp: string
  /** 执行结果：OPT 正常 / EX 异常 */
  type: string
  userId: IdStr
  /** 接口描述（取 @Operation.summary） */
  description: string
  classPath: string
  actionMethod: string
  requestUri: string
  /** IP 解析出的归属地列表 */
  ipAddress?: string[]
  httpMethod: string
  /** 请求参数（后端截断到 1000 字符） */
  params: string
  /** 响应结果（后端截断到 16383 字符） */
  result: string
  /** 异常描述 */
  exDesc: string
  /** 异常堆栈 */
  exDetail: string
  startTime: string
  finishTime: string
  /** 耗时（毫秒） */
  consumingTime: number
  /** 模块 code，如 USER_MANAGEMENT（后端 AutoLogConfig.cn 默认 false，故为英文 code） */
  module: string
  /** 操作 code，如 ADD */
  operate: string
  /** 日志内容，如「【admin】创建用户」 */
  content: string
  /** 浏览器标识 */
  ua: string
  operateTime: string
  userName: string
  nickName: string
  /** 用户来源：0 授权系统 1 外部 */
  userFrom: number
}

/** 日志分页查询参数（对应 ReqLogPagingVO） */
export interface LogPageQuery extends PageQuery {
  userId?: IdStr | ''
  /** 模块 code，后端为 = 精确匹配 */
  model?: string
  /** 操作 code，后端为 = 精确匹配 */
  operate?: string
  /** 耗时比较方式：1 大于 / 2 大于等于 / 3 等于 / 4 小于 / 5 小于等于 */
  consumingType?: number
  /** 耗时（毫秒）；与 consumingType 同时有值才生效，为空则不参与筛选 */
  consumingTime?: number
  /** 操作开始时间，格式必须为 yyyy-MM-dd HH:mm:ss（后端已注册该 pattern 的反序列化器） */
  operateStartTime?: string
  /** 操作结束时间，格式同上 */
  operateEndTime?: string
  /** 用户名，模糊匹配 */
  userName?: string
}
