<template>
  <PageContainer title="菜单管理" subtitle="仅系统用户（00）可维护；角色菜单从全量菜单中分配">
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="keyword" class="filter-form" @submit.prevent>
        <el-form-item label="菜单名称">
          <el-input
            v-model="keyword.menuName"
            placeholder="名称关键字"
            clearable
            @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" :icon="Search" @click="onSearch">查询</el-button>
          <el-button :icon="RefreshLeft" @click="onReset">重置</el-button>
          <MenuButtons :actions="toolbarActions" style="margin-left: 12px" />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table
        v-loading="loading"
        :data="treeData"
        row-key="id"
        :tree-props="{ children: 'children' }"
        default-expand-all
        border
        size="small"
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="180">
          <template #default="{ row }">
            <el-icon v-if="row.icon && row.menuType !== 'F' && row.menuType !== 'A'" class="row-icon">
              <component :is="row.icon" />
            </el-icon>
            <span>{{ row.menuName }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile" label="类型" width="72" align="center">
          <template #default="{ row }">
            <el-tag :type="menuTypeTag(row.menuType)" size="small">
              {{ menuTypeLabel(row.menuType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile" prop="perms" label="权限标识" min-width="150" show-overflow-tooltip />
        <el-table-column v-if="!isMobile" prop="path" label="路由地址" min-width="120" show-overflow-tooltip />
        <el-table-column v-if="!isMobile" prop="component" label="组件路径" min-width="140" show-overflow-tooltip />
        <el-table-column v-if="!isMobile" prop="requestUri" label="请求地址" min-width="150" show-overflow-tooltip />
        <el-table-column v-if="!isMobile" prop="orderNum" label="排序" width="60" align="center" />
        <el-table-column label="显示" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.visible === '1' ? 'info' : 'success'" size="small">
              {{ row.visible === '1' ? '隐藏' : '显示' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'danger' : 'success'" size="small">
              {{ row.status === '1' ? '停用' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile" label="归属" width="84" align="center">
          <template #default="{ row }">
            {{ ownerLabel(row.sysMenuType) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="isMobile ? 130 : 190" align="center" fixed="right">
          <template #default="{ row }">
            <!--
              系统菜单（00）为内置保护数据，前后端均不可变更；
              确需调整时由开发人员直接操作数据库。后端 updateObj 有同名校验兜底。
            -->
            <span v-if="isSysMenu(row as MenuNode)" class="sys-menu-tip">—</span>
            <template v-else>
              <MenuButtons
                link
                size="small"
                :row="row as MenuNode"
                :actions="rowActions"
              />
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <MenuFormDialog
      v-model="dialogVisible"
      :tree="treeData"
      :record="current"
      :parent="parentNode"
      @saved="fetchTree"
    />
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshLeft, Plus } from '@element-plus/icons-vue'
import PageContainer from '@/components/PageContainer/index.vue'
import MenuButtons from '@/components/MenuButtons/index.vue'
import type { MenuAction } from '@/components/MenuButtons/types'
import MenuFormDialog from './MenuFormDialog.vue'
import { menuTreePage, removeMenu } from '@/api/menu'
import type { MenuNode, MenuType } from '@/api/types/menu'
import { MENU_TYPE_DICT, MENU_OWNER_DICT, findDict } from '@/constants/dict'
import { SYS_MENU_TYPE } from '@/constants/businessCode'
import { useDevice } from '@/composables/useDevice'

const { isMobile } = useDevice()
const loading = ref(false)
const treeData = ref<MenuNode[]>([])
const keyword = reactive({ menuName: '' })

const dialogVisible = ref(false)
const current = ref<MenuNode | null>(null)
const parentNode = ref<MenuNode | null>(null)

function menuTypeLabel(t: string) {
  return findDict(MENU_TYPE_DICT, t as MenuType)?.label || t
}
function menuTypeTag(t: string) {
  return findDict(MENU_TYPE_DICT, t as MenuType)?.tagType || 'info'
}
function ownerLabel(t?: string) {
  return findDict(MENU_OWNER_DICT, t)?.label || '-'
}

/**
 * 系统菜单（'00'）为内置保护数据，前后端均不可变更，故不提供操作入口。
 * 确需调整时由开发人员直接操作数据库；后端 updateObj 有同样的校验兜底。
 */
function isSysMenu(row: MenuNode): boolean {
  return row.sysMenuType === SYS_MENU_TYPE.SYS_DEF
}

const toolbarActions: Record<string, MenuAction<MenuNode>> = {
  'menu:save:post': {
    handler: onAddRoot,
    icon: Plus,
    type: 'success'
  }
}

const rowActions: Record<string, MenuAction<MenuNode>> = {
  'menu:save:post': { handler: onAddChild, type: 'primary' },
  'menu:update:post': { handler: onEdit, type: 'primary' },
  'menu:delete:delete': { handler: onDelete, type: 'danger' }
}

async function fetchTree() {
  loading.value = true
  try {
    const data = await menuTreePage(keyword.menuName ? { menuName: keyword.menuName } : {})
    treeData.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
}

function onSearch() {
  fetchTree()
}
function onReset() {
  keyword.menuName = ''
  fetchTree()
}
function onAddRoot() {
  current.value = null
  parentNode.value = null
  dialogVisible.value = true
}
function onAddChild(row: MenuNode) {
  current.value = null
  parentNode.value = row
  dialogVisible.value = true
}
function onEdit(row: MenuNode) {
  current.value = row
  parentNode.value = null
  dialogVisible.value = true
}

async function onDelete(row: MenuNode) {
  try {
    await ElMessageBox.confirm(
      `确定删除菜单「${row.menuName}」吗？存在子菜单时需要先删除子菜单。`,
      '删除菜单',
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
    await removeMenu(row.id)
    ElMessage.success('已删除')
    await fetchTree()
  } catch {
    // 错误提示（含「存在子菜单」的拒绝原因）已由拦截器处理
    await fetchTree()
  }
}

onMounted(fetchTree)
</script>

<style lang="scss" scoped>
.filter-card {
  margin-bottom: 12px;

  :deep(.el-card__body) {
    padding-bottom: 2px;
  }
}

.row-icon {
  vertical-align: middle;
  margin-right: 4px;
}

.sys-menu-tip {
  color: var(--app-text-placeholder);
}

// H5 表格横滑
.table-card {
  :deep(.el-table) {
    width: 100% !important;
  }
}
</style>
