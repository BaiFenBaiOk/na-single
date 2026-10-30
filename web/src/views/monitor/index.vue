<template>
  <PageContainer title="系统监控" subtitle="Redis 缓存与服务器运行状态">
    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="缓存监控" name="cache">
          <CachePanel v-if="activeTab === 'cache'" />
        </el-tab-pane>
        <el-tab-pane label="服务器监控" name="server">
          <ServerPanel v-if="activeTab === 'server'" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PageContainer from '@/components/PageContainer/index.vue'
import CachePanel from './CachePanel.vue'
import ServerPanel from './ServerPanel.vue'

// 服务器信息采集较慢，切到该页签时才挂载加载
const activeTab = ref('cache')
</script>
