import type { App, Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/store/modules/user'

/**
 * v-hasPermi 按钮级权限指令。
 * 用法：
 *   v-hasPermi="'user:save:post'"
 *   v-hasPermi="['a:b:get', 'a:c:get']"  // 拥有任一即可（OR）
 * 无权限时在 mounted 阶段移除该元素。
 */
const hasPermi: Directive<HTMLElement, string | string[]> = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    check(el, binding)
  },
  updated(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    check(el, binding)
  }
}

function check(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
  const { value } = binding
  if (!value) return
  const userStore = useUserStore()
  const perms = userStore.perms
  const required = Array.isArray(value) ? value : [value]
  const ok = required.some((p) => perms.includes(p))
  if (!ok) {
    el.parentNode?.removeChild(el)
  }
}

export function setupHasPermiDirective(app: App) {
  app.directive('hasPermi', hasPermi)
}

export default hasPermi
