import type { PageResult, RawPage } from '@/api/types/common'

/**
 * 分页错位适配 —— 全项目唯一允许触碰后端原始分页字段的地方。
 *
 * 已核实（na-tool na-common 3.0.0 字节码）：BasePagesDomain.pages(IPage, list)
 * 两参工厂存在字段错位——
 *   响应.pageSize 实际承载「总条数」
 *   响应.total    实际承载「每页条数」
 * 列表数据在 totalList；且 Long / primitive long 经 Jackson 可能序列化为字符串。
 *
 * 业务页面只允许消费本函数返回的 PageResult（语义正确），禁止直读 RawPage。
 */
export function normalizePage<T>(raw: RawPage<T> | null | undefined): PageResult<T> {
  if (!raw) {
    return { records: [], current: 1, pageSize: 10, total: 0, totalPage: 0 }
  }
  return {
    records: Array.isArray(raw.totalList) ? raw.totalList : [],
    current: toNumber(raw.currentPage, 1),
    // 注意取值对调：总数取 pageSize，页大小取 total
    total: toNumber(raw.pageSize, 0),
    pageSize: toNumber(raw.total, 10),
    totalPage: toNumber(raw.totalPage, 0)
  }
}

function toNumber(v: unknown, fallback: number): number {
  const n = Number(v)
  return Number.isFinite(n) ? n : fallback
}
