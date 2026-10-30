<template>
  <PageContainer title="个人中心" subtitle="当前登录账号信息（只读，后端暂未提供资料修改接口）">
    <el-card shadow="never" class="profile-head">
      <el-avatar :size="64" :src="userStore.avatar || undefined" class="profile-avatar">
        {{ nameInitial }}
      </el-avatar>
      <div class="profile-meta">
        <h3 class="profile-name">{{ userStore.displayName }}</h3>
        <div class="profile-tags">
          <el-tag :type="userTypeItem?.tagType" size="small">{{ userTypeItem?.label || '未知' }}</el-tag>
          <el-tag :type="statusOk ? 'success' : 'danger'" size="small">
            {{ statusOk ? '正常' : '停用' }}
          </el-tag>
          <el-tag v-if="userStore.isSysUser" type="danger" effect="dark" size="small">平台方</el-tag>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="profile-card">
      <template #header><span>基本信息</span></template>
      <el-descriptions :column="descColumn" border size="small">
        <el-descriptions-item label="账号">{{ info.userName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ info.nickName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户类型">{{ userTypeItem?.label || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ joinSet(roleNames) }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ joinSet(deptNames) }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ joinSet(postNames) }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ info.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ info.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ fmtTime(info.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="profile-card">
      <template #header><span>本次登录</span></template>
      <el-descriptions :column="descColumn" border size="small">
        <el-descriptions-item label="登录时间">
          {{ fmtTime(userStore.loginInfo?.loginTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="登录 IP">
          {{ userStore.loginInfo?.loginIp || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="归属地" :span="2">
          {{ joinAddress(userStore.loginInfo?.loginAddress) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import PageContainer from '@/components/PageContainer/index.vue'
import { useUserStore } from '@/store/modules/user'
import { USER_TYPE_DICT, findDict } from '@/constants/dict'
import { fmtTime, joinSet, joinAddress } from '@/utils/format'
import { useDevice } from '@/composables/useDevice'

const { isMobile } = useDevice()
const userStore = useUserStore()

// 优先取详情接口数据，缺失字段用登录返回补
const info = computed(() => ({
  ...(userStore.loginInfo || {}),
  ...(userStore.userInfo || {})
}))

const descColumn = computed(() => (isMobile.value ? 1 : 2))
const nameInitial = computed(() => (userStore.displayName || 'U').slice(0, 1).toUpperCase())
const userTypeItem = computed(() => findDict(USER_TYPE_DICT, info.value.userType))
// 登录返回的 deptName 是字符串、用户详情是数组，统一归一成字符串数组
const asStrArray = (v: unknown): string[] =>
  Array.isArray(v) ? v.filter((x): x is string => typeof x === 'string') : v ? [String(v)] : []
const roleNames = computed(() => asStrArray(info.value.roleNames))
const deptNames = computed(() => asStrArray(info.value.deptName))
const postNames = computed(() => asStrArray(info.value.postName))
// 账号状态：sys_user.status 用 Boolean 承载 0/1——false=正常、true=停用，故需取反。
// （用户列表接口的 status 是 Integer 0/1，与这里不同，不要混淆）
const statusOk = computed(() =>
  typeof info.value.status === 'boolean' ? !info.value.status : true
)
</script>

<style lang="scss" scoped>
.profile-head {
  display: flex;
  align-items: center;
  margin-bottom: 12px;

  :deep(.el-card__body) {
    display: flex;
    align-items: center;
    gap: 16px;
  }
}

.profile-avatar {
  background-color: var(--app-color-primary);
  font-size: 26px;
  flex-shrink: 0;
}

.profile-name {
  margin: 0 0 8px;
  font-size: 17px;
}

.profile-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.profile-card {
  margin-bottom: 12px;
}
</style>
