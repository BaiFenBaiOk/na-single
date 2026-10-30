import { get } from './http/request'
import type { CacheInfo, SysInfo } from './types/monitor'

/** Redis 缓存监控信息（后端为匿名接口，前端仍放在登录后页面） */
export function getCacheInfo() {
  return get<CacheInfo>('/api/systemInfo/cache')
}

/** 服务器运行时信息（基于 OSHI，cpu 段会 sleep 1 秒采样） */
export function getSysInfo() {
  return get<SysInfo>('/api/systemInfo/sysInfo')
}
