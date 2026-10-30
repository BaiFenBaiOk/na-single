<template>
  <div class="assistant-entry" :class="`is-${type}`">
    <el-tooltip :content="title" placement="left">
      <button type="button" class="assistant-button" @click="visible = true">
        <el-icon :size="22"><component :is="icon" /></el-icon>
      </button>
    </el-tooltip>

    <el-drawer v-model="visible" :title="title" size="360px" append-to-body>
      <div class="assistant-panel">
        <el-empty :description="`${title}尚未接入服务`" :image-size="80" />
        <p class="assistant-tip">
          当前为功能入口占位。接入模型服务后，可在此展示对话窗口、快捷指令和业务问答能力。
        </p>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
/**
 * AI 助手 / 超级助手入口（占位）。
 * 项目尚未接入 AI 服务，这里只提供开关控制下的悬浮入口与说明面板。
 */
import { computed, ref } from 'vue'
import { ChatDotRound, MagicStick } from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{ type?: 'ai' | 'super' }>(), { type: 'ai' })

const visible = ref(false)
const title = computed(() => (props.type === 'super' ? '超级助手' : 'AI 助手'))
const icon = computed(() => (props.type === 'super' ? MagicStick : ChatDotRound))
</script>

<style lang="scss" scoped>
.assistant-entry {
  position: fixed;
  right: 22px;
  bottom: 80px;
  z-index: 2500;

  &.is-super {
    bottom: 140px;
  }
}

.assistant-button {
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 50%;
  background: var(--app-color-primary);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.2);
  transition: transform 0.15s;

  &:hover {
    transform: translateY(-2px);
  }
}

.assistant-panel {
  text-align: center;
}

.assistant-tip {
  font-size: 12px;
  line-height: 1.7;
  color: var(--app-text-secondary);
}
</style>
