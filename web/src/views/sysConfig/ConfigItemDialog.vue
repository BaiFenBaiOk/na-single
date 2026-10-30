<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑配置项' : '新增配置项'"
    class="app-responsive-dialog"
    :width="isMobile ? '94vw' : '560px'"
    :close-on-click-modal="false"
    append-to-body
    @update:model-value="(v: boolean) => emit('update:modelValue', v)"
    @open="onOpen"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      :label-width="isMobile ? '84px' : '92px'"
      :label-position="isMobile ? 'top' : 'right'"
    >
      <el-form-item v-if="!isEdit" label="所属表单">
        <el-input :model-value="groupName" disabled />
      </el-form-item>
      <el-form-item v-if="!isEdit" label="配置名称" prop="name">
        <el-input v-model="form.name" maxlength="64" />
      </el-form-item>
      <el-form-item v-if="!isEdit" label="配置编码" prop="code">
        <el-input v-model="form.code" placeholder="全局唯一" maxlength="128" />
      </el-form-item>
      <el-form-item label="配置值" prop="value">
        <el-input v-model="form.value" type="textarea" :rows="5" placeholder="可能含长文本 / 密钥，请谨慎填写" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remarks" type="textarea" :rows="2" maxlength="200" />
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
import { saveConfig, updateConfig } from '@/api/config'
import type { ConfigGroup, ConfigItem } from '@/api/types/config'
import { useDevice } from '@/composables/useDevice'

const props = defineProps<{
  modelValue: boolean
  /** 新增时所属分组 */
  group?: ConfigGroup | null
  /** 编辑时的配置项 */
  item?: ConfigItem | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  saved: []
}>()

const { isMobile } = useDevice()
const formRef = ref<FormInstance>()
const saving = ref(false)
// 编辑以是否传入配置项判断（新建传分组）
const isEdit = computed(() => !!props.item)

const form = reactive({
  name: '',
  code: '',
  value: '',
  remarks: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入配置编码', trigger: 'blur' }],
  value: [{ required: true, message: '请输入配置值', trigger: 'blur' }]
}

/** 新增时的所属表单名：必须用 computed，props.group 是打开弹窗时才赋值的 */
const groupName = computed(() => props.group?.name || '')

function onOpen() {
  formRef.value?.clearValidate()
  if (props.item) {
    form.name = props.item.name
    form.code = props.item.code
    form.value = props.item.value || ''
    form.remarks = props.item.remarks || ''
  } else {
    Object.assign(form, { name: '', code: '', value: '', remarks: '' })
  }
}

async function onSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (props.item) {
      // 编辑：后端仅消费 id / value / remarks
      await updateConfig({ id: props.item.id, value: form.value, remarks: form.remarks })
    } else if (props.group) {
      await saveConfig({
        formId: props.group.id,
        name: form.name,
        code: form.code,
        value: form.value,
        remarks: form.remarks
      })
    }
    emit('saved')
    emit('update:modelValue', false)
  } finally {
    saving.value = false
  }
}
</script>
