<template>
  <div class="tags-view" :class="theme.tagsStyle">
    <el-scrollbar class="tags-scroll">
      <div class="tags-list">
        <div
          v-for="(tag, index) in views"
          :key="tag.fullPath"
          class="tags-item"
          :class="{ 'is-active': isActive(tag) }"
          :draggable="theme.sortableTagsView"
          @click="onClick(tag)"
          @contextmenu.prevent="openMenu(tag, $event)"
          @dragstart="onDragStart(index, $event)"
          @dragover.prevent
          @drop="onDrop(index)"
        >
          <el-icon v-if="theme.showTagsViewIcon && tag.icon" class="tags-item__icon">
            <component :is="tag.icon" />
          </el-icon>
          <span class="tags-item__title">{{ tag.title }}</span>
          <el-icon
            v-if="!tag.affix"
            class="tags-item__close"
            @click.stop="closeTag(tag)"
          >
            <Close />
          </el-icon>
        </div>
      </div>
    </el-scrollbar>

    <ul
      v-show="menu.visible"
      class="tags-contextmenu"
      :style="{ left: `${menu.x}px`, top: `${menu.y}px` }"
    >
      <li @click="refreshTag()">刷新</li>
      <li v-if="menu.tag && !menu.tag.affix" @click="closeTag(menu.tag)">关闭</li>
      <li @click="closeOthers()">关闭其他</li>
      <li @click="closeAll">关闭全部</li>
    </ul>
  </div>
</template>

<script setup lang="ts">
/**
 * 多标签页：记录已访问路由，支持关闭、右键菜单、拖拽排序与 keep-alive 缓存。
 */
import { computed, onBeforeUnmount, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Close } from '@element-plus/icons-vue'
import { useAppStore } from '@/store/modules/app'
import { useTagsViewStore, type TagView } from '@/store/modules/tagsView'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const tagsStore = useTagsViewStore()
const theme = computed(() => appStore.theme)

const views = computed(() => tagsStore.visitedViews)
const menu = reactive<{ visible: boolean; x: number; y: number; tag: TagView | null }>({
  visible: false,
  x: 0,
  y: 0,
  tag: null
})

let dragIndex = -1

function isActive(tag: TagView) {
  return theme.value.shareTagsView ? tag.path === route.path : tag.fullPath === route.fullPath
}

// 开启“保留历史”时，先恢复上次打开的标签；过滤已不存在的路由
if (theme.value.keepTagsViewHistory) {
  tagsStore.restoreHistory()
  tagsStore.visitedViews = tagsStore.visitedViews.filter((view) =>
    router.hasRoute(view.name)
  )
}

watch(
  () => route.fullPath,
  () => {
    tagsStore.addView(route, theme.value.shareTagsView, theme.value.cacheTagsView)
  },
  { immediate: true }
)

// 标签变化或开关切换时同步到本地
watch(
  [() => tagsStore.visitedViews, () => theme.value.keepTagsViewHistory],
  () => tagsStore.persistHistory(theme.value.keepTagsViewHistory),
  { deep: true, immediate: true }
)

function onClick(tag: TagView) {
  if (isActive(tag)) return
  router.push(tag.fullPath)
}

function closeTag(tag: TagView) {
  if (!tag || tag.affix) return
  const closingActive = isActive(tag)
  tagsStore.delView(tag, theme.value.shareTagsView)
  if (closingActive) {
    const last = tagsStore.visitedViews[tagsStore.visitedViews.length - 1]
    router.push(last ? last.fullPath : '/dashboard')
  }
  menu.visible = false
}

function closeOthers(tag?: TagView | null) {
  const target = tag || menu.tag
  if (!target) return
  tagsStore.delOthers(target, theme.value.shareTagsView)
  router.push(target.fullPath)
  menu.visible = false
}

function closeAll() {
  tagsStore.delAll()
  const first = tagsStore.visitedViews[0]
  router.push(first ? first.fullPath : '/dashboard')
  menu.visible = false
}

function refreshTag() {
  tagsStore.refreshView()
  menu.visible = false
}

function openMenu(tag: TagView, event: MouseEvent) {
  menu.tag = tag
  menu.x = event.clientX
  menu.y = event.clientY
  menu.visible = true
}

function onDragStart(index: number, event: DragEvent) {
  if (!theme.value.sortableTagsView) return
  dragIndex = index
  // Firefox 需要写入 dataTransfer 才会触发 drop
  event.dataTransfer?.setData('text/plain', String(index))
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
  }
}

function onDrop(index: number) {
  if (theme.value.sortableTagsView && dragIndex >= 0) {
    tagsStore.sortViews(dragIndex, index)
  }
  dragIndex = -1
}

function hideMenu() {
  menu.visible = false
}

onMounted(() => document.addEventListener('click', hideMenu))
onBeforeUnmount(() => document.removeEventListener('click', hideMenu))
</script>

<style lang="scss" scoped>
.tags-view {
  flex-shrink: 0;
  height: 40px;
  padding: 4px 8px;
  background-color: var(--app-bg-card);
  border-bottom: 1px solid var(--app-border-color);
  overflow: hidden;
}

.tags-scroll {
  height: 100%;
}

.tags-list {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 100%;
  white-space: nowrap;
}

.tags-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 28px;
  padding: 0 8px;
  border: 1px solid var(--app-border-color);
  border-radius: 4px;
  font-size: 12px;
  color: var(--app-text-regular);
  background: var(--app-bg-card);
  cursor: pointer;
  user-select: none;
  transition: color 0.15s, border-color 0.15s, background-color 0.15s;

  &:hover {
    color: var(--app-color-primary);
    border-color: var(--app-color-primary);
  }

  &.is-active {
    color: #fff;
    background-color: var(--app-color-primary);
    border-color: var(--app-color-primary);
  }
}

.tags-item__icon {
  font-size: 13px;
}

.tags-item__title {
  max-width: 160px;
  @include ellipsis;
}

.tags-item__close {
  font-size: 12px;
  border-radius: 50%;

  &:hover {
    background-color: rgba(0, 0, 0, 0.15);
  }
}

// 风格一：下划线
.tags-style-one {
  .tags-item {
    border: none;
    border-bottom: 2px solid transparent;
    border-radius: 0;
    background: transparent;
  }

  .tags-item.is-active {
    color: var(--app-color-primary);
    background: transparent;
    border-bottom-color: var(--app-color-primary);
  }
}

// 风格四：方形
.tags-style-four .tags-item {
  border-radius: 0;
}

.tags-contextmenu {
  position: fixed;
  z-index: 3000;
  min-width: 110px;
  margin: 0;
  padding: 4px 0;
  list-style: none;
  border: 1px solid var(--app-border-color);
  border-radius: 4px;
  background: var(--app-bg-card);
  box-shadow: var(--app-shadow);

  li {
    padding: 6px 14px;
    font-size: 12px;
    color: var(--app-text-regular);
    cursor: pointer;

    &:hover {
      color: var(--app-color-primary);
      background: var(--app-bg-page);
    }
  }
}
</style>
