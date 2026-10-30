<template>
  <PageContainer title="部门管理" subtitle="部门树；删除部门前需无子部门且无用户绑定">
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" class="filter-form" @submit.prevent>
        <el-form-item class="filter-actions">
          <el-button :icon="RefreshLeft" @click="fetchTree">刷新</el-button>
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
        <el-table-column prop="name" label="部门名称" min-width="180" show-overflow-tooltip />
        <el-table-column v-if="!isMobile" prop="orderNum" label="排序" width="70" align="center" />
        <el-table-column v-if="!isMobile" prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column v-if="!isMobile" label="创建时间" min-width="150">
          <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" :width="isMobile ? 150 : 210" align="center" fixed="right">
          <template #default="{ row }">
            <MenuButtons
              link
              size="small"
              :row="row as DeptNode"
              :actions="rowActions"
            />
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && treeData.length === 0" :description="emptyText" :image-size="80" />
    </el-card>

    <DeptFormDialog
      v-model="dialogVisible"
      :record="current"
      :parent="parentNode"
      :tree="treeData"
      @saved="fetchTree"
    />
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, RefreshLeft } from '@element-plus/icons-vue'
import PageContainer from '@/components/PageContainer/index.vue'
import MenuButtons from '@/components/MenuButtons/index.vue'
import type { MenuAction } from '@/components/MenuButtons/types'
import DeptFormDialog from './DeptFormDialog.vue'
import { getDeptTree, removeDept } from '@/api/dept'
import type { DeptNode } from '@/api/types/dept'
import { fmtTime } from '@/utils/format'
import { useDevice } from '@/composables/useDevice'

const { isMobile } = useDevice()

const loading = ref(false)
const treeData = ref<DeptNode[]>([])

const dialogVisible = ref(false)
const current = ref<DeptNode | null>(null)
const parentNode = ref<DeptNode | null>(null)

const toolbarActions: Record<string, MenuAction<DeptNode>> = {
  'dept:save:post': {
    handler: onAddRoot,
    icon: Plus,
    type: 'success'
  }
}

const rowActions: Record<string, MenuAction<DeptNode>> = {
  'dept:save:post': {
    handler: onAddChild,
    type: 'primary'
  },
  'dept:update:post': { handler: onEdit, type: 'primary' },
  'dept:delete:delete': { handler: onDelete, type: 'danger' }
}

const emptyText = computed(() => '暂无部门')

async function fetchTree() {
  loading.value = true
  try {
    const data = await getDeptTree()
    treeData.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
}

function onAddRoot() {
  current.value = null
  parentNode.value = null
  dialogVisible.value = true
}

function onAddChild(row: DeptNode) {
  current.value = null
  parentNode.value = row
  dialogVisible.value = true
}

function onEdit(row: DeptNode) {
  current.value = row
  parentNode.value = null
  dialogVisible.value = true
}

async function onDelete(row: DeptNode) {
  try {
    await ElMessageBox.confirm(`确定删除部门「${row.name}」吗？`, '删除部门', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await removeDept(row.id)
    ElMessage.success('已删除')
    await fetchTree()
  } catch {
    // 错误提示（含「存在子部门 / 有用户绑定」的拒绝原因）已由拦截器处理
  }
}

onMounted(() => {
  fetchTree()
})
</script>

<style lang="scss" scoped>
.filter-card {
  margin-bottom: 12px;

  :deep(.el-card__body) {
    padding-bottom: 2px;
  }
}
</style>
