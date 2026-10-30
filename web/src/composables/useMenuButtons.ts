import { computed, onMounted, toValue, watch, type MaybeRefOrGetter } from 'vue'
import { useRoute } from 'vue-router'
import { useMenuButtonStore } from '@/store/modules/menuButton'

/**
 * 列表页按钮：按菜单 id 加载 /api/na-upms/sysuser/getMenuBtn。
 * @param menuId 显式指定菜单 id；缺省时取当前路由 meta.menuId
 */
export function useMenuButtons(menuId?: MaybeRefOrGetter<string | undefined>) {
  const route = useRoute()
  const store = useMenuButtonStore()
  const resolvedId = computed(() => String(toValue(menuId) ?? route.meta.menuId ?? ''))
  const buttons = computed(() => store.list(resolvedId.value))
  const isReady = computed(() => store.isReady(resolvedId.value))

  /** 当前菜单是否返回了指定 perms 的按钮 */
  function hasPerm(perms: string): boolean {
    return store.hasPerm(resolvedId.value, perms)
  }

  onMounted(() => store.ensure(resolvedId.value))
  watch(resolvedId, (id) => store.ensure(id))

  return { menuId: resolvedId, buttons, isReady, hasPerm }
}
