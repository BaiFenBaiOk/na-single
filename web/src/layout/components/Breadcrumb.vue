<template>
  <el-breadcrumb class="breadcrumb" separator="/">
    <el-breadcrumb-item v-for="(item, index) in chains" :key="index">
      <span
        class="breadcrumb-item"
        :class="{ 'is-link': isClickable(item, index) }"
        @click="go(item, index)"
      >
        <el-icon v-if="showIcon && item.icon" class="breadcrumb-icon">
          <component :is="item.icon" />
        </el-icon>
        <span>{{ item.menuName }}</span>
      </span>
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePermissionStore } from '@/store/modules/permission'
import { useAppStore } from '@/store/modules/app'
import type { MenuNode } from '@/api/types/menu'
import { findFirstNavigablePath, normalizePath } from '@/utils/tree'

interface BreadcrumbItem {
  menuName: string
  icon?: string
  /** 可点击跳转的地址；最后一项 / 无子页面的目录为空 */
  path?: string
}

const route = useRoute()
const router = useRouter()
const permissionStore = usePermissionStore()
const appStore = useAppStore()
const showIcon = computed(() => appStore.theme.showBreadcrumbIcon)

/** DFS 查找目标路径在菜单树中的祖先链 */
function findChain(list: MenuNode[], target: string, trail: MenuNode[] = []): MenuNode[] | null {
  for (const node of list) {
    const next = [...trail, node]
    if (normalizePath(node.path) === target) return next
    if (node.children?.length) {
      const hit = findChain(node.children, target, next)
      if (hit) return hit
    }
  }
  return null
}

const chains = computed<BreadcrumbItem[]>(() => {
  const home: BreadcrumbItem = {
    menuName: '首页',
    icon: 'HomeFilled',
    path: permissionStore.firstPath || '/profile'
  }
  const found = findChain(permissionStore.menus, route.path)
  if (found && found.length) {
    return [
      home,
      ...found.map((n) => {
        // 菜单页 / 自带组件的目录：直接跳自身；纯目录：跳到第一个可导航子菜单
        const path =
          n.menuType === 'C' || n.component
            ? normalizePath(n.path)
            : findFirstNavigablePath(n.children || [])
        return {
          menuName: n.menuName,
          icon: n.icon && n.icon !== '#' ? n.icon : undefined,
          path
        }
      })
    ]
  }
  // profile / monitor 等固定页：回退到路由 meta
  const title = route.meta?.title
  return title
    ? [home, { menuName: String(title), icon: route.meta?.icon as string | undefined }]
    : [home]
})

/** 最后一级和没有可跳转地址的项不可点击 */
function isClickable(item: BreadcrumbItem, index: number): boolean {
  return index < chains.value.length - 1 && !!item.path
}

function go(item: BreadcrumbItem, index: number) {
  if (!isClickable(item, index) || !item.path) return
  if (item.path !== route.path) {
    router.push(item.path)
  }
}
</script>

<style lang="scss" scoped>
.breadcrumb {
  @include ellipsis;
  max-width: 60vw;
}

.breadcrumb-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.breadcrumb-item.is-link {
  cursor: pointer;

  &:hover {
    color: var(--app-color-primary);
  }
}

.breadcrumb-icon {
  font-size: 13px;
}
</style>
