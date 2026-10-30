<template>
  <div v-loading="loading">
    <el-row :gutter="12">
      <el-col :xs="12" :sm="8" :md="4" v-for="card in statCards" :key="card.label">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="panel-card">
      <template #header><span>命令调用统计（Top 12）</span></template>
      <div v-for="c in topCommands" :key="c.name" class="cmd-row">
        <span class="cmd-name">{{ c.name }}</span>
        <div class="cmd-bar-wrap">
          <div class="cmd-bar" :style="{ width: barWidth(c.value) }" />
        </div>
        <span class="cmd-value">{{ c.value }}</span>
      </div>
      <el-empty v-if="!topCommands.length" description="暂无命令统计" :image-size="60" />
    </el-card>

    <el-collapse class="panel-card">
      <el-collapse-item title="Redis INFO 原始信息" name="info">
        <KeyValueTree :data="info" />
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getCacheInfo } from '@/api/monitor'
import KeyValueTree from './KeyValueTree.vue'

const loading = ref(false)
const info = ref<Record<string, string>>({})
const dbSize = ref(0)
const commandStats = ref<Array<{ name: string; value: string }>>([])

const statCards = computed(() => [
  { label: 'Key 总数', value: dbSize.value },
  { label: 'Redis 版本', value: info.value.redis_version || '-' },
  { label: '已连接客户端', value: info.value.connected_clients || '-' },
  { label: '已用内存', value: info.value.used_memory_human || '-' },
  { label: '运行天数', value: info.value.uptime_in_days ?? '-' },
  { label: '运行角色', value: info.value.role || '-' }
])

const topCommands = computed(() =>
  [...commandStats.value]
    .map((c) => ({ name: c.name, value: Number(c.value) || 0 }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 12)
)

const maxValue = computed(() => Math.max(1, ...topCommands.value.map((c) => c.value)))

function barWidth(v: number): string {
  return `${Math.max(2, Math.round((v / maxValue.value) * 100))}%`
}

async function load() {
  loading.value = true
  try {
    const data = await getCacheInfo()
    info.value = data.info || {}
    dbSize.value = Number(data.dbSize) || 0
    commandStats.value = data.commandStats || []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style lang="scss" scoped>
.stat-card {
  margin-bottom: 12px;
  text-align: center;

  :deep(.el-card__body) {
    padding: 14px 8px;
  }
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: var(--app-color-primary);
}

.stat-label {
  margin-top: 4px;
  font-size: 12px;
  color: var(--app-text-secondary);
}

.panel-card {
  margin-bottom: 12px;
}

.cmd-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 6px 0;
}

.cmd-name {
  width: 90px;
  flex-shrink: 0;
  font-size: 12px;
  text-transform: uppercase;
  color: var(--app-text-regular);
}

.cmd-bar-wrap {
  flex: 1;
  background-color: var(--el-fill-color-light);
  border-radius: 3px;
  height: 14px;
  overflow: hidden;
}

.cmd-bar {
  height: 100%;
  background: linear-gradient(90deg, #1d6fff, #6ea8ff);
  border-radius: 3px;
  transition: width 0.4s ease;
}

.cmd-value {
  width: 70px;
  text-align: right;
  font-size: 12px;
  color: var(--app-text-secondary);
  flex-shrink: 0;
}
</style>
