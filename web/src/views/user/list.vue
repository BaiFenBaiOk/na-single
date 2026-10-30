<template>
  <PageContainer title="用户管理" subtitle="账号全局唯一，逻辑删除后仍占用；列表不展示密码">
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" class="filter-form" @submit.prevent>
        <el-form-item label="账号">
          <el-input v-model="table.query.userName" placeholder="账号" clearable @keyup.enter="table.handleSearch()" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="table.query.status" placeholder="全部" clearable style="width: 110px">
            <el-option v-for="d in STATUS_DICT" :key="String(d.value)" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" :icon="Search" @click="table.handleSearch()">查询</el-button>
          <el-button :icon="RefreshLeft" @click="table.handleReset()">重置</el-button>
          <MenuButtons :actions="toolbarActions" style="margin-left: 12px" />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="table.loading.value" :data="table.records.value" border size="small">
        <el-table-column label="昵称" min-width="130">
          <template #default="{ row }">
            <span class="online-cell">
              <span v-if="isOnline(row as UserRecord)" class="online-dot" title="在线" />
              {{ row.nickName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="账号" min-width="120" show-overflow-tooltip />
        <el-table-column v-if="!isMobile" label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="userTypeTag(row.userType)" size="small">{{ userTypeLabel(row.userType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile" prop="phone" label="手机号" width="130" />
        <el-table-column v-if="!isMobile" prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column label="角色" min-width="120">
          <template #default="{ row }">
            {{ roleNames(row as UserRecord) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="!row.status"
              :disabled="isReadonly(row as UserRecord) || !canUpdate"
              @change="(v) => onToggleStatus(row as UserRecord, v as boolean)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="isMobile ? 260 : 380" align="center" fixed="right">
          <template #default="{ row }">
            <MenuButtons
              link
              size="small"
              :row="row as UserRecord"
              :actions="rowActions"
            />
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        :current-page="table.query.currentPage"
        :page-size="table.query.pageSize"
        :total="table.total.value"
        :page-sizes="[10, 20, 50]"
        :small="isMobile"
        :layout="isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
        @current-change="table.handlePageChange"
        @size-change="table.handleSizeChange"
      />
    </el-card>

    <UserFormDialog
      v-model="formVisible"
      :record="current"
      @saved="table.fetchList"
    />
    <AssignRoleDialog v-model="roleVisible" :user="current" @saved="table.fetchList" />
    <AssignDeptDialog v-model="deptVisible" :user="current" @saved="table.fetchList" />
    <AssignPostDialog v-model="postVisible" :user="current" @saved="table.fetchList" />
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshLeft, Plus } from '@element-plus/icons-vue'
import PageContainer from '@/components/PageContainer/index.vue'
import MenuButtons from '@/components/MenuButtons/index.vue'
import type { MenuAction } from '@/components/MenuButtons/types'
import { useMenuButtons } from '@/composables/useMenuButtons'
import UserFormDialog from './UserFormDialog.vue'
import AssignRoleDialog from './AssignRoleDialog.vue'
import AssignDeptDialog from './AssignDeptDialog.vue'
import AssignPostDialog from './AssignPostDialog.vue'
import { userPage, updateUser, kickUser, removeUser } from '@/api/user'
import type { UserPageQuery, UserRecord } from '@/api/types/user'
import { useTable } from '@/composables/useTable'
import { STATUS_DICT, USER_TYPE_DICT, findDict } from '@/constants/dict'
import { SYS_USER_TYPE } from '@/constants/businessCode'
import { useDevice } from '@/composables/useDevice'
import { useUserStore } from '@/store/modules/user'

const { isMobile } = useDevice()
const userStore = useUserStore()

const table = useTable<UserPageQuery, UserRecord>({
  fetcher: userPage,
  initialQuery: { currentPage: 1, pageSize: 10, userName: '', status: '' },
  pageSize: 10
})

const formVisible = ref(false)
const roleVisible = ref(false)
const deptVisible = ref(false)
const postVisible = ref(false)
const current = ref<UserRecord | null>(null)

function userTypeLabel(v: string) {
  return findDict(USER_TYPE_DICT, v)?.label || v
}
function userTypeTag(v: string) {
  return findDict(USER_TYPE_DICT, v)?.tagType || 'info'
}

/**
 * 在线：tokens 中存在有效令牌。
 * 后端无令牌时可能返回 [null]，故需过滤空值，否则会误判为在线。
 */
function isOnline(row: UserRecord): boolean {
  return (
    Array.isArray(row.tokens) &&
    row.tokens.some((t) => t !== null && t !== undefined && t !== '')
  )
}

/** 角色名称拼接（不渲染任何密码字段） */
function roleNames(row: UserRecord): string {
  return (row.roles || []).map((r) => r.roleName).filter(Boolean).join('、') || '-'
}

/** 是否为当前登录账号自身 */
function isSelf(row: UserRecord): boolean {
  return !!row.id && row.id === userStore.loginInfo?.userId
}

/**
 * 数据写入类操作（编辑 / 分配角色 / 启停）不可用的原因；空串表示可用。
 * 1. 系统用户（00）为内置保护数据，任何操作者都不可操作；
 * 2. 当前登录账号自身。
 */
function readonlyReason(row: UserRecord): string {
  if (row.userType === SYS_USER_TYPE.SYS_DEF) {
    return '系统用户为内置数据，不可操作'
  }
  if (isSelf(row)) {
    return '不能操作当前登录的账号'
  }
  return ''
}

function isReadonly(row: UserRecord): boolean {
  return readonlyReason(row) !== ''
}

/**
 * 下线按钮不可用的原因；空串表示可用。
 * 踢下线只清理登录令牌、不改动用户数据，因此不受只读规则限制，
 * 也不以在线与否为条件（无令牌时后端清理是幂等的）——除自己外任何人都可下线。
 */
function kickDisabledReason(row: UserRecord): string {
  return isSelf(row) ? '不能将自己踢下线' : ''
}

const { isReady, hasPerm } = useMenuButtons()
const canUpdate = computed(() => isReady.value && hasPerm('user:update:post'))

const toolbarActions: Record<string, MenuAction<UserRecord>> = {
  'user:save:post': {
    handler: onAdd,
    icon: Plus,
    type: 'success'
  }
}

const rowActions: Record<string, MenuAction<UserRecord>> = {
  'user:update:post': {
    handler: onEdit,
    type: 'primary',
    disabled: (row) => isReadonly(row),
    tooltip: (row) => readonlyReason(row)
  },
  'user:bind:role': {
    handler: onAssignRole,
    type: 'success',
    disabled: (row) => isReadonly(row),
    tooltip: (row) => readonlyReason(row)
  },
  'user:bind:dept': {
    handler: onAssignDept,
    type: 'success',
    disabled: (row) => isReadonly(row),
    tooltip: (row) => readonlyReason(row)
  },
  'user:bind:post': {
    handler: onAssignPost,
    type: 'success',
    disabled: (row) => isReadonly(row),
    tooltip: (row) => readonlyReason(row)
  },
  // 下线只清登录令牌、不改数据，不受只读规则限制，只禁止操作自己
  'user:clearToken:get': {
    handler: onKick,
    type: 'danger',
    disabled: (row) => kickDisabledReason(row) !== '',
    tooltip: (row) => kickDisabledReason(row)
  },
  'user:delete:delete': {
    handler: onDelete,
    type: 'danger',
    disabled: (row) => isReadonly(row),
    tooltip: (row) => readonlyReason(row)
  }
}

function onAdd() {
  current.value = null
  formVisible.value = true
}
function onEdit(row: UserRecord) {
  current.value = row
  formVisible.value = true
}
function onAssignRole(row: UserRecord) {
  current.value = row
  roleVisible.value = true
}
function onAssignDept(row: UserRecord) {
  current.value = row
  deptVisible.value = true
}
function onAssignPost(row: UserRecord) {
  current.value = row
  postVisible.value = true
}

async function onToggleStatus(row: UserRecord, enabled: boolean) {
  try {
    // 账号状态用 Boolean 承载：false 正常 / true 停用，故取反
    await updateUser({ id: row.id, userName: row.userName, nickName: row.nickName, status: !enabled })
    ElMessage.success(enabled ? '已启用' : '已停用')
    await table.fetchList()
  } catch {
    await table.fetchList()
  }
}

async function onKick(row: UserRecord) {
  try {
    await ElMessageBox.confirm(`确定将「${row.nickName}」强制下线吗？`, '踢下线', {
      confirmButtonText: '下线',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await kickUser(row.id)
    ElMessage.success('已强制下线')
    await table.fetchList()
  } catch {
    // 错误提示已由拦截器处理
  }
}

async function onDelete(row: UserRecord) {
  try {
    await ElMessageBox.confirm(
      `确定删除用户「${row.nickName}」吗？若仍有角色绑定，需要先在「分配角色」中解除。`,
      '删除用户',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }
  try {
    await removeUser(row.id)
    ElMessage.success('已删除')
    await table.fetchList()
  } catch {
    // 错误提示（含「请先解除用户与角色的关系」的拒绝原因）已由拦截器处理
    await table.fetchList()
  }
}

onMounted(() => {
  table.fetchList()
})
</script>

<style lang="scss" scoped>
.filter-card {
  margin-bottom: 12px;

  :deep(.el-card__body) {
    padding-bottom: 2px;
  }
}

.online-cell {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.online-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background-color: #22c55e;
  flex-shrink: 0;
}

.pager {
  margin-top: 12px;
  justify-content: flex-end;
}

@media (max-width: 767.98px) {
  .pager {
    justify-content: center;
  }
}
</style>
