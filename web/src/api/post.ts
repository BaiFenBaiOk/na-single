import { del, post, put } from './http/request'
import { normalizePage } from './http/pageAdapter'
import type { RawPage, IdStr, PageResult } from './types/common'
import type { PostPageQuery, PostRecord, PostSavePayload } from './types/post'

/** 岗位分页（响应经分页错位适配） */
export async function postPage(body: PostPageQuery): Promise<PageResult<PostRecord>> {
  const raw = await post<RawPage<PostRecord>>('/api/post/list-paging', body)
  return normalizePage(raw)
}

/** 新建岗位 */
export function savePost(body: PostSavePayload) {
  return post<null>('/api/post/save', body)
}

/** 编辑岗位 */
export function updatePost(body: PostSavePayload & { id: IdStr }) {
  return put<null>('/api/post/update', body)
}

/** 删除岗位（有用户绑定会被后端拒绝并返回提示） */
export function removePost(id: IdStr) {
  return del<null>(`/api/post/delete/${encodeURIComponent(id)}`)
}
