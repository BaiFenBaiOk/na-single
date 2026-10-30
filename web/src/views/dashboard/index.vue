<template>
  <PageContainer>
    <el-card shadow="never" class="welcome-card">
      <div class="welcome">
        <el-avatar :size="56" :src="userStore.avatar || undefined" class="welcome-avatar">
          {{ nameInitial }}
        </el-avatar>
        <div class="welcome-text">
          <h2 class="welcome-title">
            你好，{{ userStore.displayName || userStore.loginInfo?.userName }}
          </h2>
          <p class="welcome-desc">
            <el-tag v-if="userStore.isSysUser" type="danger" size="small">系统用户</el-tag>
            <el-tag v-else type="success" size="small">普通用户</el-tag>
          </p>
        </div>
      </div>
    </el-card>

    <el-row :gutter="12" class="info-row">
      <el-col :xs="24" :sm="12">
        <el-card shadow="never" class="info-card">
          <template #header><span>本次登录</span></template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="登录账号">
              {{ userStore.loginInfo?.userName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="登录时间">
              {{ fmtTime(userStore.loginInfo?.loginTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="登录 IP">
              {{ userStore.loginInfo?.loginIp || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="归属地">
              {{ joinAddress(userStore.loginInfo?.loginAddress) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12">
        <el-card shadow="never" class="info-card">
          <template #header><span>快捷入口</span></template>
          <div class="quick-links">
            <el-button :icon="MenuIcon" @click="router.push(permissionStore.firstPath)">
              进入工作台
            </el-button>
            <el-button :icon="UserFilled" @click="router.push('/profile')">个人中心</el-button>
          </div>
          <el-alert
            class="tip"
            type="info"
            :closable="false"
            title="提示"
            description="左侧菜单按当前账号被授权的菜单动态生成；系统用户（00）可维护菜单与系统数据。"
          />
        </el-card>
      </el-col>
    </el-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Menu as MenuIcon, UserFilled } from '@element-plus/icons-vue'
import PageContainer from '@/components/PageContainer/index.vue'
import { useUserStore } from '@/store/modules/user'
import { usePermissionStore } from '@/store/modules/permission'
import { fmtTime, joinAddress } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

const nameInitial = computed(() =>
  (userStore.displayName || userStore.loginInfo?.userName || 'U').slice(0, 1).toUpperCase()
)
</script>

<style lang="scss" scoped>
.welcome-card {
  margin-bottom: 12px;
}

.welcome {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome-avatar {
  background-color: var(--app-color-primary);
  font-size: 22px;
}

.welcome-title {
  margin: 0 0 6px;
  font-size: 18px;
}

.welcome-desc {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: var(--app-text-secondary);
}

.info-row {
  margin: 0 !important;
}

.info-card {
  margin-bottom: 12px;
}

.quick-links {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 14px;
}

.tip {
  margin-top: 4px;
}
</style>
