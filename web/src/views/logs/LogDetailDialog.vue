<template>
  <el-dialog
    :model-value="modelValue"
    title="日志详情"
    class="app-responsive-dialog"
    :width="isMobile ? '94vw' : '880px'"
    :close-on-click-modal="false"
    append-to-body
    @update:model-value="(v: boolean) => emit('update:modelValue', v)"
  >
    <template v-if="record">
      <el-descriptions :column="isMobile ? 1 : 2" border size="small">
        <el-descriptions-item label="操作时间">{{ fmtTime(record.operateTime) }}</el-descriptions-item>
        <el-descriptions-item label="执行结果">
          <el-tag :type="typeTag(record.type)" size="small">{{ typeLabel(record.type) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户">{{ userText }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ moduleLabel(record.module) }}</el-descriptions-item>
        <el-descriptions-item label="操作">{{ operateLabel(record.operate) }}</el-descriptions-item>
        <el-descriptions-item label="耗时">
          {{ record.consumingTime === null || record.consumingTime === undefined ? '-' : `${record.consumingTime} ms` }}
        </el-descriptions-item>
        <el-descriptions-item label="请求 IP">{{ record.requestIp || '-' }}</el-descriptions-item>
        <el-descriptions-item label="IP 归属地" :span="isMobile ? 1 : 2">
          {{ joinAddress(record.ipAddress) }}
        </el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ record.httpMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="接口描述">{{ record.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请求地址" :span="isMobile ? 1 : 2">
          <span class="mono">{{ record.requestUri || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="类名" :span="isMobile ? 1 : 2">
          <span class="mono">{{ record.classPath || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="方法名">{{ record.actionMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="浏览器标识" :span="isMobile ? 1 : 2">
          <span class="mono">{{ record.ua || '-' }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 长文本单独成块，避免撑破 descriptions 的表格布局 -->
      <div
        v-for="b in blocks"
        :key="b.title"
        class="block"
        :class="{ 'is-danger': b.danger }"
      >
        <div class="block-head">
          <span class="block-title">{{ b.title }}</span>
          <el-button link type="primary" size="small" @click="copy(b.text)">复制</el-button>
        </div>
        <pre class="block-body">{{ b.text }}</pre>
      </div>
    </template>

    <template #footer>
      <el-button type="primary" @click="emit('update:modelValue', false)">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { LogRecord } from '@/api/types/log'
import { LOG_MODULE_DICT, LOG_OPERATE_DICT, LOG_TYPE_DICT, findDict } from '@/constants/dict'
import { fmtTime, joinAddress } from '@/utils/format'
import { useDevice } from '@/composables/useDevice'

const props = defineProps<{
  modelValue: boolean
  record?: LogRecord | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const { isMobile } = useDevice()

const userText = computed(() => {
  const r = props.record
  if (!r) return '-'
  if (r.nickName && r.userName) return `${r.nickName}（${r.userName}）`
  return r.nickName || r.userName || (r.userId ? `#${r.userId}` : '-')
})

/** 长文本区块；异常相关仅在存在时展示 */
const blocks = computed(() => {
  const r = props.record
  if (!r) return [] as { title: string; text: string; danger?: boolean }[]
  const list: { title: string; text: string; danger?: boolean }[] = [
    { title: '日志内容', text: r.content || '-' },
    { title: '请求参数', text: r.params || '-' },
    { title: '响应结果', text: r.result || '-' }
  ]
  if (r.exDesc) list.push({ title: '异常描述', text: r.exDesc, danger: true })
  if (r.exDetail) list.push({ title: '异常堆栈', text: r.exDetail, danger: true })
  return list
})

function typeLabel(v?: string) {
  return findDict(LOG_TYPE_DICT, v)?.label || v || '-'
}
function typeTag(v?: string) {
  return findDict(LOG_TYPE_DICT, v)?.tagType || 'info'
}
/** 字典命中则显示中文，未命中直接回显原始 code（后端 cn 开关切换时会走到这里） */
function moduleLabel(v?: string) {
  return findDict(LOG_MODULE_DICT, v)?.label || v || '-'
}
function operateLabel(v?: string) {
  return findDict(LOG_OPERATE_DICT, v)?.label || v || '-'
}

async function copy(text: string) {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch {
    ElMessage.warning('当前浏览器不支持自动复制，请手动选中复制')
  }
}
</script>

<style lang="scss" scoped>
.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
  word-break: break-all;
}

.block {
  margin-top: 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  overflow: hidden;

  &.is-danger {
    border-color: var(--el-color-danger-light-5);

    .block-head {
      background-color: var(--el-color-danger-light-9);
    }
  }
}

.block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 10px;
  background-color: var(--el-fill-color-light);
}

.block-title {
  font-size: 13px;
  font-weight: 500;
}

.block-body {
  margin: 0;
  padding: 10px;
  max-height: 220px;
  overflow: auto;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  background-color: var(--el-fill-color-blank);
}
</style>
