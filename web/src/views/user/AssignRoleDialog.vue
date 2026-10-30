<template>
  <el-dialog
    :model-value="modelValue"
    :title="`分配角色 - ${user?.nickName || user?.userName || ''}`"
    class="app-responsive-dialog"
    :width="isMobile ? '94vw' : '460px'"
    :close-on-click-modal="false"
    append-to-body
    @update:model-value="(v: boolean) => emit('update:modelValue', v)"
    @open="onOpen"
  >
    <div v-loading="loading" class="role-box">
      <el-checkbox-group v-model="checkedRoles">
        <el-scrollbar max-height="320px">
          <el-checkbox
            v-for="r in roleOptions"
            :key="r.id"
            :value="r.id"
            class="role-item"
          >
            {{ r.roleName }}
            <span class="role-code">（{{ r.roleCode }}）</span>
          </el-checkbox>
          <el-empty v-if="!loading && roleOptions.length === 0" description="暂无角色" :image-size="60" />
        </el-scrollbar>
      </el-checkbox-group>
    </div>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { bindRoles, getBindRoles } from '@/api/user'
import { rolePage } from '@/api/role'
import type { RoleRecord } from '@/api/types/role'
import type { UserRecord } from '@/api/types/user'
import type { IdStr } from '@/api/types/common'
import { useDevice } from '@/composables/useDevice'

const props = defineProps<{
  modelValue: boolean
  user?: UserRecord | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  saved: []
}>()

const { isMobile } = useDevice()
const loading = ref(false)
const saving = ref(false)
const roleOptions = ref<RoleRecord[]>([])
const checkedRoles = ref<IdStr[]>([])

async function onOpen() {
  if (!props.user) return
  loading.value = true
  checkedRoles.value = []
  try {
    // 同时拉取角色列表与该用户已绑角色
    const [roles, bound] = await Promise.all([
      rolePage({ currentPage: 1, pageSize: 200 }),
      getBindRoles(props.user.id)
    ])
    roleOptions.value = roles.records
    checkedRoles.value = Array.isArray(bound) ? bound : []
  } finally {
    loading.value = false
  }
}

async function onSubmit() {
  if (!props.user) return
  saving.value = true
  try {
    // 空数组 = 取消该用户的全部角色
    await bindRoles({ userId: props.user.id, roleIds: checkedRoles.value })
    ElMessage.success(checkedRoles.value.length ? '角色分配成功' : '已取消全部角色')
    emit('saved')
    emit('update:modelValue', false)
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.role-box {
  min-height: 120px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  padding: 8px 12px;
}

.role-item {
  display: flex;
  width: 100%;
  margin: 6px 0;
}

.role-code {
  color: var(--app-text-secondary);
  font-size: 12px;
}
</style>
