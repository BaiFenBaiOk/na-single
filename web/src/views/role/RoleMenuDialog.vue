<template>
  <TreeAssignDialog
    :model-value="modelValue"
    :title="`角色菜单授权 - ${role?.roleName || ''}`"
    :fetch-echo="fetchEcho"
    :on-save="onSave"
    allow-empty
    @update:model-value="(v: boolean) => emit('update:modelValue', v)"
  />
</template>

<script setup lang="ts">
import TreeAssignDialog from '@/components/TreeAssignDialog/index.vue'
import { roleMenuEcho, saveRoleMenu } from '@/api/menu'
import type { IdStr } from '@/api/types/common'
import type { RoleRecord } from '@/api/types/role'

const props = defineProps<{
  modelValue: boolean
  role?: RoleRecord | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const fetchEcho = () =>
  roleMenuEcho({ roleId: props.role?.id || '' })

// 后端校验：角色必须存在，菜单必须存在且未删除
const onSave = (menuIds: IdStr[]) =>
  saveRoleMenu({ roleId: props.role?.id || '', menuIds })
</script>
