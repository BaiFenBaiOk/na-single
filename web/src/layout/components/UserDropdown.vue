<template>
  <el-dropdown trigger="click" @command="onCommand">
    <span class="user-trigger">
      <el-avatar :size="30" :src="userStore.avatar || undefined" class="avatar">
        {{ nameInitial }}
      </el-avatar>
      <span class="user-meta">
        <span class="name">{{ userStore.displayName || '用户' }}</span>
      </span>
      <el-tag v-if="userStore.isSysUser" size="small" type="danger" effect="dark" class="platform-tag">
        平台
      </el-tag>
      <el-icon class="caret"><ArrowDown /></el-icon>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="profile">
          <el-icon><User /></el-icon>{{ t('navbar.profile') }}
        </el-dropdown-item>
        <el-dropdown-item command="monitor">
          <el-icon><Monitor /></el-icon>{{ t('navbar.monitor') }}
        </el-dropdown-item>
        <el-dropdown-item command="theme" divided>
          <el-icon><Brush /></el-icon>{{ t('navbar.theme') }}
        </el-dropdown-item>
        <el-dropdown-item command="language">
          <el-icon><Switch /></el-icon>{{ t('navbar.language') }}
        </el-dropdown-item>
        <el-dropdown-item command="fullscreen">
          <el-icon><FullScreen /></el-icon>
          {{ isFullscreen ? t('navbar.exitFullscreen') : t('navbar.fullscreen') }}
        </el-dropdown-item>
        <el-dropdown-item v-if="appStore.theme.lockScreenEnabled" command="lock">
          <el-icon><Lock /></el-icon>{{ t('navbar.lock') }}
        </el-dropdown-item>
        <el-dropdown-item command="logout" divided>
          <el-icon><SwitchButton /></el-icon>{{ t('navbar.logout') }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  Brush,
  FullScreen,
  Lock,
  Monitor,
  Switch,
  SwitchButton,
  User
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'
import { usePermissionStore } from '@/store/modules/permission'
import { useAppStore } from '@/store/modules/app'
import { resetRouter } from '@/router'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()
const appStore = useAppStore()

/** 浏览器全屏状态 */
const isFullscreen = ref(false)

function syncFullscreen() {
  isFullscreen.value = !!document.fullscreenElement
}

onMounted(() => document.addEventListener('fullscreenchange', syncFullscreen))
onBeforeUnmount(() => document.removeEventListener('fullscreenchange', syncFullscreen))

/** 切换全屏 */
async function toggleFullscreen() {
  try {
    if (document.fullscreenElement) {
      await document.exitFullscreen()
    } else {
      await document.documentElement.requestFullscreen()
    }
  } catch {
    ElMessage.warning(t('navbar.fullscreenUnsupported'))
  }
}

/** 中英文切换 */
function toggleLanguage() {
  appStore.setLanguage(appStore.language === 'en' ? 'zh-CN' : 'en')
}

/** 锁屏 */
function lockScreen() {
  appStore.lockScreen()
}

const nameInitial = computed(() => (userStore.displayName || 'U').slice(0, 1).toUpperCase())

async function onCommand(command: string) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'monitor') {
    router.push('/monitor')
  } else if (command === 'theme') {
    appStore.openThemeDrawer()
  } else if (command === 'language') {
    toggleLanguage()
  } else if (command === 'fullscreen') {
    await toggleFullscreen()
  } else if (command === 'lock') {
    lockScreen()
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm(t('navbar.logoutConfirm'), t('navbar.logoutTitle'), {
        confirmButtonText: t('navbar.logoutButton'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      })
    } catch {
      return // 取消
    }
    await userStore.logout()
    permissionStore.reset()
    resetRouter()
    appStore.unlockScreen()
    router.push('/login')
  }
}
</script>

<style lang="scss" scoped>
.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}

.avatar {
  flex-shrink: 0;
  background-color: var(--app-color-primary);
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
  max-width: 160px;
}

.name {
  font-size: 13px;
  color: var(--app-text-primary);
  @include ellipsis;
}

.caret {
  color: var(--app-text-secondary);
}

// H5 隐藏文字，只留头像，节省空间
@media (max-width: 767.98px) {
  .user-meta,
  .platform-tag,
  .caret {
    display: none;
  }
}
</style>
