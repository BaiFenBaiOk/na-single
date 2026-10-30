// 岗位相关类型
import type { IdStr, PageQuery } from './common'

/** 岗位行（对应 ResSysPostPagingVO；平铺结构，无层级） */
export interface PostRecord {
  id: IdStr
  parentId?: IdStr
  name: string
  /** 岗位编码（全局唯一） */
  code: string
  createTime?: string
  updateTime?: string
  isDel?: boolean
}

export interface PostPageQuery extends PageQuery {
  name?: string
  code?: string
  isDel?: boolean
}

/** 新建 / 编辑岗位提交体 */
export interface PostSavePayload {
  name: string
  code: string
}
