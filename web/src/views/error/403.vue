<template>
  <div class="error-page">
    <el-result icon="warning" title="403" sub-title="抱歉，您没有权限访问此页面">
      <template #extra>
        <el-button type="primary" @click="goHome">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { resetRouter } from '@/router'
import { useUserStore } from '@/store/modules/user'
import { usePermissionStore } from '@/store/modules/permission'

const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

async function goHome() {
  // 未登录：直接回登录页
  if (!userStore.token) {
    router.replace('/login')
    return
  }
  // 已登录但没有任何菜单——该账号登录后无处可去。
  // 必须先清理登录态再跳登录页：否则守卫会把已登录的用户从 /login 弹回首页，
  // 再因无菜单回到本页，形成循环（表现为"点了没反应"）。
  if (permissionStore.menus.length === 0) {
    await userStore.logout(false)
    permissionStore.reset()
    resetRouter()
    router.replace('/login')
    return
  }
  router.replace('/')
}
</script>

<style lang="scss" scoped>
.error-page {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--app-bg-page);
}
</style>
