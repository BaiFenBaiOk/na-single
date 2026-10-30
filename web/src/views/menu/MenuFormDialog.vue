<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑菜单' : '新增菜单'"
    class="app-responsive-dialog"
    :width="isMobile ? '94vw' : '640px'"
    :close-on-click-modal="false"
    append-to-body
    @update:model-value="(v: boolean) => emit('update:modelValue', v)"
    @open="onOpen"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      :label-width="isMobile ? '84px' : '96px'"
      :label-position="isMobile ? 'top' : 'right'"
    >
      <el-form-item label="上级菜单">
        <el-tree-select
          v-model="form.parentId"
          :data="parentOptions"
          :props="{ label: 'menuName', children: 'children' }"
          node-key="id"
          value-key="id"
          check-strictly
          :disabled="isEdit"
          :render-after-expand="false"
          placeholder="根目录"
          class="full-width"
        />
      </el-form-item>

      <el-form-item label="菜单类型" prop="menuType">
        <el-radio-group v-model="form.menuType" :disabled="isEdit">
          <el-radio-button
            v-for="d in menuTypeOptions"
            :key="d.value"
            :value="d.value"
            :disabled="d.disabled"
          >
            {{ d.label }}
          </el-radio-button>
        </el-radio-group>
        <div v-if="childTypeHint" class="field-tip">{{ childTypeHint }}</div>
      </el-form-item>

      <el-form-item label="菜单名称" prop="menuName">
        <el-input v-model="form.menuName" placeholder="请输入菜单名称" maxlength="32" />
      </el-form-item>

      <!-- 目录 / 菜单：路由与图标 -->
      <template v-if="form.menuType === 'M' || form.menuType === 'C'">
        <el-form-item label="路由地址" prop="path">
          <el-input v-model="form.path" placeholder="如 /userList（根目录也需填写）" />
        </el-form-item>
        <el-form-item v-if="form.menuType === 'C'" label="组件路径" prop="component">
          <el-input v-model="form.component" placeholder="如 /user/list 或 /Dashboard" />
        </el-form-item>
        <el-form-item label="路由参数">
          <el-input v-model="form.query" placeholder="可选，如 id=1&type=a" />
        </el-form-item>
        <el-form-item label="图标">
          <el-select
            v-model="form.icon"
            filterable
            allow-create
            default-first-option
            clearable
            placeholder="选择或输入 Element Plus 图标名"
            class="full-width"
          >
            <el-option v-for="n in ICON_OPTIONS" :key="n" :label="n" :value="n">
              <el-icon class="menu-icon-option"><component :is="n" /></el-icon>
              <span>{{ n }}</span>
            </el-option>
          </el-select>
        </el-form-item>
      </template>

      <!-- 按钮 / 接口：请求地址 + 权限标识 -->
      <template v-else>
        <el-form-item label="请求地址" prop="requestUri">
          <el-input v-model="form.requestUri" placeholder="如 /api/sysmenu/save" />
        </el-form-item>
        <el-form-item label="权限标识" prop="perms">
          <el-input v-model="form.perms" placeholder="如 menu:save:post" />
        </el-form-item>
      </template>

      <el-row :gutter="12">
        <el-col :xs="12" :sm="8">
          <el-form-item label="排序" label-width="84px">
            <el-input-number v-model="form.orderNum" :min="0" :max="9999" controls-position="right" />
          </el-form-item>
        </el-col>
        <el-col :xs="12" :sm="8">
          <el-form-item label="显示" label-width="70px">
            <el-select v-model="form.visible">
              <el-option v-for="d in VISIBLE_DICT" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="8">
          <el-form-item label="状态" label-width="70px">
            <el-select v-model="form.status">
              <el-option v-for="d in MENU_STATUS_DICT" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row v-if="form.menuType === 'C'" :gutter="12">
        <el-col :xs="12" :sm="12">
          <el-form-item label="是否外链">
            <el-radio-group v-model="form.isFrame">
              <el-radio v-for="d in IS_FRAME_DICT" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :xs="12" :sm="12">
          <el-form-item label="是否缓存">
            <el-radio-group v-model="form.isCache">
              <el-radio v-for="d in IS_CACHE_DICT" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

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
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { saveMenu, updateMenu } from '@/api/menu'
import type { MenuNode, MenuType } from '@/api/types/menu'
import { generateId } from '@/utils/generateId'
import { flattenTree } from '@/utils/tree'
import {
  MENU_TYPE_DICT,
  MENU_STATUS_DICT,
  VISIBLE_DICT,
  IS_FRAME_DICT,
  IS_CACHE_DICT
} from '@/constants/dict'
import { useDevice } from '@/composables/useDevice'

const props = defineProps<{
  modelValue: boolean
  /** 全量菜单树（用于选择上级） */
  tree: MenuNode[]
  /** 编辑时传入的节点 */
  record?: MenuNode | null
  /** 新增子项时传入的父节点；新增根为 null */
  parent?: MenuNode | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  saved: []
}>()

const { isMobile } = useDevice()
const formRef = ref<FormInstance>()
const saving = ref(false)

const isEdit = computed(() => !!props.record)

// 常用图标（存名字符串，侧边栏用 <component :is> 解析，图标已全局注册）
const ICON_OPTIONS = [
  'Menu', 'Setting', 'User', 'Lock', 'Monitor', 'House', 'Folder', 'Document',
  'DataLine', 'Grid', 'Tools', 'OfficeBuilding', 'Avatar', 'Key', 'List',
  'Connection', 'Coin', 'Histogram', 'Bell', 'Operation'
]

interface MenuForm {
  id: string
  parentId: string
  menuType: string
  menuName: string
  orderNum: number
  path: string
  component: string
  query: string
  requestUri: string
  perms: string
  icon: string
  isFrame: number
  isCache: number
  visible: string
  status: string
  remark: string
}

function emptyForm(): MenuForm {
  return {
    id: '',
    parentId: '0',
    // 默认「目录」：与新增根菜单场景匹配（后端要求目录的上级必须为 0）
    menuType: 'M',
    menuName: '',
    orderNum: 0,
    path: '',
    component: '',
    query: '',
    requestUri: '',
    perms: '',
    icon: '',
    isFrame: 1,
    isCache: 0,
    visible: '0',
    status: '0',
    remark: ''
  }
}

const form = reactive<MenuForm>(emptyForm())

/**
 * 各上级类型下允许创建的子类型（接口不受限，见 allowedTypesOf）。
 * 与后端校验保持一致：目录下可继续建目录或菜单，其余逐级向下、不可跨级。
 */
const ALLOWED_CHILD_TYPES: Record<string, MenuType[]> = {
  M: ['C', 'M'],
  C: ['F']
}

/**
 * 依据上级节点推导可用的子类型。
 * 接口（A）不受层级限制——挂在目录 / 菜单 / 按钮下均可；但接口下不能再建任何子级。
 */
function allowedTypesOf(parentId: string): MenuType[] {
  if (!parentId || parentId === '0') return ['M'] // 根级只能建目录
  const parent = flattenTree(props.tree).find((n) => n.id === parentId)
  if (parent?.menuType === 'A') return [] // 接口下不可再建
  const base = (parent?.menuType && ALLOWED_CHILD_TYPES[parent.menuType]) || []
  return [...new Set<MenuType>([...base, 'A'])]
}

/** 上级选项中剔除接口（A）节点——它们不能再挂子级 */
function pruneApiNodes(nodes: MenuNode[]): MenuNode[] {
  return nodes
    .filter((n) => n.menuType !== 'A')
    .map((n) => ({
      ...n,
      children: n.children?.length ? pruneApiNodes(n.children) : undefined
    }))
}

// 上级选项：在树顶部补一个虚拟「根目录」
const parentOptions = computed<MenuNode[]>(() => [
  { id: '0', menuName: '根目录', menuType: 'M' } as MenuNode,
  ...pruneApiNodes(props.tree)
])

/** 当前上级下允许的类型集合 */
const allowedTypes = computed(() => allowedTypesOf(form.parentId))

/**
 * 类型选项：四个选项始终一致展示，当前上级不允许的置为 disabled。
 * 对照关系——根目录:目录；目录:菜单、目录；菜单:按钮；按钮:接口。
 */
const menuTypeOptions = computed(() =>
  MENU_TYPE_DICT.map((d) => ({
    ...d,
    disabled: allowedTypes.value.length > 0 && !allowedTypes.value.includes(d.value as MenuType)
  }))
)

const childTypeHint = computed(() => {
  if (isEdit.value || !allowedTypes.value.length) return ''
  const labels = allowedTypes.value
    .map((t) => MENU_TYPE_DICT.find((d) => d.value === t)?.label)
    .join('、')
  return `${form.parentId === '0' ? '根目录' : '所选上级'}下只能创建「${labels}」`
})

// 上级变化时：当前类型若已不在允许范围内，自动切到第一个合法类型
watch(
  () => form.parentId,
  (id) => {
    if (isEdit.value) return
    const allowed = allowedTypesOf(id)
    if (allowed.length && !allowed.includes(form.menuType as MenuType)) {
      form.menuType = allowed[0]
    }
  }
)

const rules = computed<FormRules>(() => {
  const r: FormRules = {
    menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
    menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }]
  }
  if (form.menuType === 'M' || form.menuType === 'C') {
    r.path = [{ required: true, message: '请输入路由地址', trigger: 'blur' }]
  }
  if (form.menuType === 'C') {
    r.component = [{ required: true, message: '请输入组件路径', trigger: 'blur' }]
  }
  if (form.menuType === 'F' || form.menuType === 'A') {
    r.requestUri = [{ required: true, message: '请输入请求地址', trigger: 'blur' }]
    r.perms = [{ required: true, message: '请输入权限标识', trigger: 'blur' }]
  }
  return r
})

function onOpen() {
  Object.assign(form, emptyForm())
  if (props.record) {
    // 编辑：回填原节点
    const n = props.record
    Object.assign(form, {
      id: n.id,
      parentId: n.parentId || '0',
      menuType: n.menuType,
      menuName: n.menuName,
      orderNum: Number(n.orderNum) || 0,
      path: n.path || '',
      component: n.component || '',
      query: n.query || '',
      requestUri: n.requestUri || '',
      perms: n.perms || '',
      icon: n.icon || '',
      isFrame: Number(n.isFrame),
      isCache: Number(n.isCache),
      visible: n.visible ?? '0',
      status: n.status ?? '0',
      remark: n.remark || ''
    })
  } else if (props.parent) {
    // 新增子项：默认落到该上级允许的第一个类型（目录下默认「菜单」）
    form.parentId = props.parent.id
    form.menuType = allowedTypesOf(props.parent.id)[0] || 'C'
  }
}

async function onSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 双保险：非法类型已在选项上置灰，这里再校验一次防止其他路径绕过
  // （后端 validateMenuTypeChain 有同样的校验）
  if (!isEdit.value) {
    const allowed = allowedTypesOf(form.parentId)
    if (allowed.length && !allowed.includes(form.menuType as MenuType)) {
      ElMessage.warning(childTypeHint.value || '菜单类型与上级不匹配')
      return
    }
  }

  saving.value = true
  try {
    if (isEdit.value) {
      await updateMenu({ ...form })
    } else {
      // 新增：id 兜底（后端菜单 save 实际由雪花生成，传 id 仅过校验）
      await saveMenu({ ...form, id: generateId() })
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

.menu-icon-option {
  vertical-align: middle;
  margin-right: 6px;
}

.field-tip {
  font-size: 12px;
  color: var(--app-text-secondary);
  line-height: 1.4;
}
</style>
