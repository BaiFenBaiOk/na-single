// vue-router 路由元信息类型增强。
// 该文件必须是模块（保留下面的 import），declare module 才是「增强」而非整体替换；
// 否则在无 import 的脚本文件里会把 vue-router 类型整个覆盖，导致 createRouter 等导出全部丢失。
import 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    /** 菜单 / 页面标题 */
    title?: string
    /** Element Plus 图标名（M/C 节点） */
    icon?: string
    /** 当前页面在 sys_menu 中的 id（用于 getMenuBtn 查询按钮） */
    menuId?: string
    /** 权限标识 */
    perms?: string
    /** 是否 keep-alive 缓存（isCache=0） */
    keepAlive?: boolean
    /** 是否在侧边栏隐藏（visible=1） */
    hidden?: boolean
    /** 仅系统用户 00 可访问 */
    requireSysDef?: boolean
    /** 后端菜单下发的原始组件路径，仅用于匹配失败时在占位页提示，便于核对菜单配置 */
    rawComponent?: string
  }
}
