<template>
  <PageContainer title="系统配置" subtitle="按配置表单分组；删除为逻辑删除">
    <el-card shadow="never" class="table-card" v-loading="table.loading.value">
      <el-tabs v-model="activeTab" v-if="groups.length">
        <el-tab-pane v-for="g in groups" :key="g.id" :name="g.id">
          <template #label>
            <span class="tab-label">{{ g.name }} <em>({{ g.list?.length || 0 }})</em></span>
          </template>

          <div class="group-toolbar">
            <MenuButtons size="small" :actions="groupActions(g)" />
          </div>

          <el-table :data="g.list || []" border size="small">
            <el-table-column prop="name" label="名称" min-width="130" show-overflow-tooltip />
            <el-table-column v-if="!isMobile" prop="code" label="编码" min-width="140" show-overflow-tooltip />
            <el-table-column prop="value" label="配置值" min-width="200" show-overflow-tooltip />
            <el-table-column v-if="!isMobile" prop="remarks" label="备注" min-width="120" show-overflow-tooltip />
            <el-table-column label="操作" :width="isMobile ? 100 : 130" align="center" fixed="right">
              <template #default="{ row }">
                <MenuButtons
                  link
                  size="small"
                  :row="row as ConfigItem"
                  :actions="rowActions(g)"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <el-empty v-else-if="!table.loading.value" description="暂无配置分组" />

      <el-pagination
        class="pager"
        :current-page="table.query.currentPage"
        :page-size="table.query.pageSize"
        :total="table.total.value"
        :page-sizes="[10, 20, 50]"
        :small="isMobile"
        :layout="isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
        @current-change="onPageChange"
        @size-change="table.handleSizeChange"
      />
    </el-card>

    <ConfigItemDialog
      v-model="dialogVisible"
      :group="currentGroup"
      :item="currentItem"
      @saved="table.fetchList"
    />
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import PageContainer from '@/components/PageContainer/index.vue'
import MenuButtons from '@/components/MenuButtons/index.vue'
import type { MenuAction } from '@/components/MenuButtons/types'
import ConfigItemDialog from './ConfigItemDialog.vue'
import { configPage, removeConfig } from '@/api/config'
import type { ConfigGroup, ConfigItem, ConfigPageQuery } from '@/api/types/config'
import { useTable } from '@/composables/useTable'
import { useDevice } from '@/composables/useDevice'

const { isMobile } = useDevice()

const table = useTable<ConfigPageQuery, ConfigGroup>({
  fetcher: configPage,
  initialQuery: { currentPage: 1, pageSize: 10 },
  pageSize: 10
})

const groups = computed<ConfigGroup[]>(() => table.records.value ?? [])
const activeTab = ref('')
const dialogVisible = ref(false)
const currentGroup = ref<ConfigGroup | null>(null)
const currentItem = ref<ConfigItem | null>(null)

// 每次翻页后默认选中第一个分组
watch(
  groups,
  (list) => {
    if (list.length && !list.some((g) => g.id === activeTab.value)) {
      activeTab.value = list[0].id
    }
  },
  { immediate: true }
)

function onPageChange(p: number) {
  table.handlePageChange(p)
}

/** 每个分组 tab 独立的新增按钮 */
function groupActions(group: ConfigGroup): Record<string, MenuAction<ConfigItem>> {
  return {
    'sysConfig:save:post': {
      handler: () => onAdd(group),
      icon: Plus,
      type: 'success'
    }
  }
}

/** 行内编辑 / 删除需要携带当前分组 */
function rowActions(group: ConfigGroup): Record<string, MenuAction<ConfigItem>> {
  return {
    'sysConfig:update:post': {
      handler: (row) => onEdit(group, row),
      type: 'primary'
    },
    'sysConfig:delete:delete': {
      handler: (row) => onRemove(row),
      type: 'danger'
    }
  }
}

function onAdd(group: ConfigGroup) {
  currentGroup.value = group
  currentItem.value = null
  dialogVisible.value = true
}
function onEdit(group: ConfigGroup, item: ConfigItem) {
  currentGroup.value = group
  currentItem.value = item
  dialogVisible.value = true
}
async function onRemove(item: ConfigItem) {
  try {
    await ElMessageBox.confirm(`确定删除配置项「${item.name}」吗？`, '删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  await removeConfig(item.id)
  ElMessage.success('已删除')
  await table.fetchList()
}

onMounted(table.fetchList)
</script>

<style lang="scss" scoped>
.tab-label {
  font-size: 13px;

  em {
    font-style: normal;
    color: var(--app-text-secondary);
  }
}

.group-toolbar {
  margin-bottom: 10px;
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
