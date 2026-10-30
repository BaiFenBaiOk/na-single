import { post } from './http/request'
import { normalizePage } from './http/pageAdapter'
import type { RawPage, PageResult } from './types/common'
import type { LogPageQuery, LogRecord } from './types/log'

/**
 * 剔除空串 / 空值字段。
 * 后端 ReqLogPagingVO 的 userId 是 Long，传 "" 会直接反序列化失败；
 * 其余字符串条件在 SQL 里已判空，不传等价于不筛选。
 */
function pruneEmpty<T extends object>(body: T): T {
  const src = body as Record<string, unknown>
  const out: Record<string, unknown> = {}
  Object.keys(src).forEach((k) => {
    const v = src[k]
    if (v === '' || v === undefined || v === null) return
    out[k] = v
  })
  return out as T
}

/** 操作日志分页 */
export async function logPage(body: LogPageQuery): Promise<PageResult<LogRecord>> {
  const raw = await post<RawPage<LogRecord>>('/api/log/list-paging', pruneEmpty(body))
  return normalizePage(raw)
}
