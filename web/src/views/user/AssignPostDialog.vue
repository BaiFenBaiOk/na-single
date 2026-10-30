<template>
  <el-dialog
    :model-value="modelValue"
    :title="`分配岗位 - ${user?.nickName || user?.userName || ''}`"
    class="app-responsive-dialog"
    :width="isMobile ? '94vw' : '460px'"
    :close-on-click-modal="false"
    append-to-body
    @update:model-value="(v: boolean) => emit('update:modelValue', v)"
    @open="onOpen"
  >
    <div v-loading="loading" class="post-box">
      <el-checkbox-group v-model="checkedPosts">
        <el-scrollbar max-height="320px">
          <el-checkbox
            v-for="p in postOptions"
            :key="p.id"
            :value="p.id"
            class="post-item"
          >
            {{ p.name }}
            <span class="post-code">（{{ p.code }}）</span>
          </el-checkbox>
          <el-empty v-if="!loading && postOptions.length === 0" description="暂无岗位" :image-size="60" />
        </el-scrollbar>
      </el-checkbox-group>
    </div>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { bindPosts, getBindPosts } from '@/api/user'
import { postPage } from '@/api/post'
import type { PostRecord } from '@/api/types/post'
import type { UserRecord } from '@/api/types/user'
import type { IdStr } from '@/api/types/common'
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
const postOptions = ref<PostRecord[]>([])
const checkedPosts = ref<IdStr[]>([])

async function onOpen() {
  if (!props.user) return
  loading.value = true
  checkedPosts.value = []
  try {
    // 同时拉取岗位列表与该用户已绑岗位
    const [posts, bound] = await Promise.all([
      postPage({ currentPage: 1, pageSize: 200 }),
      getBindPosts(props.user.id)
    ])
    postOptions.value = posts.records
    checkedPosts.value = Array.isArray(bound) ? bound.map((v) => String(v)) : []
  } finally {
    loading.value = false
  }
}

async function onSubmit() {
  if (!props.user) return
  saving.value = true
  try {
    // 空数组 = 取消该用户的全部岗位
    await bindPosts({ userId: props.user.id, postIds: checkedPosts.value })
    ElMessage.success(checkedPosts.value.length ? '岗位分配成功' : '已取消全部岗位')
    emit('saved')
    emit('update:modelValue', false)
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.post-box {
  min-height: 120px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  padding: 8px 12px;
}

.post-item {
  display: flex;
  width: 100%;
  margin: 6px 0;
}

.post-code {
  color: var(--app-text-secondary);
  font-size: 12px;
}
</style>
