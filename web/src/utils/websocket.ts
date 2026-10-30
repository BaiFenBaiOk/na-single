/**
 * na WebSocket 客户端。
 *
 * 能力：
 * - 握手鉴权：token 通过 ?token= 传递（浏览器原生 WebSocket 无法设置 Authorization 头）
 * - 心跳保活：定时发送 ping，超过 pong 等待时间主动重连
 * - 断线重连：指数退避 + 随机抖动，连续握手失败达到上限后停止并回调
 * - 消息订阅：按 type 注册处理器，返回取消订阅函数
 *
 * 协议：文本帧为 JSON `{ type, content }`；content 为 JSON 字符串。
 */

export interface JsonWsMessage {
  /** 消息类型 */
  type: string
  /** 消息内容（JSON 字符串） */
  content: string | null
}

export type WsHandler = (content: string, message: JsonWsMessage) => void

export type WsModule = 'upms'

export interface NaWebSocketOptions {
  /** 连接的服务模块，默认 upms */
  module?: WsModule
  /** 服务内连接路径，默认 /ws */
  path?: string
  /** 心跳间隔（毫秒），默认 30000 */
  heartbeatInterval?: number
  /** pong 等待超时（毫秒），默认 10000 */
  pongTimeout?: number
  /** 最大重连延迟（毫秒），默认 30000 */
  maxReconnectDelay?: number
  /** 连续握手失败上限，达到后停止重连，默认 5 */
  maxAuthFailures?: number
  /** 鉴权失败回调（默认清理本地 token 并跳转登录） */
  onAuthFailed?: () => void
}

type ResolvedOptions = Required<Omit<NaWebSocketOptions, 'onAuthFailed'>> & {
  onAuthFailed: () => void
}

/** 与 store/modules/user.ts 的持久化 key 保持一致 */
const TOKEN_STORAGE_KEY = 'na-user'

const READY_OPEN = 1
const READY_CONNECTING = 0

/** 认证过期关闭码（与服务端 JsonWebSocketMessageHandler 保持一致） */
const CLOSE_CODE_AUTH_EXPIRED = 4001

/** 读取持久化 token（直接读 localStorage，避免与 pinia store 循环依赖） */
function readToken(): string {
  try {
    const raw = localStorage.getItem(TOKEN_STORAGE_KEY)
    if (!raw) return ''
    const parsed = JSON.parse(raw)
    return parsed?.token ?? ''
  } catch {
    return ''
  }
}

/** 解析 WebSocket 基础地址：优先 VITE_WS_BASE_URL，留空走同源 */
function resolveBaseUrl(): string {
  const configured = import.meta.env.VITE_WS_BASE_URL
  if (configured) {
    if (configured.startsWith('http://')) {
      return `ws://${configured.slice('http://'.length)}`.replace(/\/$/, '')
    }
    if (configured.startsWith('https://')) {
      return `wss://${configured.slice('https://'.length)}`.replace(/\/$/, '')
    }
    if (configured.startsWith('/')) {
      const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
      return `${protocol}//${location.host}${configured}`.replace(/\/$/, '')
    }
    return configured.replace(/\/$/, '')
  }
  const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${protocol}//${location.host}`
}

/** 默认鉴权失败处理：清空本地 token 并跳转登录 */
function defaultAuthFailedHandler(): void {
  try {
    const raw = localStorage.getItem(TOKEN_STORAGE_KEY)
    if (raw) {
      const parsed = JSON.parse(raw)
      parsed.token = ''
      localStorage.setItem(TOKEN_STORAGE_KEY, JSON.stringify(parsed))
    }
  } catch {
    // 忽略持久化结构异常
  }
  if (location.pathname !== '/login') {
    const redirect = encodeURIComponent(location.pathname + location.search)
    location.href = `/login?redirect=${redirect}`
  }
}

/**
 * na WebSocket 客户端。
 *
 * 使用方式：
 * ```ts
 * wsClient.connect()
 * const off = wsClient.on('notice', (content) => console.log(JSON.parse(content)))
 * wsClient.send('demo', { text: 'hello' })
 * off()
 * ```
 */
export class NaWebSocket {
  private readonly options: ResolvedOptions

  private socket: WebSocket | null = null

  private readonly handlers = new Map<string, Set<WsHandler>>()

  private heartbeatTimer: number | null = null

  private reconnectTimer: number | null = null

  private reconnectAttempt = 0

  private handshakeFailures = 0

  private manualClose = false

  private hasOpened = false

  private lastPongAt = 0

  constructor(options: NaWebSocketOptions = {}) {
    this.options = {
      module: options.module ?? 'upms',
      path: options.path ?? '/ws',
      heartbeatInterval: options.heartbeatInterval ?? 30000,
      pongTimeout: options.pongTimeout ?? 10000,
      maxReconnectDelay: options.maxReconnectDelay ?? 30000,
      maxAuthFailures: options.maxAuthFailures ?? 5,
      onAuthFailed: options.onAuthFailed ?? defaultAuthFailedHandler
    }
  }

  /** 当前连接状态（WebSocket.readyState） */
  get readyState(): number {
    return this.socket?.readyState ?? WebSocket.CLOSED
  }

  /** 建立连接（幂等：已连接 / 连接中会直接返回） */
  connect(): void {
    if (this.socket
      && (this.socket.readyState === READY_OPEN || this.socket.readyState === READY_CONNECTING)) {
      return
    }
    const token = readToken()
    if (!token) {
      console.warn('[WebSocket] 未读取到登录 token，跳过连接')
      return
    }
    this.manualClose = false
    this.hasOpened = false

    const url = `${resolveBaseUrl()}/na-${this.options.module}${this.options.path}`
      + `?token=${encodeURIComponent(token)}`
    // 日志中不打印完整 token
    console.info(`[WebSocket] 正在连接 ${url.replace(/token=[^&]+/, 'token=***')}`)
    try {
      const socket = new WebSocket(url)
      this.socket = socket
      socket.onopen = () => this.handleOpen()
      socket.onmessage = (event) => this.handleMessage(event)
      socket.onerror = () => {
        // 交给 onclose 统一处理重连
        console.warn('[WebSocket] 连接发生错误，等待 onclose 处理')
      }
      socket.onclose = (event) => this.handleClose(event)
    } catch (e) {
      console.error('[WebSocket] 创建连接失败', e)
      this.scheduleReconnect()
    }
  }

  /** 主动断开（不会触发重连） */
  disconnect(code = 1000, reason = 'manual'): void {
    this.manualClose = true
    this.clearTimers()
    const socket = this.socket
    this.socket = null
    if (socket) {
      try {
        socket.close(code, reason)
      } catch {
        // 忽略关闭异常
      }
    }
  }

  /**
   * 发送消息
   *
   * @param type    消息类型
   * @param content 字符串按 JSON 字符串原样发送；对象序列化为 JSON；未传则为 null
   */
  send(type: string, content?: unknown): void {
    if (!this.socket || this.socket.readyState !== READY_OPEN) {
      console.warn(`[WebSocket] 连接未就绪，消息未发送: ${type}`)
      return
    }
    const payload = JSON.stringify({
      type,
      content: typeof content === 'string'
        ? content
        : content === undefined ? null : JSON.stringify(content)
    })
    this.socket.send(payload)
  }

  /** 订阅指定类型的消息，返回取消订阅函数 */
  on(type: string, handler: WsHandler): () => void {
    let handlers = this.handlers.get(type)
    if (!handlers) {
      handlers = new Set()
      this.handlers.set(type, handlers)
    }
    handlers.add(handler)
    return () => this.off(type, handler)
  }

  /** 取消订阅 */
  off(type: string, handler: WsHandler): void {
    const handlers = this.handlers.get(type)
    if (!handlers) {
      return
    }
    handlers.delete(handler)
    if (handlers.size === 0) {
      this.handlers.delete(type)
    }
  }

  private handleOpen(): void {
    this.hasOpened = true
    this.reconnectAttempt = 0
    this.handshakeFailures = 0
    this.lastPongAt = Date.now()
    console.info('[WebSocket] 已连接')
    this.startHeartbeat()
  }

  private handleMessage(event: MessageEvent): void {
    const data = typeof event.data === 'string' ? event.data : ''
    if (!data) {
      return
    }
    if (data === 'pong') {
      this.lastPongAt = Date.now()
      return
    }

    let message: JsonWsMessage
    try {
      message = JSON.parse(data)
    } catch {
      return
    }
    if (!message || typeof message.type !== 'string') {
      return
    }
    if (message.type === 'pong') {
      this.lastPongAt = Date.now()
      return
    }
    if (message.type === 'error') {
      console.warn('[WebSocket] 服务端返回错误:', message.content)
      return
    }

    const handlers = this.handlers.get(message.type)
    if (!handlers) {
      return
    }
    const content = message.content ?? ''
    handlers.forEach((handler) => {
      try {
        handler(content, message)
      } catch (e) {
        console.error('[WebSocket] 消息处理异常', e)
      }
    })
  }

  private handleClose(event: CloseEvent): void {
    this.stopHeartbeat()
    this.socket = null
    console.warn(`[WebSocket] 连接关闭 code=${event.code} reason=${event.reason || '-'} opened=${this.hasOpened}`)

    if (this.manualClose) {
      this.manualClose = false
      console.info('[WebSocket] 主动断开，不重连')
      return
    }
    // 服务端判定认证过期
    if (event.code === CLOSE_CODE_AUTH_EXPIRED) {
      console.error('[WebSocket] 鉴权失败(4001)，停止重连')
      this.notifyAuthFailed()
      return
    }
    // 握手连续失败（例如 token 失效，HTTP 401），达到上限后停止重连
    if (!this.hasOpened) {
      this.handshakeFailures += 1
      console.warn(`[WebSocket] 握手失败 ${this.handshakeFailures}/${this.options.maxAuthFailures}`)
      if (this.handshakeFailures >= this.options.maxAuthFailures) {
        this.notifyAuthFailed()
        return
      }
    }
    this.scheduleReconnect()
  }

  private scheduleReconnect(): void {
    if (this.reconnectTimer !== null) {
      return
    }
    const delay = Math.min(
      this.options.maxReconnectDelay,
      1000 * 2 ** this.reconnectAttempt
    ) + Math.floor(Math.random() * 500)
    this.reconnectAttempt += 1
    console.info(`[WebSocket] ${delay}ms 后重连（第 ${this.reconnectAttempt} 次）`)
    this.reconnectTimer = window.setTimeout(() => {
      this.reconnectTimer = null
      this.connect()
    }, delay)
  }

  private startHeartbeat(): void {
    this.stopHeartbeat()
    this.heartbeatTimer = window.setInterval(() => {
      if (!this.socket || this.socket.readyState !== READY_OPEN) {
        return
      }
      // pong 超时：主动断开，由 onclose 触发重连
      if (Date.now() - this.lastPongAt
        > this.options.heartbeatInterval + this.options.pongTimeout) {
        console.warn('[WebSocket] 心跳超时，主动断开并重连')
        try {
          this.socket.close(4000, 'HEARTBEAT_TIMEOUT')
        } catch {
          // 忽略关闭异常
        }
        return
      }
      try {
        this.socket.send('ping')
      } catch {
        // 忽略发送异常
      }
    }, this.options.heartbeatInterval)
  }

  private stopHeartbeat(): void {
    if (this.heartbeatTimer !== null) {
      window.clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  private clearTimers(): void {
    this.stopHeartbeat()
    if (this.reconnectTimer !== null) {
      window.clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
  }

  private notifyAuthFailed(): void {
    this.clearTimers()
    this.socket = null
    console.error('[WebSocket] 鉴权失败，停止重连')
    this.options.onAuthFailed()
  }
}

/** 默认单例：连接 na-upms 的 /ws，登录后调用 connect() */
export const wsClient = new NaWebSocket({ module: 'upms' })
