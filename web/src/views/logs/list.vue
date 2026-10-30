<template>
  <PageContainer title="操作日志" subtitle="记录各接口的调用情况；异常请求会标红，点击「详情」查看参数与堆栈">
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" class="filter-form" @submit.prevent>
        <el-form-item label="用户名">
          <el-input
            v-model="table.query.userName"
            placeholder="账号"
            clearable
            style="width: 140px"
            @keyup.enter="table.handleSearch()"
          />
        </el-form-item>
        <!-- <el-form-item label="模块">
          <el-select
            v-model="table.query.model"
            placeholder="全部"
            clearable
            filterable
            allow-create
            default-first-option
            style="width: 150px"
          >
            <el-option v-for="d in LOG_MODULE_DICT" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作">
          <el-select
            v-model="table.query.operate"
            placeholder="全部"
            clearable
            filterable
            allow-create
            default-first-option
            style="width: 130px"
          >
            <el-option v-for="d in LOG_OPERATE_DICT" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item> -->
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            :shortcuts="TIME_SHORTCUTS"
            :value-format="TIME_FORMAT"
            :default-time="DEFAULT_TIME"
            style="width: 360px"
            @change="onTimeChange"
          />
        </el-form-item>
        <el-form-item label="耗时">
          <el-select v-model="table.query.consumingType" style="width: 108px">
            <el-option v-for="d in CONSUMING_TYPE_DICT" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
          <el-input-number
            v-model="table.query.consumingTime"
            :min="0"
            :controls="false"
            placeholder="毫秒"
            style="width: 96px; margin-left: 8px"
          />
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" :icon="Search" @click="table.handleSearch()">查询</el-button>
          <el-button :icon="RefreshLeft" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
      <!-- <div class="filter-tip">
        说明：模块 / 操作由后端按 code 精确匹配，下拉里的中文仅作提示；时间格式 yyyy-MM-dd HH:mm:ss。
      </div> -->
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="table.loading.value" :data="table.records.value" border size="small">
        <el-table-column prop="operateTime" label="操作时间" width="170">
          <template #default="{ row }">{{ fmtTime(row.operateTime) }}</template>
        </el-table-column>
        <el-table-column label="用户" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">{{ userText(row as LogRecord) }}</template>
        </el-table-column>
        <el-table-column v-if="!isMobile" label="模块" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ moduleLabel(row.module) }}</template>
        </el-table-column>
        <el-table-column v-if="!isMobile" label="操作" width="90" align="center">
          <template #default="{ row }">{{ operateLabel(row.operate) }}</template>
        </el-table-column>
        <el-table-column label="日志内容" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.content || '-' }}</template>
        </el-table-column>
        <el-table-column v-if="!isMobile" label="请求地址" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="uri-cell">{{ row.httpMethod }} {{ row.requestUri }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile" prop="requestIp" label="IP" width="130" show-overflow-tooltip />
        <el-table-column label="耗时" width="90" align="right">
          <template #default="{ row }">
            {{ row.consumingTime === null || row.consumingTime === undefined ? '-' : `${row.consumingTime} ms` }}
          </template>
        </el-table-column>
        <el-table-column label="结果" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.type)" size="small">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <MenuButtons
              link
              size="small"
              :row="row as LogRecord"
              :actions="rowActions"
            />
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        :current-page="table.query.currentPage"
        :page-size="table.query.pageSize"
        :total="table.total.value"
        :page-sizes="[10, 20, 50]"
        :small="isMobile"
        :layout="isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
        @current-change="table.handlePageChange"
        @size-change="table.handleSizeChange"
      />
    </el-card>

    <LogDetailDialog v-model="detailVisible" :record="current" />
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Search, RefreshLeft } from '@element-plus/icons-vue'
import PageContainer from '@/components/PageContainer/index.vue'
import MenuButtons from '@/components/MenuButtons/index.vue'
import type { MenuAction } from '@/components/MenuButtons/types'
import LogDetailDialog from './LogDetailDialog.vue'
import { logPage } from '@/api/log'
import type { LogPageQuery, LogRecord } from '@/api/types/log'
import { useTable } from '@/composables/useTable'
import {
  CONSUMING_TYPE_DICT,
  LOG_MODULE_DICT,
  LOG_OPERATE_DICT,
  LOG_TYPE_DICT,
  findDict
} from '@/constants/dict'
import { fmtTime } from '@/utils/format'
import { useDevice } from '@/composables/useDevice'

const { isMobile } = useDevice()

/** 必须与后端已注册的反序列化格式一致（JacksonModule 用 `yyyy-MM-dd HH:mm:ss`） */
const TIME_FORMAT = 'YYYY-MM-DD HH:mm:ss'
/** 只选日期时补全为整天的起止时刻，避免「当天 00:00:00 ~ 当天 00:00:00」查不到数据 */
const DEFAULT_TIME: [Date, Date] = [
  new Date(2000, 0, 1, 0, 0, 0),
  new Date(2000, 0, 1, 23, 59, 59)
]
const TIME_SHORTCUTS = [
  {
    text: '最近一小时',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 1)
      return [start, end]
    }
  },
  {
    text: '今天',
    value: () => {
      const start = new Date()
      start.setHours(0, 0, 0, 0)
      return [start, new Date()]
    }
  },
  {
    text: '最近七天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 7)
      return [start, end]
    }
  }
]

const table = useTable<LogPageQuery, LogRecord>({
  fetcher: logPage,
  initialQuery: {
    currentPage: 1,
    pageSize: 10,
    userName: '',
    model: '',
    operate: '',
    // 后端 consumingType 默认 1（大于），需与 consumingTime 同时有值才生效
    consumingType: 1,
    consumingTime: undefined,
    operateStartTime: '',
    operateEndTime: ''
  },
  pageSize: 10
})

/** 日期组件自身持有值；变更时同步进查询参数（日期组件不接受 query 里的空串） */
const timeRange = ref<string[] | null>(null)

function onTimeChange() {
  const v = timeRange.value
  table.query.operateStartTime = v?.[0] || ''
  table.query.operateEndTime = v?.[1] || ''
}

const detailVisible = ref(false)
const current = ref<LogRecord | null>(null)

const rowActions: Record<string, MenuAction<LogRecord>> = {
  'log:detail:get': { handler: onDetail, type: 'primary' }
}

function userText(row: LogRecord): string {
  if (row.nickName && row.userName) return `${row.nickName}（${row.userName}）`
  return row.nickName || row.userName || (row.userId ? `#${row.userId}` : '-')
}

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

function onDetail(row: LogRecord) {
  current.value = row
  detailVisible.value = true
}

function onReset() {
  timeRange.value = null
  return table.handleReset()
}

onMounted(() => {
  table.fetchList()
})
</script>

<style lang="scss" scoped>
.filter-card {
  margin-bottom: 12px;

  :deep(.el-card__body) {
    padding-bottom: 10px;
  }
}

.filter-tip {
  font-size: 12px;
  color: var(--app-text-secondary);
  margin: -4px 0 8px;
}

.uri-cell {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
}

.pager {
  margin-top: 12px;
  justify-content: flex-end;
}

@media (max-width: 767.98px) {
  .pager {
    justify-content: center;
  }
}
</style>
