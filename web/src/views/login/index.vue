<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-brand">
        <img src="/favicon.svg" alt="logo" class="login-logo" />
        <h1 class="login-title">na 管理平台</h1>
        <p class="login-subtitle">RBAC 权限管理系统 V3</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
        label-position="top"
        @keyup.enter="onSubmit"
      >
        <el-form-item prop="userName">
          <el-input
            v-model="form.userName"
            placeholder="请输入账号"
            :prefix-icon="User"
            clearable
            autocomplete="username"
          />
        </el-form-item>

        <el-form-item prop="passWord">
          <el-input
            v-model="form.passWord"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            autocomplete="current-password"
          />
        </el-form-item>

        <el-form-item prop="captcha">
          <div class="captcha-row">
            <el-input
              v-model="form.captcha"
              placeholder="验证码"
              :prefix-icon="Key"
              maxlength="6"
              clearable
            />
            <img
              :src="captchaUrl"
              alt="验证码"
              title="点击刷新验证码"
              class="captcha-img"
              @click="refreshCaptcha"
            />
          </div>
        </el-form-item>

        <el-button
          type="primary"
          class="login-btn"
          :loading="loading"
          @click="onSubmit"
        >
          登 录
        </el-button>
      </el-form>
    </div>

    <p class="login-footer">Copyright © {{ year }} na 管理平台</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'
import { usePermissionStore } from '@/store/modules/permission'
import { BIZ_CODE } from '@/constants/businessCode'
import { BizError } from '@/api/http/request'
import { buildCaptchaUrl, newUuid } from '@/utils/captcha'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const captchaUuid = ref('')
const captchaUrl = ref('')

const form = reactive({
  userName: '',
  passWord: '',
  captcha: ''
})

const rules: FormRules = {
  userName: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  passWord: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const year = new Date().getFullYear()

/** 重新生成 uuid 并换一张验证码图 */
function refreshCaptcha() {
  captchaUuid.value = newUuid()
  captchaUrl.value = buildCaptchaUrl(captchaUuid.value)
}

async function onSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login({
      userName: form.userName.trim(),
      passWord: form.passWord,
      uuid: captchaUuid.value,
      captcha: form.captcha.trim()
    })
    // 动态菜单与路由由全局守卫负责加载，这里只负责跳转到目标页
    permissionStore.reset()
    const redirect = (route.query.redirect as string) || '/'
    router.replace(redirect)
  } catch (e) {
    // 验证码错误 / 失效：换新图并清空输入（错误提示已由响应拦截器统一弹出）
    if (e instanceof BizError && e.code === BIZ_CODE.CAPTCHA_ERROR) {
      form.captcha = ''
      refreshCaptcha()
    }
  } finally {
    loading.value = false
  }
}

onMounted(refreshCaptcha)
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
  padding: 16px;
  background: linear-gradient(135deg, #0f2a5e 0%, #1d6fff 100%);
}

.login-card {
  width: 400px;
  max-width: 94vw;
  padding: 32px 32px 28px;
  background: var(--app-bg-card);
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
}

.login-brand {
  text-align: center;
  margin-bottom: 22px;
}

.login-logo {
  width: 52px;
  height: 52px;
}

.login-title {
  margin: 10px 0 4px;
  font-size: 20px;
  font-weight: 600;
  color: var(--app-text-primary);
}

.login-subtitle {
  margin: 0;
  font-size: 12px;
  color: var(--app-text-secondary);
}

.captcha-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

.captcha-img {
  width: 120px;
  height: 40px;
  flex-shrink: 0;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid var(--el-border-color);
  // 验证码图通常为浅底，避免深色模式下突兀
  background-color: #f5f7fa;
}

.login-btn {
  width: 100%;
  margin-top: 4px;
}

.login-footer {
  color: rgba(255, 255, 255, 0.75);
  font-size: 12px;
  margin: 0;
}

@media (max-width: 767.98px) {
  .login-card {
    padding: 24px 20px 20px;
  }
}
</style>
