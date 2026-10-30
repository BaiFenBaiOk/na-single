import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN'
import en from './en'

export type AppLocale = 'zh-CN' | 'en'

/** 从持久化的 app store 中读取语言，保证刷新后保持 */
function initialLocale(): AppLocale {
  try {
    const raw = localStorage.getItem('na-app')
    if (raw) {
      const parsed = JSON.parse(raw) as { language?: string }
      if (parsed.language === 'en') return 'en'
    }
  } catch {
    // 持久化数据异常时使用默认语言
  }
  return 'zh-CN'
}

export const i18n = createI18n({
  legacy: false,
  locale: initialLocale(),
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    en
  }
})

/** 切换语言（同步 i18n 与 Element Plus） */
export function setLocale(locale: AppLocale) {
  i18n.global.locale.value = locale
}
