import { reactive, ref } from 'vue'
import type { PageResult } from '@/api/types/common'

interface UseTableOptions<Q, R> {
  /** 真正发请求的函数，入参为合并后的查询体，返回归一化分页结果 */
  fetcher: (query: Q) => Promise<PageResult<R>>
  /** 初始查询参数（不含分页字段） */
  initialQuery: Q
  /** 初始每页条数 */
  pageSize?: number
}

/**
 * 分页列表通用逻辑：loading / records / total / 分页与查询。
 * 调用方只需把 fetcher 接到具体 api（api 内部已做分页错位适配）。
 */
export function useTable<Q extends { currentPage?: number; pageSize?: number }, R>(
  options: UseTableOptions<Q, R>
) {
  const loading = ref(false)
  const records = ref<R[]>([]) as ReturnType<typeof ref<R[]>>
  const total = ref(0)

  const query = reactive({
    currentPage: 1,
    pageSize: options.pageSize ?? 10,
    ...options.initialQuery
  }) as Q & { currentPage: number; pageSize: number }

  async function fetchList() {
    loading.value = true
    try {
      const page = await options.fetcher(query)
      records.value = page.records
      total.value = page.total
      // 若当前页超出范围（删除后），回退一页
      if (page.current > 1 && records.value.length === 0) {
        query.currentPage = page.current - 1
        await fetchList()
      }
    } finally {
      loading.value = false
    }
  }

  function handleSearch() {
    query.currentPage = 1
    return fetchList()
  }

  function handleReset() {
    Object.assign(query, options.initialQuery, { currentPage: 1, pageSize: query.pageSize })
    return fetchList()
  }

  function handlePageChange(page: number) {
    query.currentPage = page
    return fetchList()
  }

  function handleSizeChange(size: number) {
    query.pageSize = size
    query.currentPage = 1
    return fetchList()
  }

  return {
    loading,
    records,
    total,
    query,
    fetchList,
    handleSearch,
    handleReset,
    handlePageChange,
    handleSizeChange
  }
}
