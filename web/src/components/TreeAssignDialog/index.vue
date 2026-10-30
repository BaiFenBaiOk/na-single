<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    class="app-responsive-dialog"
    :width="isMobile ? '94vw' : '520px'"
    :close-on-click-modal="false"
    append-to-body
    @update:model-value="(v: boolean) => emit('update:modelValue', v)"
    @open="loadEcho"
  >
    <div class="assign-toolbar">
      <div class="assign-toolbar__left">
        <el-button link type="primary" size="small" @click="onSelectAll">全选</el-button>
        <el-divider direction="vertical" />
        <el-button link type="primary" size="small" @click="onInvert">反选</el-button>
      </div>
      <div class="assign-toolbar__right">
        <span class="assign-toolbar__label">父子联动</span>
        <el-tooltip :content="linkageTip" placement="top">
          <el-switch v-model="linkage" size="small" @change="onLinkageChange" />
        </el-tooltip>
      </div>
    </div>
    <div class="assign-hint">{{ linkageTip }}</div>

    <div v-loading="loading" class="tree-wrap">
      <el-tree
        ref="treeRef"
        :data="tree"
        show-checkbox
        node-key="id"
        :props="treeProps"
        :check-strictly="!linkage"
        :default-expanded-keys="expandedKeys"
        :expand-on-click-node="false"
        class="assign-tree"
      >
        <template #default="{ data }">
          <span class="tree-node" :class="{ 'is-virtual': virtualIds.has(String(data.id)) }">
            <span>{{ data.menuName }}</span>
            <el-tag size="small" :type="menuTypeTag(data.menuType)" class="tree-node__tag">
              {{ menuTypeLabel(data.menuType) }}
            </el-tag>
            <!-- 按钮 / 接口节点展示权限标识，便于授权时核对 -->
            <span v-if="data.perms" class="tree-node__perms">{{ data.perms }}</span>
          </span>
        </template>
      </el-tree>
      <el-empty v-if="!loading && tree.length === 0" description="暂无可授权菜单" :image-size="70" />
    </div>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onConfirm">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { TreeInstance, TreeNodeData } from 'element-plus'
import type { MenuAssignEcho, MenuNode, MenuType } from '@/api/types/menu'
import type { IdStr } from '@/api/types/common'
import { flattenTree, leafCheckedFromChains, leafCheckedFromIds, uniq } from '@/utils/tree'
import { MENU_TYPE_DICT, findDict } from '@/constants/dict'
import { useDevice } from '@/composables/useDevice'

const props = defineProps<{
  modelValue: boolean
  title: string
  /** 拉取回显数据：{ menuTree, menuIds?, menuIdsArr? }，优先消费扁平的 menuIds */
  fetchEcho: () => Promise<MenuAssignEcho>
  /** 保存回调，入参为「勾选 + 半选」合并后的完整 id 链 */
  onSave: (menuIds: IdStr[]) => Promise<unknown>
  /** 是否允许清空 */
  allowEmpty?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const { isMobile } = useDevice()
const treeRef = ref<TreeInstance>()
const loading = ref(false)
const saving = ref(false)
const tree = ref<MenuNode[]>([])
const expandedKeys = ref<IdStr[]>([])
/**
 * 仅用于补全层级的祖先节点 id——打开弹窗时快照一次，之后不再变化。
 *
 * 为什么不直接用节点数据里的 disabled 字段：el-tree 的节点 disabled 是 getter，
 * 每次渲染都从 node.data 重新读取，而 props.data 的 watch 是 deep 模式，
 * 联动切换时其内部会反复重置勾选状态并重建节点，该属性存在失效的情况。
 * 改用「前端快照 + 函数式 props」可彻底摆脱对 data 属性的运行时依赖。
 */
const virtualIds = ref<Set<IdStr>>(new Set())

// disabled 用函数形式直接读快照集合
const treeProps = {
  label: 'menuName',
  children: 'children',
  disabled: (data: TreeNodeData) => virtualIds.value.has(String(data.id))
}

/**
 * 父子联动：开启时勾选父级会带上全部子级（适合整块授权）；
 * 关闭后父级与子级各自独立，所见即所得——授权了什么就只显示勾选什么。
 * 默认关闭，避免只授权子项时父级被连带显示为半选。
 */
const linkage = ref(false)
const linkageTip = computed(() =>
  linkage.value
    ? '已开启：勾选父级会同时选中其下全部子项（提交含父级目录）'
    : '已关闭：精确勾选，选中哪个就只提交哪个'
)

/** 可勾选的全部节点 id（排除仅为补全层级而展示的祖先节点） */
const selectableIds = computed<IdStr[]>(() =>
  flattenTree(tree.value)
    .filter((n) => !virtualIds.value.has(n.id))
    .map((n) => n.id)
)

/** 当前已选（勾选 + 半选）集合，剔除仅为补全层级的祖先节点 */
function currentSelected(): IdStr[] {
  const checked = (treeRef.value?.getCheckedKeys() || []) as IdStr[]
  const half = (treeRef.value?.getHalfCheckedKeys() || []) as IdStr[]
  return uniq([...checked, ...half]).filter((id) => !virtualIds.value.has(id))
}

/** 全选：所有可勾选节点一并授权 */
function onSelectAll() {
  treeRef.value?.setCheckedKeys(selectableIds.value)
}

/** 反选：已选（含半选）之外的可勾选节点变为选中 */
function onInvert() {
  const selected = new Set(currentSelected())
  treeRef.value?.setCheckedKeys(selectableIds.value.filter((id) => !selected.has(id)))
}

/**
 * 切换父子联动：保留当前授权范围。
 *
 * 回填时必须做「末端过滤」——若把父级一并回填，联动模式下父节点被设为勾选
 * 会级联展开其整棵子树，反复切换会让范围越滚越大直至全选。
 * 只回填末端节点，父级在联动模式下由 el-tree 自动推导半选状态。
 */
function onLinkageChange() {
  const keep = leafCheckedFromIds(tree.value, currentSelected())
  // 等 el-tree 应用新的 check-strictly 后再回填
  nextTick(() => treeRef.value?.setCheckedKeys(keep))
}

function menuTypeLabel(t: string) {
  return findDict(MENU_TYPE_DICT, t as MenuType)?.label || t
}
function menuTypeTag(t: string) {
  return findDict(MENU_TYPE_DICT, t as MenuType)?.tagType || 'info'
}

/**
 * 打开时拉取回显。每次都重置为独立（不联动）模式，
 * 此时直接勾选后端返回的授权集合即可精确还原——存了什么就勾什么。
 */
async function loadEcho() {
  loading.value = true
  try {
    const echo = await props.fetchEcho()
    tree.value = echo.menuTree || []
    // 快照不可勾选的祖先节点 id，供 treeProps.disabled 判定（只取这一次）
    virtualIds.value = new Set(
      flattenTree(tree.value)
        .filter((n) => n.disabled)
        .map((n) => n.id)
    )
    linkage.value = false // 每次打开重置为精确勾选模式（所见即所得）
    // 默认展开根下一层，方便查看
    expandedKeys.value = tree.value.map((n) => n.id)
    await nextTick()
    // 回显：扁平 menuIds 即精确的授权集合，直接勾选（所见即所得）；
    // 若接口只给 id 链（角色授权），链内含祖先路径，取其末端 = 实际存储的节点。
    // 补全层级的祖先节点不参与勾选，避免"显示已勾选但保存时被剔除"的错觉。
    const checkedIds = (
      Array.isArray(echo.menuIds) ? echo.menuIds : leafCheckedFromChains(echo.menuIdsArr || [])
    ).filter((id) => !virtualIds.value.has(id))
    treeRef.value?.setCheckedKeys(checkedIds)
  } finally {
    loading.value = false
  }
}

async function onConfirm() {
  // 仅为补全层级的祖先不参与提交，否则会超出授权范围被后端校验拦下
  const checked = ((treeRef.value?.getCheckedKeys() || []) as IdStr[]).filter(
    (id) => !virtualIds.value.has(id)
  )
  // 联动模式下父级处于半选状态，需一并提交才能保留完整父链；
  // 独立（不联动）模式下严格按勾选提交——选中哪个传哪个，不额外带上父级目录。
  const halfChecked = linkage.value
    ? ((treeRef.value?.getHalfCheckedKeys() || []) as IdStr[]).filter(
        (id) => !virtualIds.value.has(id)
      )
    : []
  const menuIds = uniq([...checked, ...halfChecked])

  if (!props.allowEmpty && menuIds.length === 0) {
    ElMessage.warning('请至少勾选一个菜单')
    return
  }
  saving.value = true
  try {
    await props.onSave(menuIds)
    ElMessage.success('授权保存成功')
    emit('update:modelValue', false)
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.assign-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 4px;
}

.assign-toolbar__left {
  display: flex;
  align-items: center;
}

.assign-toolbar__right {
  display: flex;
  align-items: center;
  gap: 6px;
}

.assign-toolbar__label {
  font-size: 12px;
  color: var(--app-text-regular);
}

.assign-hint {
  font-size: 12px;
  color: var(--app-text-secondary);
  line-height: 1.5;
  margin-bottom: 8px;
}

.tree-wrap {
  min-height: 200px;
  max-height: 50vh;
  overflow-y: auto;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  padding: 8px;
}

.tree-node {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.tree-node__tag {
  transform: scale(0.85);
}

/**
 * 仅用于补全层级的祖先节点：复选框始终保持空白外观。
 *
 * 用 :has() 从节点内容反查复选框，而不是依赖 el-checkbox 的 is-disabled
 * ——后者由 el-tree 在节点重建过程中动态求值，切换联动的瞬间可能短暂失效，
 * 造成勾选标记闪现。这里只看我们自己打的稳定标记，不会闪。
 */
.assign-tree :deep(.el-tree-node__content:has(.is-virtual)) .el-checkbox .el-checkbox__inner {
  background-color: var(--el-fill-color-blank) !important;
  border-color: var(--el-border-color) !important;

  &::after,
  &::before {
    display: none !important;
  }
}

.tree-node__perms {
  font-size: 11px;
  color: var(--app-text-secondary);
  background-color: var(--el-fill-color-light);
  padding: 1px 4px;
  border-radius: 3px;
  @include ellipsis;
  max-width: 40vw;
}
</style>
