/** 图形验证码：后端接口直接返回 image/jpeg，前端自生成 uuid 作为 key */

/** 生成验证码标识（优先原生 UUID，兜底手写 v4） */
export function newUuid(): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (ch) => {
    const r = (Math.random() * 16) | 0
    const v = ch === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

/**
 * 拼接验证码图片地址（同源走 vite 代理 / nginx，匿名无需 token）。
 *
 * 注意：该地址直接用于 <img src>，不经过 axios 请求拦截器，
 * 因此必须手动带模块前缀 /api/na-upms，与后端 CaptchaController 的映射保持一致。
 */
export function buildCaptchaUrl(uuid: string): string {
  return `/api/na-upms/systemInfo/get/${encodeURIComponent(uuid)}`
}
