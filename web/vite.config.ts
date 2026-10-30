import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { fileURLToPath, URL } from 'node:url'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    // 自动导入 Vue / Vue Router / Pinia / VueUse 的 API
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia', '@vueuse/core'],
      resolvers: [ElementPlusResolver()],
      dts: 'src/types/auto-imports.d.ts',
      eslintrc: { enabled: false }
    }),
    // 自动按需注册 Element Plus 组件与 src/components 下的组件
    Components({
      resolvers: [ElementPlusResolver()],
      dirs: ['src/components'],
      dts: 'src/types/components.d.ts'
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        // 仅注入 SCSS 变量 / mixin，禁止在此文件写实体 CSS（会被重复注入每个编译单元）
        additionalData: `@use "@/styles/variables.scss" as *;`
      }
    }
  },
  server: {
    host: '0.0.0.0', // 监听全部网卡，便于手机真机联调 H5
    port: 5173,
    proxy: {
      // 单体后端直连：/api/na-upms/** 原样转发到 10061
      '/api/na-upms': {
        target: 'http://localhost:10061',
        changeOrigin: true
      },
      // WebSocket 与本地文件 URL 共用 /na-upms 前缀：
      //   /na-upms/ws        -> 后端 /ws
      //   /na-upms/video/**  -> 后端 /video/**
      '/na-upms': {
        target: 'http://localhost:10061',
        changeOrigin: true,
        ws: true,
        rewrite: (path) => path.replace(/^\/na-upms/, '')
      }
    }
  },
  build: {
    chunkSizeWarningLimit: 1500,
    rollupOptions: {
      output: {
        manualChunks: {
          vue: ['vue', 'vue-router', 'pinia'],
          'element-plus': ['element-plus', '@element-plus/icons-vue']
        }
      }
    }
  }
})
