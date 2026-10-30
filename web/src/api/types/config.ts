// 系统配置 / 表单相关类型
import type { IdStr, PageQuery } from './common'

/** 配置项（对应 ResSysConfigPagingVO） */
export interface ConfigItem {
  id: IdStr
  formId: IdStr
  code: string
  name: string
  /** longtext，可能含平台协议 / OSS 密钥等敏感内容，展示注意脱敏 */
  value: string
  remarks?: string
  isDel?: boolean
}

/** 配置表单分组（对应 ResSysFormPagingVO，内嵌该表单下的配置项列表） */
export interface ConfigGroup {
  id: IdStr
  name: string
  code: string
  list: ConfigItem[]
}

export interface ConfigPageQuery extends PageQuery {
  fromCode?: string
}

/** 新建配置项（对应 ReqSysConfigSaveVO） */
export interface ConfigSavePayload {
  formId: IdStr
  name: string
  code: string
  value: string
  remarks?: string
}

/** 编辑配置项（对应 ReqSysConfigUpdateVO，三字段必填） */
export interface ConfigUpdatePayload {
  id: IdStr
  value: string
  remarks: string
}
