<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑岗位' : '新建岗位'"
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
      <el-form-item label="岗位名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入岗位名称" maxlength="20" />
      </el-form-item>
      <el-form-item label="岗位编码" prop="code">
        <el-input v-model="form.code" placeholder="如 manager（全局唯一）" maxlength="20" />
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
import { savePost, updatePost } from '@/api/post'
import type { PostRecord } from '@/api/types/post'
import { useDevice } from '@/composables/useDevice'

const props = defineProps<{
  modelValue: boolean
  /** 编辑时传入的岗位行 */
  record?: PostRecord | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  saved: []
}>()

const { isMobile } = useDevice()
const formRef = ref<FormInstance>()
const saving = ref(false)
const isEdit = computed(() => !!props.record)

interface PostFormState {
  id: string
  name: string
  code: string
}

const form = reactive<PostFormState>({
  id: '',
  name: '',
  code: ''
})

const rules = computed<FormRules>(() => {
  const r: FormRules = {
    name: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
    code: [
      { required: true, message: '请输入岗位编码', trigger: 'blur' },
      {
        pattern: /^[A-Za-z][A-Za-z0-9_]{1,19}$/,
        message: '以英文字母开头，只能包含英文字符、数字和下划线，长度2-20位',
        trigger: 'blur'
      }
    ]
  }
  return r
})

/**
 * 生成新建岗位时的默认编码：post_ + 时间戳 base36 后 4 位 + 3 位随机。
 * 仅是预填值，用户可改；全局唯一由后端 queryPostByCode 查重兜底。
 */
function generatePostCode(): string {
  const stamp = Date.now().toString(36).slice(-4)
  const rand = Math.random().toString(36).slice(2, 5).padEnd(3, '0')
  return `post_${stamp}${rand}`
}

function onOpen() {
  formRef.value?.clearValidate()
  if (props.record) {
    const n = props.record
    form.id = n.id
    form.name = n.name
    form.code = n.code || ''
  } else {
    Object.assign(form, {
      id: '',
      name: '',
      code: generatePostCode()
    })
  }
}

async function onSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (isEdit.value) {
      await updatePost({ id: form.id, name: form.name.trim(), code: form.code.trim() })
    } else {
      await savePost({
        name: form.name.trim(),
        code: form.code.trim()
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
</style>
