<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑角色' : '新建角色'"
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
      <el-form-item label="角色名称" prop="roleName">
        <el-input v-model="form.roleName" placeholder="请输入角色名称" maxlength="32" />
      </el-form-item>
      <el-form-item label="角色编码" prop="roleCode">
        <el-input
          v-model="form.roleCode"
          :disabled="isEdit"
          placeholder="如 manager（全局唯一）"
          maxlength="64"
        />
      </el-form-item>
      <el-form-item label="数据范围" prop="dataScope">
        <el-select v-model="form.dataScope" :disabled="isEdit" class="full-width">
          <el-option
            v-for="d in availableScopeOptions"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          >
            <el-tag :type="d.tagType" size="small">{{ d.label }}</el-tag>
          </el-option>
        </el-select>
        <div v-if="isEdit" class="field-tip">数据范围在创建后不支持修改</div>
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number
          v-model="form.roleSort"
          :min="0"
          :max="9999"
          :disabled="isEdit"
          controls-position="right"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-switch
          v-model="form.status"
          :active-value="false"
          :inactive-value="true"
          active-text="正常"
          inactive-text="停用"
        />
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
import { saveRole, updateRole } from '@/api/role'
import type { RoleRecord } from '@/api/types/role'
import { generateId } from '@/utils/generateId'
import { DATA_SCOPE_DICT } from '@/constants/dict'
import { DATA_SCOPE, SYS_USER_TYPE } from '@/constants/businessCode'
import { useUserStore } from '@/store/modules/user'
import { useDevice } from '@/composables/useDevice'

const props = defineProps<{
  modelValue: boolean
  record?: RoleRecord | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  saved: []
}>()

const { isMobile } = useDevice()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const saving = ref(false)
const isEdit = computed(() => !!props.record)

interface RoleFormState {
  id: string
  roleName: string
  roleCode: string
  roleSort: number
  dataScope: string
  /** false 正常 / true 停用 */
  status: boolean
}

const form = reactive<RoleFormState>({
  id: '',
  roleName: '',
  roleCode: '',
  roleSort: 0,
  dataScope: DATA_SCOPE.CUSTOM, // 默认自定义（2），绝不能默认 1
  status: false
})

// 普通用户隐藏「全部数据权限」（data_scope=1 会拿到全量菜单）
const availableScopeOptions = computed(() =>
  DATA_SCOPE_DICT.filter((d) => userStore.isSysUser || !d.requireSysUser)
)

const rules = computed<FormRules>(() => {
  const r: FormRules = {
    roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
    roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
    dataScope: [{ required: true, message: '请选择数据范围', trigger: 'change' }]
  }
  return r
})

/**
 * 生成新建角色时的默认编码：role_ + 时间戳 base36 后 4 位 + 3 位随机。
 * 仅是预填值，用户可改；全局唯一由后端 queryRoleByRoleObj 查重兜底。
 */
function generateRoleCode(): string {
  const stamp = Date.now().toString(36).slice(-4)
  const rand = Math.random().toString(36).slice(2, 5).padEnd(3, '0')
  return `role_${stamp}${rand}`
}

function onOpen() {
  formRef.value?.clearValidate()
  if (props.record) {
    const n = props.record
    form.id = n.id
    form.roleName = n.roleName
    form.roleCode = n.roleCode
    form.roleSort = Number(n.roleSort) || 0
    form.dataScope = n.dataScope || DATA_SCOPE.CUSTOM
    form.status = !!n.status
  } else {
    Object.assign(form, {
      id: '',
      roleName: '',
      // 预填一个英文编码，用户可自行修改
      roleCode: generateRoleCode(),
      roleSort: 0,
      dataScope: DATA_SCOPE.CUSTOM,
      status: false
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
      // 后端 update 仅消费 roleName / status（roleCode/dataScope/roleType/roleSort 均不更新）
      await updateRole({ id: form.id, roleName: form.roleName, status: form.status })
    } else {
      await saveRole({
        id: generateId(),
        roleName: form.roleName,
        roleCode: form.roleCode.trim(),
        roleSort: form.roleSort,
        dataScope: form.dataScope,
        status: form.status,
        // 角色类型固定为普通（03），无需选择
        roleType: SYS_USER_TYPE.ORDINARY
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
