<template>
  <el-drawer
    v-model="visible"
    direction="ltr"
    :size="260"
    :with-header="false"
    class="mobile-drawer"
  >
    <Sidebar force-expand />
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from './Sidebar.vue'
import { useAppStore } from '@/store/modules/app'

const route = useRoute()
const appStore = useAppStore()

const visible = computed({
  get: () => appStore.drawerVisible,
  set: (val: boolean) => {
    if (val) appStore.openDrawer()
    else appStore.closeDrawer()
  }
})

// 路由切换后自动收起抽屉
watch(
  () => route.path,
  () => appStore.closeDrawer()
)
</script>

<style lang="scss">
// 抽屉内容区去掉默认内边距，让深色侧栏铺满
.mobile-drawer.el-drawer {
  padding: 0;

  .el-drawer__body {
    padding: 0;
    background-color: #001529;
  }
}
</style>
