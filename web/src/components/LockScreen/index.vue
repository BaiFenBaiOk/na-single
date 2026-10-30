<template>
  <div class="lock-screen">
    <div class="lock-card">
      <el-avatar :size="72" :src="userStore.avatar || undefined" class="lock-avatar">
        {{ nameInitial }}
      </el-avatar>
      <div class="lock-name">{{ userStore.displayName || '用户' }}</div>
      <div class="lock-title">{{ t('lock.title') }}</div>
      <div class="lock-subtitle">{{ t('lock.subtitle') }}</div>

      <el-input
        ref="passwordRef"
        v-model="password"
        type="password"
        show-password
        size="large"
        autocomplete="new-password"
        class="lock-input"
        :placeholder="t('lock.placeholder')"
        @keyup.enter="unlock"
      />
      <el-button
        type="primary"
        size="large"
        class="lock-button"
        :loading="loading"
        @click="unlock"
      >
        {{ t('lock.unlock') }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 全屏锁屏遮罩。
 *
 * 锁屏状态保存在 app store 并持久化，刷新后仍然是锁定状态；
 * 解锁时调用后端校验当前登录用户的登录密码。
 */
import { computed, nextTick, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import type { InputInstance } from 'element-plus'
import { verifyPassword } from '@/api/user'
import { useAppStore } from '@/store/modules/app'
import { useUserStore } from '@/store/modules/user'

const { t } = useI18n()
const appStore = useAppStore()
const userStore = useUserStore()

const password = ref('')
const loading = ref(false)
const passwordRef = ref<InputInstance>()

const nameInitial = computed(() => (userStore.displayName || 'U').slice(0, 1).toUpperCase())

onMounted(async () => {
  await nextTick()
  passwordRef.value?.focus()
})

async function unlock() {
  const value = password.value.trim()
  if (!value) {
    ElMessage.warning(t('lock.passwordRequired'))
    return
  }
  if (loading.value) return
  loading.value = true
  try {
    await verifyPassword(value)
    password.value = ''
    appStore.unlockScreen()
  } catch (e) {
    // 后端返回“密码错误”等业务提示时统一用锁屏文案
    ElMessage.error(t('lock.error'))
    password.value = ''
    await nextTick()
    passwordRef.value?.focus()
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.lock-screen {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(135deg, #1f2a44 0%, #2f6fed 100%);
}

.lock-card {
  width: 360px;
  max-width: 100%;
  padding: 32px 28px 28px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.25);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.lock-avatar {
  background: var(--app-color-primary);
  font-size: 26px;
  margin-bottom: 10px;
}

.lock-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--app-text-primary);
}

.lock-title {
  margin-top: 14px;
  font-size: 20px;
  font-weight: 600;
  color: var(--app-text-primary);
}

.lock-subtitle {
  margin-top: 6px;
  margin-bottom: 18px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--app-text-secondary);
}

.lock-input {
  width: 100%;
}

.lock-button {
  width: 100%;
  margin-top: 14px;
}
</style>
