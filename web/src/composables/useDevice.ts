import { useMediaQuery } from '@vueuse/core'
import type { ComputedRef } from 'vue'

/** 与 SCSS $breakpoint-mobile（768px）保持一致，用 767.98 避免断点重叠 */
export function useDevice(): { isMobile: ComputedRef<boolean> } {
  const isMobile = useMediaQuery('(max-width: 767.98px)')
  return { isMobile }
}
