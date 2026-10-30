<template>
  <PageContainer title="岗位管理" subtitle="岗位编码全局唯一；删除岗位前需先解除用户绑定">
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" class="filter-form" @submit.prevent>
        <el-form-item label="岗位名称">
          <el-input
            v-model="table.query.name"
            placeholder="名称关键字"
            clearable
            @keyup.enter="table.handleSearch()"
          />
        </el-form-item>
        <el-form-item label="编码">
          <el-input
            v-model="table.query.code"
            placeholder="编码关键字"
            clearable
            @keyup.enter="table.handleSearch()"
          />
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
        <el-table-column prop="name" label="岗位名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="code" label="岗位编码" min-width="140" show-overflow-tooltip />
        <el-table-column v-if="!isMobile" label="创建时间" min-width="150">
          <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" :width="isMobile ? 110 : 140" align="center" fixed="right">
          <template #default="{ row }">
            <MenuButtons
              link
              size="small"
              :row="row as PostRecord"
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

    <PostFormDialog
      v-model="dialogVisible"
      :record="current"
      @saved="table.fetchList"
    />
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshLeft, Plus } from '@element-plus/icons-vue'
import PageContainer from '@/components/PageContainer/index.vue'
import MenuButtons from '@/components/MenuButtons/index.vue'
import type { MenuAction } from '@/components/MenuButtons/types'
import PostFormDialog from './PostFormDialog.vue'
import { postPage, removePost } from '@/api/post'
import type { PostPageQuery, PostRecord } from '@/api/types/post'
import { useTable } from '@/composables/useTable'
import { fmtTime } from '@/utils/format'
import { useDevice } from '@/composables/useDevice'

const { isMobile } = useDevice()

const table = useTable<PostPageQuery, PostRecord>({
  fetcher: postPage,
  initialQuery: { currentPage: 1, pageSize: 10, name: '', code: '' },
  pageSize: 10
})

const dialogVisible = ref(false)
const current = ref<PostRecord | null>(null)

const toolbarActions: Record<string, MenuAction<PostRecord>> = {
  'post:save:post': {
    handler: onAdd,
    icon: Plus,
    type: 'success'
  }
}

const rowActions: Record<string, MenuAction<PostRecord>> = {
  'post:update:post': { handler: onEdit, type: 'primary' },
  'post:delete:delete': { handler: onDelete, type: 'danger' }
}

function onAdd() {
  current.value = null
  dialogVisible.value = true
}

function onEdit(row: PostRecord) {
  current.value = row
  dialogVisible.value = true
}

async function onDelete(row: PostRecord) {
  try {
    await ElMessageBox.confirm(`确定删除岗位「${row.name}」吗？`, '删除岗位', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await removePost(row.id)
    ElMessage.success('已删除')
    await table.fetchList()
  } catch {
    // 错误提示（含有用户绑定的拒绝原因）已由拦截器处理
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
