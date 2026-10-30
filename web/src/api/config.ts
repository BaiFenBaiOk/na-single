import { get, post } from './http/request'
import { normalizePage } from './http/pageAdapter'
import type { RawPage, IdStr, PageResult } from './types/common'
import type {
  ConfigGroup,
  ConfigItem,
  ConfigPageQuery,
  ConfigSavePayload,
  ConfigUpdatePayload
} from './types/config'

/**
 * 系统配置分页：外层按表单(form)分页，records 即分组数组，
 * 每组内嵌该表单下配置项 list。
 */
export async function configPage(body: ConfigPageQuery): Promise<PageResult<ConfigGroup>> {
  const raw = await post<RawPage<ConfigGroup>>('/api/sys-config/list-paging', body)
  return normalizePage(raw)
}

/** 编辑配置项（逻辑字段 id/value/remarks 必填） */
export function updateConfig(body: ConfigUpdatePayload) {
  return post<null>('/api/sys-config/update', body)
}

/** 新建配置项 */
export function saveConfig(body: ConfigSavePayload) {
  return post<null>('/api/sys-config/save', body)
}

/** 删除配置项（逻辑删除） */
export function removeConfig(id: IdStr) {
  return get<null>(`/api/sys-config/delete/${id}`)
}

export type { ConfigItem }
