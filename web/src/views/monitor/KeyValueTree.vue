<template>
  <div class="kvt">
    <template v-for="[k, v] in viewEntries" :key="k">
      <div v-if="isPrimitive(v)" class="kvt-row" :class="{ indent: depth > 0 }">
        <span class="kvt-k">{{ k }}</span>
        <span class="kvt-v">{{ display(v) }}</span>
      </div>
      <div v-else class="kvt-node">
        <div class="kvt-k kvt-group">{{ k }}</div>
        <KeyValueTree :data="v" :depth="depth + 1" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{ data: unknown; depth?: number }>(), { depth: 0 })

function isPrimitive(v: unknown): boolean {
  return v === null || v === undefined || typeof v !== 'object'
}

function display(v: unknown): string {
  if (v === null || v === undefined || v === '') return '-'
  return String(v)
}

// 数组转为带 [下标] 键的对象，统一走对象递归
const viewEntries = computed<Array<[string, unknown]>>(() => {
  const d = props.data
  if (Array.isArray(d)) {
    return d.map((v, i) => [`[${i}]`, v] as [string, unknown])
  }
  if (d && typeof d === 'object') {
    return Object.entries(d as Record<string, unknown>)
  }
  return []
})
</script>

<style lang="scss" scoped>
.kvt {
  font-size: 13px;
}

.kvt-node {
  margin: 2px 0;
}

.kvt-row {
  display: flex;
  gap: 10px;
  padding: 3px 0;
  border-bottom: 1px dashed var(--el-border-color-lighter);
}

.kvt-k {
  color: var(--app-text-secondary);
  min-width: 180px;
  flex-shrink: 0;
  word-break: break-all;
}

.kvt-v {
  color: var(--app-text-primary);
  word-break: break-all;
}

.kvt-group {
  font-weight: 600;
  color: var(--app-color-primary);
  margin-top: 6px;
}

:deep(.kvt) {
  padding-left: 14px;
  border-left: 2px solid var(--el-border-color-lighter);
}

@media (max-width: 767.98px) {
  .kvt-row {
    flex-direction: column;
    gap: 2px;
  }
  .kvt-k {
    min-width: 0;
  }
}
</style>
