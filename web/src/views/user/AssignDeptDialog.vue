<template>
  <el-dialog
    :model-value="modelValue"
    :title="`分配部门 - ${user?.nickName || user?.userName || ''}`"
    class="app-responsive-dialog"
    :width="isMobile ? '94vw' : '460px'"
    :close-on-click-modal="false"
    append-to-body
    @update:model-value="(v: boolean) => emit('update:modelValue', v)"
    @open="onOpen"
  >
    <div v-loading="loading" class="dept-box">
      <el-tree
        ref="treeRef"
        :data="deptTree"
        node-key="id"
        :props="{ label: 'name', children: 'children' }"
        highlight-current
        :expand-on-click-node="false"
        default-expand-all
        empty-text="暂无部门"
        @node-click="onNodeClick"
      />
    </div>
    <div class="selected-tip">
      当前选择：{{ selectedName || '未选择（保存后将解除绑定）' }}
    </div>

    <template #footer>
      <el-button @click="onClear">清除选择</el-button>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { nextTick, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { ElTree } from 'element-plus'
import { getBindDept, bindDept } from '@/api/user'
import { getDeptTree } from '@/api/dept'
import type { DeptNode } from '@/api/types/dept'
import type { UserRecord } from '@/api/types/user'
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
const deptTree = ref<DeptNode[]>([])
const selectedId = ref('')
const selectedName = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()

async function onOpen() {
  if (!props.user) return
  loading.value = true
  selectedId.value = ''
  selectedName.value = ''
  try {
    // 同时拉取部门树与该用户已绑部门
    const [tree, bound] = await Promise.all([
      getDeptTree(),
      getBindDept(props.user.id)
    ])
    deptTree.value = Array.isArray(tree) ? tree : []
    if (bound) {
      selectedId.value = String(bound)
      // 等树渲染完成后再回显高亮，并记录名称
      await nextTick()
      treeRef.value?.setCurrentKey(selectedId.value)
      selectedName.value = findDeptName(deptTree.value, selectedId.value)
    }
  } finally {
    loading.value = false
  }
}

/** 在树中按 id 查找部门名（回显提示用） */
function findDeptName(nodes: DeptNode[], id: string): string {
  for (const n of nodes) {
    if (n.id === id) return n.name
    if (n.children?.length) {
      const hit = findDeptName(n.children, id)
      if (hit) return hit
    }
  }
  return ''
}

function onNodeClick(data: DeptNode) {
  selectedId.value = data.id
  selectedName.value = data.name
}

function onClear() {
  selectedId.value = ''
  selectedName.value = ''
  treeRef.value?.setCurrentKey(undefined)
}

async function onSubmit() {
  if (!props.user) return
  saving.value = true
  try {
    // deptId 传空 = 解除绑定
    await bindDept({ userId: props.user.id, deptId: selectedId.value || null })
    ElMessage.success(selectedId.value ? '部门分配成功' : '已解除部门绑定')
    emit('saved')
    emit('update:modelValue', false)
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.dept-box {
  min-height: 160px;
  max-height: 340px;
  overflow: auto;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  padding: 8px 12px;
}

.selected-tip {
  margin-top: 8px;
  font-size: 12px;
  color: var(--app-text-secondary);
}
</style>
