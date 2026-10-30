import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from './store'
import { setupRouterGuard } from './router/guard'
import { setupHasPermiDirective } from './directives/hasPermi'
import { i18n } from './i18n'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// Element Plus：组件 JS 按需（unplugin），样式全量引入以保证
// ElMessage / ElMessageBox / ElLoading 等命令式 API 弹层样式完整
import 'element-plus/dist/index.css'
// 深色模式变量（由 html.dark 触发）
import 'element-plus/theme-chalk/dark/css-vars.css'
import 'nprogress/nprogress.css'
import '@/styles/index.scss'

const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(i18n)

// 全量注册 Element Plus 图标（菜单 icon 存的是图标名字符串，需全局可解析）
for (const [iconName, iconComponent] of Object.entries(ElementPlusIconsVue)) {
  app.component(iconName, iconComponent)
}

setupHasPermiDirective(app)
setupRouterGuard()

app.mount('#app')
