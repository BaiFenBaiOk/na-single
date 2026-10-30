<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑部门' : '新建部门'"
    class="app-responsive-dialog"
    :width="isMobile ? '94vw' : '520px'"
    :close-on-click-modal="false"
    append-to-body
    @update:model-value="(v: boolean) => emit('update:modelValue', v)"
    @open="onOpen"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      :label-width="isMobile ? '88px' : '100px'"
      :label-position="isMobile ? 'top' : 'right'"
    >
      <el-form-item label="上级部门">
        <el-tree-select
          v-model="form.parentId"
          :data="parentOptions"
          :props="{ label: 'name', children: 'children' }"
          node-key="id"
          value-key="id"
          check-strictly
          :disabled="isAddChild"
          :render-after-expand="false"
          placeholder="请选择上级部门"
          class="full-width"
        />
        <div class="field-tip">顶级部门直接选择「顶级部门」；上级不能选自己或自己的下级（后端会校验）</div>
      </el-form-item>
      <el-form-item label="部门名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入部门名称" maxlength="20" />
      </el-form-item>
      <el-form-item label="显示顺序">
        <el-input-number v-model="form.orderNum" :min="0" :max="9999" controls-position="right" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" placeholder="选填" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { saveDept, updateDept } from '@/api/dept'
import type { DeptNode } from '@/api/types/dept'
import { useDevice } from '@/composables/useDevice'

const props = defineProps<{
  modelValue: boolean
  /** 编辑时传入的部门行 */
  record?: DeptNode | null
  /** 新建下级时预选的上级部门 */
  parent?: DeptNode | null
  /** 部门树（上级选择用） */
  tree?: DeptNode[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  saved: []
}>()

const { isMobile } = useDevice()
const formRef = ref<FormInstance>()
const saving = ref(false)
const isEdit = computed(() => !!props.record)

interface DeptFormState {
  id: string
  parentId: string
  name: string
  orderNum: number
  remark: string
}

const form = reactive<DeptFormState>({
  id: '',
  parentId: '',
  name: '',
  orderNum: 9999,
  remark: ''
})

/** 新增下级（预选了上级部门）：上级部门锁定为父部门，避免挂错层级 */
const isAddChild = computed(() => !isEdit.value && !!props.parent)

/**
 * 上级选择数据：顶层补一个虚拟「顶级部门」节点（id='0'）。
 * 既让顶级状态可读（显示「顶级部门」而不是 0），也允许把部门移回顶级。
 */
const parentOptions = computed<DeptNode[]>(() => [
  { id: '0', parentId: '0', name: '顶级部门', children: props.tree || [] }
])

const rules = computed<FormRules>(() => ({
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}))

function onOpen() {
  formRef.value?.clearValidate()
  if (props.record) {
    const n = props.record
    form.id = n.id
    // 顶级部门的 parentId 为 0/'0'/空，统一归一为 '0'，对应虚拟「顶级部门」节点
    const pid = n.parentId
    form.parentId = pid === undefined || pid === null || String(pid) === '0' ? '0' : String(pid)
    form.name = n.name
    form.orderNum = Number(n.orderNum) || 0
    form.remark = n.remark || ''
  } else {
    Object.assign(form, {
      id: '',
      // 新增下级预选上级（且上方已锁定）；否则默认「顶级部门」
      parentId: props.parent?.id || '0',
      name: '',
      orderNum: 9999,
      remark: ''
    })
  }
}

async function onSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    // parentId 空串表示顶级部门，统一转为 '0'
    const parentId = form.parentId || '0'
    if (isEdit.value) {
      await updateDept({
        id: form.id,
        parentId,
        name: form.name.trim(),
        orderNum: form.orderNum,
        remark: form.remark
      })
    } else {
      await saveDept({
        parentId,
        name: form.name.trim(),
        orderNum: form.orderNum,
        remark: form.remark
      })
    }
    emit('saved')
    emit('update:modelValue', false)
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.full-width {
  width: 100%;
}

.field-tip {
  font-size: 12px;
  color: var(--app-text-secondary);
  line-height: 1.4;
}
</style>
