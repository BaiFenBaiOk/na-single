<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑用户' : '新建用户'"
    class="app-responsive-dialog"
    :width="isMobile ? '94vw' : '600px'"
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
      <el-row :gutter="12">
        <el-col :xs="24" :sm="12">
          <el-form-item label="账号" prop="userName">
            <el-input
              v-model="form.userName"
              :disabled="isEdit"
              placeholder="字母或数字，全局唯一"
              maxlength="32"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item label="昵称" prop="nickName">
            <el-input v-model="form.nickName" maxlength="32" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item :label="isEdit ? '重置密码' : '密码'" :prop="isEdit ? undefined : 'passWord'">
        <el-input
          v-model="form.passWord"
          type="password"
          show-password
          :placeholder="isEdit ? '留空表示不修改密码' : '字母、数字或符号 !@#$%^&*()_+-='"
          autocomplete="new-password"
        />
      </el-form-item>

      <el-row :gutter="12">
        <el-col :xs="24" :sm="12">
          <el-form-item label="性别">
            <el-select v-model="form.sex" class="full-width">
              <el-option v-for="d in SEX_DICT" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="12">
        <el-col :xs="24" :sm="12">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" maxlength="11" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" maxlength="64" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="头像地址">
        <el-input v-model="form.avatar" placeholder="图片 URL（后端暂无上传接口）" maxlength="255" />
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

      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" />
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
import type { FormInstance, FormItemRule, FormRules } from 'element-plus'
import { saveUser, updateUser } from '@/api/user'
import type { UserRecord, UserSavePayload } from '@/api/types/user'
import { generateId } from '@/utils/generateId'
import { isAlphaNumeric, isValidPassword, isValidPhone, isValidEmail } from '@/utils/validate'
import { SEX_DICT } from '@/constants/dict'
import { SYS_USER_TYPE } from '@/constants/businessCode'
import { useDevice } from '@/composables/useDevice'

const props = defineProps<{
  modelValue: boolean
  record?: UserRecord | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  saved: []
}>()

const { isMobile } = useDevice()
const formRef = ref<FormInstance>()
const saving = ref(false)
const isEdit = computed(() => !!props.record)

interface UserFormState {
  userName: string
  nickName: string
  passWord: string
  sex: number
  phone: string
  email: string
  avatar: string
  /** 账号状态：false 正常 / true 停用 */
  status: boolean
  remark: string
}

const form = reactive<UserFormState>({
  userName: '',
  nickName: '',
  passWord: '',
  sex: 0,
  phone: '',
  email: '',
  avatar: '',
  status: false,
  remark: ''
})

const rules = computed<FormRules>(() => {
  const r: FormRules = {
    userName: [
      { required: true, message: '请输入账号', trigger: 'blur' },
      {
        validator: (_rule: FormItemRule, value: string, cb: (e?: Error) => void) => {
          if (value && isAlphaNumeric(value)) cb()
          else cb(new Error('账号只能包含字母或数字'))
        },
        trigger: 'blur'
      }
    ],
    nickName: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
    phone: [
      {
        validator: (_rule, value: string, cb) => {
          if (isValidPhone(value)) cb()
          else cb(new Error('手机号格式不正确'))
        },
        trigger: 'blur'
      }
    ],
    email: [
      {
        validator: (_rule, value: string, cb) => {
          if (isValidEmail(value)) cb()
          else cb(new Error('邮箱格式不正确'))
        },
        trigger: 'blur'
      }
    ]
  }
  // 新建时密码必填；编辑时留空即不修改
  if (!isEdit.value) {
    r.passWord = [
      { required: true, message: '请输入密码', trigger: 'blur' },
      {
        validator: (_rule, value: string, cb) => {
          if (value && isValidPassword(value)) cb()
          else cb(new Error('密码只能包含字母、数字或符号 !@#$%^&*()_+-='))
        },
        trigger: 'blur'
      }
    ]
  }
  return r
})

function onOpen() {
  formRef.value?.clearValidate()
  if (props.record) {
    const n = props.record
    form.userName = n.userName
    form.nickName = n.nickName || ''
    form.passWord = '' // 回填必须留空，禁止展示明文
    form.sex = Number(n.sex) || 0
    form.phone = n.phone || ''
    form.email = n.email || ''
    form.avatar = n.avatar || ''
    form.status = !!n.status
    form.remark = n.remark || ''
  } else {
    Object.assign(form, {
      userName: '',
      nickName: '',
      passWord: '',
      sex: 0,
      phone: '',
      email: '',
      avatar: '',
      status: false,
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
    if (isEdit.value && props.record) {
      const payload: Partial<UserSavePayload> & { id: string } = {
        id: props.record.id,
        userName: form.userName,
        nickName: form.nickName,
        sex: form.sex,
        phone: form.phone,
        email: form.email,
        avatar: form.avatar,
        status: form.status,
        remark: form.remark
      }
      // 关键：密码留空则不传该字段，避免把空串写成新密码
      if (form.passWord) payload.passWord = form.passWord
      await updateUser(payload)
    } else {
      await saveUser({
        id: generateId(),
        userName: form.userName.trim(),
        nickName: form.nickName.trim(),
        passWord: form.passWord,
        // 用户类型固定为普通（03），无需选择
        userType: SYS_USER_TYPE.ORDINARY,
        sex: form.sex,
        phone: form.phone,
        email: form.email,
        avatar: form.avatar,
        status: form.status,
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
</style>
