<template>
  <PageContainer title="角色管理" subtitle="角色菜单从全量菜单中分配；数据范围为「全部」仅系统用户可设">
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" class="filter-form" @submit.prevent>
        <el-form-item label="角色名称">
          <el-input v-model="table.query.roleName" placeholder="名称" clearable @keyup.enter="table.handleSearch()" />
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
        <el-table-column prop="roleName" label="角色名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="roleCode" label="角色编码" min-width="130" show-overflow-tooltip />
        <el-table-column label="数据范围" width="130">
          <template #default="{ row }">
            <el-tag :type="scopeTag(row.dataScope)" size="small">{{ scopeLabel(row.dataScope) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile" label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="roleTypeTag(row.roleType)" size="small">{{ roleTypeLabel(row.roleType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile" prop="roleSort" label="排序" width="60" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="!row.status"
              :disabled="isReadonly(row as RoleRecord) || !canUpdate"
              @change="(v) => onToggleStatus(row as RoleRecord, v as boolean)"
            />
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile" prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" :width="isMobile ? 130 : 190" align="center" fixed="right">
          <template #default="{ row }">
            <MenuButtons
              link
              size="small"
              :row="row as RoleRecord"
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

    <RoleFormDialog
      v-model="formVisible"
      :record="current"
      @saved="table.fetchList"
    />
    <RoleMenuDialog v-model="menuVisible" :role="current" />
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import MenuButtons from '@/components/MenuButtons/index.vue'
import type { MenuAction } from '@/components/MenuButtons/types'
import { useMenuButtons } from '@/composables/useMenuButtons'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshLeft, Plus } from '@element-plus/icons-vue'
import PageContainer from '@/components/PageContainer/index.vue'
import RoleFormDialog from './RoleFormDialog.vue'
import RoleMenuDialog from './RoleMenuDialog.vue'
import { rolePage, updateRole, removeRole } from '@/api/role'
import type { RolePageQuery, RoleRecord } from '@/api/types/role'
import { useTable } from '@/composables/useTable'
import {
  STATUS_DICT,
  DATA_SCOPE_DICT,
  ROLE_TYPE_DICT,
  findDict
} from '@/constants/dict'
import { SYS_USER_TYPE } from '@/constants/businessCode'
import { fmtTime } from '@/utils/format'
import { useDevice } from '@/composables/useDevice'

const { isMobile } = useDevice()

const table = useTable<RolePageQuery, RoleRecord>({
  fetcher: rolePage,
  initialQuery: { currentPage: 1, pageSize: 10, roleName: '', status: '' },
  pageSize: 10
})

const formVisible = ref(false)
const menuVisible = ref(false)
const current = ref<RoleRecord | null>(null)

function scopeLabel(v: string) {
  return findDict(DATA_SCOPE_DICT, v)?.label || v
}
function scopeTag(v: string) {
  return findDict(DATA_SCOPE_DICT, v)?.tagType || 'info'
}
function roleTypeLabel(v: string) {
  return findDict(ROLE_TYPE_DICT, v)?.label || v
}
function roleTypeTag(v: string) {
  return findDict(ROLE_TYPE_DICT, v)?.tagType || 'info'
}

/**
 * 数据写入类操作（编辑 / 分配菜单 / 启停 / 删除）不可用的原因；空串表示可用。
 * 系统角色（00）为内置保护数据，任何操作者都不可操作。
 */
function readonlyReason(row: RoleRecord): string {
  if (row.roleType === SYS_USER_TYPE.SYS_DEF) {
    return '系统角色为内置数据，不可操作'
  }
  return ''
}

function isReadonly(row: RoleRecord): boolean {
  return readonlyReason(row) !== ''
}

const { isReady, hasPerm } = useMenuButtons()
const canUpdate = computed(() => isReady.value && hasPerm('role:update:post'))

const toolbarActions: Record<string, MenuAction<RoleRecord>> = {
  'role:save:post': {
    handler: onAdd,
    icon: Plus,
    type: 'success'
  }
}

const rowActions: Record<string, MenuAction<RoleRecord>> = {
  'role:update:post': {
    handler: onEdit,
    type: 'primary',
    disabled: isReadonly,
    tooltip: readonlyReason
  },
  'role:bind:menu': {
    handler: onAssignMenu,
    type: 'warning',
    disabled: isReadonly,
    tooltip: readonlyReason
  },
  'role:delete:delete': {
    handler: onDelete,
    type: 'danger',
    disabled: isReadonly,
    tooltip: readonlyReason
  }
}

function onAdd() {
  current.value = null
  formVisible.value = true
}
function onEdit(row: RoleRecord) {
  current.value = row
  formVisible.value = true
}
function onAssignMenu(row: RoleRecord) {
  current.value = row
  menuVisible.value = true
}

async function onDelete(row: RoleRecord) {
  try {
    await ElMessageBox.confirm(
      `确定删除角色「${row.roleName}」吗？若仍有用户绑定该角色，需要先解除绑定。`,
      '删除角色',
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
    await removeRole(row.id)
    ElMessage.success('已删除')
    await table.fetchList()
  } catch {
    // 错误提示（含「仍有用户绑定」的拒绝原因）已由拦截器处理
    await table.fetchList()
  }
}

async function onToggleStatus(row: RoleRecord, enabled: boolean) {
  try {
    // 状态用 Boolean 承载：false 正常 / true 停用，故取反
    await updateRole({ id: row.id, roleName: row.roleName, status: !enabled })
    ElMessage.success(enabled ? '已启用' : '已停用')
    await table.fetchList()
  } catch {
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
    padding-bottom: 10px;
  }
}

.filter-tip {
  font-size: 12px;
  color: var(--app-text-secondary);
  margin: -4px 0 8px;
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
