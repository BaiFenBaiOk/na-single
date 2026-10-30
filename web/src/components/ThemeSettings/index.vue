<template>
  <el-drawer
    v-model="visible"
    title="布局配置"
    :size="isMobile ? '88%' : '340px'"
    append-to-body
    class="theme-settings-drawer"
  >
    <div class="theme-panel">
      <!-- 布局切换 -->
      <section class="theme-section">
        <div class="theme-section__title">布局切换</div>
        <div class="layout-grid">
          <button
            v-for="item in LAYOUT_OPTIONS"
            :key="item.value"
            type="button"
            class="layout-card"
            :class="{ 'is-active': appStore.theme.layout === item.value }"
            @click="selectLayout(item.value)"
          >
            <span class="layout-card__preview" :class="`is-${item.value}`">
              <span class="layout-card__aside" />
              <span class="layout-card__main" />
            </span>
            <span class="layout-card__label">{{ item.label }}</span>
          </button>
        </div>
      </section>

      <!-- 全局主题 -->
      <section class="theme-section">
        <div class="theme-section__title">全局主题</div>
        <div class="theme-row">
          <span>primary</span>
          <el-color-picker v-model="primaryColor" />
        </div>
        <div class="theme-swatches">
          <button
            v-for="color in PRESET_COLORS"
            :key="color"
            type="button"
            class="theme-swatch"
            :class="{ 'is-active': appStore.theme.primaryColor === color }"
            :style="{ backgroundColor: color }"
            @click="appStore.theme.primaryColor = color"
          />
        </div>
        <div class="theme-row">
          <span>深色模式</span>
          <el-switch v-model="appStore.theme.darkMode" />
        </div>
        <div class="theme-row">
          <span>组件大小</span>
          <el-select v-model="appStore.theme.componentSize" size="small" style="width: 110px">
            <el-option label="大" value="large" />
            <el-option label="默认" value="default" />
            <el-option label="小" value="small" />
          </el-select>
        </div>
      </section>

      <!-- 顶栏设置 -->
      <section class="theme-section">
        <div class="theme-section__title">顶栏设置</div>
        <div class="theme-row">
          <span>顶栏背景</span>
          <el-color-picker
            :model-value="appStore.theme.headerBg"
            @update:model-value="(v: string | null) => setColor('headerBg', v)"
          />
        </div>
        <div class="theme-row">
          <span>顶栏默认字体颜色</span>
          <el-color-picker
            :model-value="appStore.theme.headerTextColor"
            @update:model-value="(v: string | null) => setColor('headerTextColor', v)"
          />
        </div>
        <div class="theme-row">
          <span>顶栏背景渐变</span>
          <el-switch v-model="appStore.theme.headerBgGradual" />
        </div>
      </section>

      <!-- 菜单设置 -->
      <section class="theme-section">
        <div class="theme-section__title">菜单设置</div>
        <div class="theme-row">
          <span>菜单背景</span>
          <el-color-picker
            :model-value="appStore.theme.menuBg"
            @update:model-value="(v: string | null) => setColor('menuBg', v)"
          />
        </div>
        <div class="theme-row">
          <span>菜单默认字体颜色</span>
          <el-color-picker
            :model-value="appStore.theme.menuTextColor"
            @update:model-value="(v: string | null) => setColor('menuTextColor', v)"
          />
        </div>
        <div class="theme-row">
          <span>菜单高亮背景色</span>
          <el-color-picker
            :model-value="appStore.theme.menuActiveBg"
            @update:model-value="(v: string | null) => setColor('menuActiveBg', v)"
          />
        </div>
        <div class="theme-row">
          <span>菜单选中字体</span>
          <el-color-picker
            :model-value="appStore.theme.menuActiveTextColor"
            @update:model-value="(v: string | null) => setColor('menuActiveTextColor', v)"
          />
        </div>
        <div class="theme-row">
          <span>菜单背景渐变</span>
          <el-switch v-model="appStore.theme.menuBgGradual" />
        </div>
      </section>

      <!-- 分栏设置 -->
      <section class="theme-section">
        <div class="theme-section__title">分栏设置</div>
        <div class="theme-row">
          <span>分栏菜单背景</span>
          <el-color-picker
            :model-value="appStore.theme.columnsMenuBg"
            @update:model-value="(v: string | null) => setColor('columnsMenuBg', v)"
          />
        </div>
        <div class="theme-row">
          <span>分栏菜单默认字体颜色</span>
          <el-color-picker
            :model-value="appStore.theme.columnsMenuTextColor"
            @update:model-value="(v: string | null) => setColor('columnsMenuTextColor', v)"
          />
        </div>
        <div class="theme-row">
          <span>分栏菜单背景渐变</span>
          <el-switch v-model="appStore.theme.columnsMenuBgGradual" />
        </div>
        <div class="theme-row">
          <span>分栏菜单鼠标悬停预加载</span>
          <el-switch v-model="appStore.theme.columnsMenuHoverPreload" />
        </div>
      </section>

      <!-- 界面设置 -->
      <section class="theme-section">
        <div class="theme-section__title">界面设置</div>
        <div class="theme-row">
          <span>菜单水平折叠</span>
          <el-switch v-model="collapsed" />
        </div>
        <div class="theme-row">
          <span>菜单手风琴</span>
          <el-switch v-model="appStore.theme.uniqueOpened" />
        </div>
        <div class="theme-row">
          <span>固定 Header</span>
          <el-switch v-model="appStore.theme.fixedHeader" />
        </div>
        <div class="theme-row">
          <span>经典布局顶部菜单</span>
          <el-switch v-model="appStore.theme.classicSplitMenu" />
        </div>
        <div class="theme-row">
          <span>开启锁屏</span>
          <el-switch v-model="appStore.theme.lockScreenEnabled" />
        </div>
      </section>

      <!-- 界面显示 -->
      <section class="theme-section">
        <div class="theme-section__title">界面显示</div>
        <div class="theme-row">
          <span>侧边栏 Logo</span>
          <el-switch v-model="appStore.theme.showLogo" />
        </div>
        <div class="theme-row">
          <span>开启 Breadcrumb</span>
          <el-switch v-model="appStore.theme.showBreadcrumb" />
        </div>
        <div class="theme-row">
          <span>开启 Breadcrumb 图标</span>
          <el-switch v-model="appStore.theme.showBreadcrumbIcon" />
        </div>
        <div class="theme-row">
          <span>开启 TagsView</span>
          <el-switch v-model="appStore.theme.showTagsView" />
        </div>
        <div class="theme-row">
          <span>开启 TagsView 图标</span>
          <el-switch v-model="appStore.theme.showTagsViewIcon" />
        </div>
        <div class="theme-row">
          <span>开启 TagsView 缓存</span>
          <el-switch v-model="appStore.theme.cacheTagsView" />
        </div>
        <div class="theme-row">
          <span>开启 TagsView 拖拽</span>
          <el-switch v-model="appStore.theme.sortableTagsView" />
        </div>
        <div class="theme-row">
          <span>开启 TagsView 共用</span>
          <el-switch v-model="appStore.theme.shareTagsView" />
        </div>
        <div class="theme-row">
          <span>保留 TagsView 历史</span>
          <el-switch v-model="appStore.theme.keepTagsViewHistory" />
        </div>
        <div class="theme-row">
          <span>开启 Footer</span>
          <el-switch v-model="appStore.theme.showFooter" />
        </div>
        <div class="theme-row">
          <span>灰色模式</span>
          <el-switch v-model="appStore.theme.grayMode" />
        </div>
        <div class="theme-row">
          <span>色弱模式</span>
          <el-switch v-model="appStore.theme.colorWeak" />
        </div>
        <div class="theme-row">
          <span>开启水印</span>
          <el-switch v-model="appStore.theme.showWatermark" />
        </div>
        <div v-if="appStore.theme.showWatermark" class="theme-row">
          <span>水印文案</span>
          <el-input
            v-model="appStore.theme.watermarkText"
            size="small"
            maxlength="30"
            style="width: 150px"
          />
        </div>
        <div class="theme-row">
          <span>开启 AI 助手</span>
          <el-switch v-model="appStore.theme.showAiAssistant" />
        </div>
        <div class="theme-row">
          <span>开启超级助手</span>
          <el-switch v-model="appStore.theme.showSuperAssistant" />
        </div>
      </section>

      <!-- 其它设置 -->
      <section class="theme-section">
        <div class="theme-section__title">其它设置</div>
        <div class="theme-row">
          <span>TagsView 风格</span>
          <el-select v-model="appStore.theme.tagsStyle" size="small" style="width: 130px">
            <el-option label="风格一" value="tags-style-one" />
            <el-option label="风格四" value="tags-style-four" />
            <el-option label="风格五" value="tags-style-five" />
          </el-select>
        </div>
        <div class="theme-row">
          <span>主页面切换动画</span>
          <el-select v-model="appStore.theme.pageAnimation" size="small" style="width: 130px">
            <el-option label="slide-right" value="slide-right" />
            <el-option label="slide-left" value="slide-left" />
            <el-option label="opacitys" value="opacitys" />
          </el-select>
        </div>
        <div class="theme-row">
          <span>分栏高亮风格</span>
          <el-select v-model="appStore.theme.columnsAsideStyle" size="small" style="width: 130px">
            <el-option label="圆角" value="columns-round" />
            <el-option label="卡片" value="columns-card" />
          </el-select>
        </div>
        <div class="theme-row">
          <span>分栏布局风格</span>
          <el-select v-model="appStore.theme.columnsAsideLayout" size="small" style="width: 130px">
            <el-option label="垂直" value="columns-vertical" />
            <el-option label="水平" value="columns-horizontal" />
          </el-select>
        </div>
      </section>
    </div>

    <template #footer>
      <div class="theme-footer">
        <el-button @click="appStore.resetTheme()">恢复默认</el-button>
        <el-button type="primary" @click="copyConfig">一键复制配置</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
/**
 * 布局配置抽屉。
 *
 * 配置保存在 app store（持久化），App.vue 监听变化后写入 CSS 变量；
 * 布局切换、TagsView、水印等由 Layout 读取配置渲染。
 */
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useDevice } from '@/composables/useDevice'
import { useAppStore } from '@/store/modules/app'
import type { LayoutType } from '@/utils/theme'

type ColorKey =
  | 'headerBg'
  | 'headerTextColor'
  | 'menuBg'
  | 'menuTextColor'
  | 'menuActiveTextColor'
  | 'menuActiveBg'
  | 'columnsMenuBg'
  | 'columnsMenuTextColor'

const LAYOUT_OPTIONS: Array<{ value: LayoutType; label: string }> = [
  { value: 'sidebar', label: '左侧' },
  { value: 'classic', label: '左顶' },
  { value: 'transverse', label: '横向' },
  { value: 'columns', label: '分栏' }
]

const PRESET_COLORS = [
  '#1d6fff',
  '#409eff',
  '#1677ff',
  '#722ed1',
  '#13c2c2',
  '#52c41a',
  '#fa8c16',
  '#f5222d',
  '#eb2f96'
]

const { isMobile } = useDevice()
const appStore = useAppStore()

const visible = computed({
  get: () => appStore.themeDrawerVisible,
  set: (value: boolean) => {
    appStore.themeDrawerVisible = value
  }
})

const primaryColor = computed({
  get: () => appStore.theme.primaryColor,
  set: (value: string | null) => {
    if (value) appStore.theme.primaryColor = value
  }
})

/** 菜单折叠沿用已有 sidebarCollapsed 状态 */
const collapsed = computed({
  get: () => appStore.sidebarCollapsed,
  set: (value: boolean) => appStore.setSidebar(value)
})

function setColor(key: ColorKey, value: string | null) {
  appStore.theme[key] = value || ''
}

/** 切换到左顶布局时自动开启“经典布局顶部菜单”，确保与左侧布局有明显差异 */
function selectLayout(layout: LayoutType) {
  appStore.theme.layout = layout
  if (layout === 'classic') {
    appStore.theme.classicSplitMenu = true
  }
}

async function copyConfig() {
  const text = JSON.stringify(
    { ...appStore.theme, collapsed: appStore.sidebarCollapsed },
    null,
    2
  )
  let ok = false
  try {
    await navigator.clipboard.writeText(text)
    ok = true
  } catch {
    // 非安全上下文（内网 http）下降级
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    try {
      ok = document.execCommand('copy')
    } finally {
      document.body.removeChild(textarea)
    }
  }
  if (ok) {
    ElMessage.success('布局配置已复制')
  } else {
    ElMessage.error('复制失败，请手动复制')
  }
}
</script>

<style lang="scss" scoped>
.theme-panel {
  padding-bottom: 8px;
}

.theme-section {
  padding-bottom: 16px;
  margin-bottom: 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);

  &:last-child {
    border-bottom: none;
    margin-bottom: 0;
  }
}

.theme-section__title {
  margin-bottom: 12px;
  font-size: 13px;
  font-weight: 600;
  color: var(--app-text-primary);
  text-align: center;
  position: relative;

  &::before,
  &::after {
    content: '';
    position: absolute;
    top: 50%;
    width: 30%;
    height: 1px;
    background: var(--el-border-color-lighter);
  }

  &::before {
    left: 0;
  }

  &::after {
    right: 0;
  }
}

.theme-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
  font-size: 13px;
  color: var(--app-text-regular);

  &:last-child {
    margin-bottom: 0;
  }
}

.theme-swatches {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 4px 0 14px;
}

.theme-swatch {
  width: 24px;
  height: 24px;
  padding: 0;
  border: 2px solid transparent;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.15s;

  &:hover {
    transform: scale(1.08);
  }

  &.is-active {
    border-color: var(--app-text-primary);
    box-shadow: 0 0 0 2px var(--app-bg-card);
  }
}

// 布局切换卡片
.layout-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.layout-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 8px;
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  background: transparent;
  cursor: pointer;
  transition: border-color 0.15s, background-color 0.15s;

  &:hover {
    border-color: var(--app-color-primary);
  }

  &.is-active {
    border-color: var(--app-color-primary);
    background: var(--el-color-primary-light-9);
  }
}

.layout-card__preview {
  position: relative;
  display: block;
  width: 100%;
  height: 52px;
  border-radius: 4px;
  background: var(--el-fill-color-light);
  overflow: hidden;
}

.layout-card__aside {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 26%;
  background: var(--app-bg-sidebar, #001529);
}

.layout-card__main {
  position: absolute;
  left: 26%;
  right: 0;
  top: 0;
  bottom: 0;
  background: var(--app-bg-page);
}

.layout-card__preview.is-transverse .layout-card__aside {
  width: 100%;
  height: 26%;
  bottom: auto;
}

.layout-card__preview.is-transverse .layout-card__main {
  left: 0;
  top: 26%;
}

.layout-card__preview.is-classic .layout-card__aside {
  width: 26%;
  height: 100%;
}

.layout-card__preview.is-classic::before {
  content: '';
  position: absolute;
  left: 26%;
  right: 0;
  top: 0;
  height: 22%;
  background: var(--app-bg-header, #fff);
  border-bottom: 1px solid var(--el-border-color-lighter);
  z-index: 1;
}

.layout-card__preview.is-columns .layout-card__aside {
  width: 16%;
  background: var(--app-bg-columns, #545c64);
}

.layout-card__preview.is-columns::after {
  content: '';
  position: absolute;
  left: 16%;
  top: 0;
  bottom: 0;
  width: 20%;
  background: var(--app-bg-sidebar, #001529);
  opacity: 0.8;
}

.layout-card__label {
  font-size: 12px;
  color: var(--app-text-regular);
}

.theme-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
