import axios, {
  type AxiosInstance,
  type AxiosRequestConfig,
  type AxiosResponse,
  type InternalAxiosRequestConfig
} from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/api/types/common'
import { wsClient } from '@/utils/websocket'
import { BIZ_CODE } from '@/constants/businessCode'

/** 扩展请求配置，支持业务静默调用（出错不弹全局提示） */
declare module 'axios' {
  interface AxiosRequestConfig {
    /** 为 true 时业务错误不自动弹 ElMessage，由调用方自行处理 */
    silent?: boolean
  }
}

/**
 * 接口模块名。
 *
 * 单体项目只保留 na-upms，所有接口统一请求 /api/na-upms/**。
 */
export type ApiModule = 'na-upms'

/**
 * 给 /api/** 路径补上模块段。
 *
 * 示例：/api/login + na-upms → /api/na-upms/login
 *
 * 已是目标前缀（避免重复拼接）或不是 /api/ 开头的地址原样返回。
 */
function withModulePrefix(url: string | undefined, module: ApiModule): string | undefined {
  if (!url || !url.startsWith('/api/')) return url

  const prefix = `/api/${module}`
  if (url === prefix || url.startsWith(`${prefix}/`)) {
    return url
  }
  // '/api'.length === 4，保留 /api 后面的完整路径（含查询参数）
  return `${prefix}${url.slice('/api'.length)}`
}

/**
 * 从本地读取 token（直接读 localStorage，避免与 user store 产生循环依赖）。
 * 键名与 store/modules/user.ts 的持久化 key 保持一致。
 */
const TOKEN_STORAGE_KEY = 'na-user'

/** 读取本地登录 token（上传组件等非 axios 请求也需要） */
export function getToken(): string {
  try {
    const raw = localStorage.getItem(TOKEN_STORAGE_KEY)
    if (raw) {
      const parsed = JSON.parse(raw)
      return parsed?.token ?? ''
    }
  } catch {
    // 持久化结构异常时忽略
  }
  return ''
}

// 防止并发请求在会话失效时重复弹提示 / 重复跳转
let isRedirecting = false

/** 会话失效：清理本地态并整页跳转登录（整页刷新可接受，且规避模块循环依赖） */
function handleUnauthorized() {
  if (isRedirecting) return
  isRedirecting = true
  // 先断开 WebSocket，避免会话失效后仍保持长连接
  wsClient.disconnect(4001, 'unauthorized')
  ElMessage.error('登录状态已失效，请重新登录')
  const redirect = encodeURIComponent(window.location.pathname + window.location.search)
  // 清掉持久化的 token，登录页据此重新走登录流程
  try {
    const raw = localStorage.getItem(TOKEN_STORAGE_KEY)
    if (raw) {
      const parsed = JSON.parse(raw)
      parsed.token = ''
      localStorage.setItem(TOKEN_STORAGE_KEY, JSON.stringify(parsed))
    }
  } catch {
    // 忽略
  }
  window.setTimeout(() => {
    window.location.href = `/login?redirect=${redirect}`
  }, 400)
}

/** 创建带统一 token、模块前缀、响应拆壳能力的 axios 实例 */
function createHttp(module: ApiModule): AxiosInstance {
  const instance = axios.create({
    // 生产环境留空走同源；如需浏览器直连可填服务地址
    baseURL: import.meta.env.VITE_API_BASE_URL || '',
    timeout: 15000
  })

  // ---------------- 请求拦截器 ----------------
  instance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
      // 把 /api/** 补成 /api/na-upms/**
      config.url = withModulePrefix(config.url, module)

      const token = getToken()
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      return config
    },
    (error) => Promise.reject(error)
  )

  // ---------------- 响应拦截器 ----------------
  instance.interceptors.response.use(handleResponse, handleResponseError)

  return instance
}

/** 响应成功处理：二进制直返、统一拆壳、按业务码提示 */
function handleResponse(response: AxiosResponse<ApiResponse>) {
  // 二进制（图片 / 文件下载）直接返回原始数据
  if (response.config.responseType === 'blob' || response.data instanceof Blob) {
    return response.data as unknown as never
  }

  const res = response.data
  // 非标准响应壳（理论上后端统一包装），直接透传
  if (res === null || typeof res === 'undefined' || typeof res.code === 'undefined') {
    return res as unknown as never
  }

  const code = Number(res.code)

  // 成功：0 默认成功（R.successNotMsg），兼容 1（Status.SUCCESS）
  if (code === 0 || code === 1) {
    return res.data as never
  }

  // 未登录
  if (code === 401) {
    handleUnauthorized()
    return Promise.reject(new BizError(res.msg || '未登录', code, res))
  }

  // 无权限
  if (code === 403) {
    if (!response.config.silent) {
      ElMessage.error(res.msg || '无权访问')
    }
    return Promise.reject(new BizError(res.msg || '无权访问', code, res))
  }

  // 其他业务错误（-1 失败 / -2 业务异常 / 10004 参数错误 / 140003 验证码错误等）
  if (!response.config.silent) {
    ElMessage.error(res.msg || '操作失败')
  }
  return Promise.reject(new BizError(res.msg || '操作失败', code, res))
}

/** 响应失败处理：HTTP 层错误兜底 */
function handleResponseError(error: any) {
  // HTTP 层错误（后端安全处理器多为 HTTP 200 + body，这里仅作防御）
  const status = error?.response?.status
  if (status === 401) {
    handleUnauthorized()
  } else if (status === 403) {
    ElMessage.error('无权访问')
  } else if (error?.code === 'ECONNABORTED') {
    ElMessage.error('请求超时，请稍后重试')
  } else if (!error?.config?.silent) {
    ElMessage.error(error?.response?.data?.msg || '网络异常，请检查后端服务')
  }
  return Promise.reject(error)
}

/** 平台接口实例（na-upms），请求 /api/na-upms/** */
const service = createHttp('na-upms')

/** 业务错误（携带后端 code 与原始响应，供调用方按码分流） */
export class BizError extends Error {
  code: number
  raw: ApiResponse
  constructor(message: string, code: number, raw: ApiResponse) {
    super(message)
    this.name = 'BizError'
    this.code = code
    this.raw = raw
  }
}

// 语义化的方法封装：返回值即业务 data（拦截器已拆壳）。
// 拦截器内已把响应改写成业务 data，但 axios 的静态类型仍按 AxiosResponse 推导，
// 故这里统一断言为 Promise<T>。
export function get<T = unknown>(url: string, params?: object, config?: AxiosRequestConfig): Promise<T> {
  return service.get(url, { params, ...config }) as unknown as Promise<T>
}

export function post<T = unknown>(url: string, data?: object, config?: AxiosRequestConfig): Promise<T> {
  return service.post(url, data, config) as unknown as Promise<T>
}

export function put<T = unknown>(url: string, data?: object, config?: AxiosRequestConfig): Promise<T> {
  return service.put(url, data, config) as unknown as Promise<T>
}

export function del<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return service.delete(url, config) as unknown as Promise<T>
}

export default service
