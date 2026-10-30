import dayjs from 'dayjs'

/** 格式化时间（后端统一 yyyy-MM-dd HH:mm:ss） */
export function fmtTime(v?: string | null, pattern = 'YYYY-MM-DD HH:mm:ss'): string {
  if (!v) return '-'
  const d = dayjs(v)
  return d.isValid() ? d.format(pattern) : String(v)
}

/** 空值显示为占位符 */
export function emptyDash(v?: string | number | null, dash = '-'): string {
  return v === undefined || v === null || v === '' ? dash : String(v)
}

/** 后端 Set / 数组字段拼接展示 */
export function joinSet(v?: string[] | null, sep = '、'): string {
  if (!v || v.length === 0) return '-'
  return v.filter(Boolean).join(sep)
}

/** 登录归属地（List<String>）拼接 */
export function joinAddress(v?: string[] | null): string {
  if (!v || v.length === 0) return '-'
  return v.filter(Boolean).join(' ')
}
