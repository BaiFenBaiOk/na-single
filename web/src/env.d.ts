/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** 接口基础路径，开发 / 生产均走同源（vite 代理或 nginx 反代），默认空串 */
  readonly VITE_API_BASE_URL: string
  /** 应用标题 */
  readonly VITE_APP_TITLE: string
  /** WebSocket 基础地址，留空走同源（开发经 Vite 代理、生产经 Nginx） */
  readonly VITE_WS_BASE_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<Record<string, unknown>, Record<string, unknown>, unknown>
  export default component
}
