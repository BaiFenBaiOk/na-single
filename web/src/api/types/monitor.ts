// 系统监控类型（后端字段多且动态，采用宽松索引签名）

/** Redis 缓存监控信息（GET /api/systemInfo/cache） */
export interface CacheInfo {
  /** Redis INFO 输出的键值对象 */
  info: Record<string, string>
  /** key 总数 */
  dbSize: number
  /** 命令统计 */
  commandStats: Array<{
    /** 命令名（已去掉 cmdstat_ 前缀） */
    name: string
    /** 调用次数 */
    value: string
  }>
}

/** 服务器监控信息（GET /api/systemInfo/sysInfo），结构随 OSHI 采集变化，宽松处理 */
export type SysInfo = Record<string, unknown>
