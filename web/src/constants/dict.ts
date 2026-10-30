// 业务字典：value + label + tagType，供 el-select / el-tag 共用
import { DATA_SCOPE, SYS_MENU_TYPE, SYS_USER_TYPE } from './businessCode'

export interface DictItem<V = string> {
  value: V
  label: string
  /** el-tag 类型 */
  tagType?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
}

/**
 * 启停状态（用户 / 角色通用）。
 * 后端统一用 Boolean 承载数据库的 0/1——false 正常 / true 停用，与直觉相反，勿混淆。
 */
export const STATUS_DICT: DictItem<boolean>[] = [
  { value: false, label: '正常', tagType: 'success' },
  { value: true, label: '停用', tagType: 'danger' }
]

/** 菜单状态（char 0 正常 / 1 停用） */
export const MENU_STATUS_DICT: DictItem[] = [
  { value: '0', label: '正常', tagType: 'success' },
  { value: '1', label: '停用', tagType: 'danger' }
]

/** 是否显示：0 显示 / 1 隐藏 */
export const VISIBLE_DICT: DictItem[] = [
  { value: '0', label: '显示', tagType: 'success' },
  { value: '1', label: '隐藏', tagType: 'info' }
]

/** 是否外链：0 是 / 1 否 */
export const IS_FRAME_DICT: DictItem<number>[] = [
  { value: 0, label: '是', tagType: 'warning' },
  { value: 1, label: '否', tagType: 'success' }
]

/** 是否缓存：0 缓存 / 1 不缓存 */
export const IS_CACHE_DICT: DictItem<number>[] = [
  { value: 0, label: '缓存', tagType: 'success' },
  { value: 1, label: '不缓存', tagType: 'info' }
]

/** 性别 */
export const SEX_DICT: DictItem<number>[] = [
  { value: 0, label: '未知', tagType: 'info' },
  { value: 1, label: '男', tagType: 'primary' },
  { value: 2, label: '女', tagType: 'danger' }
]

/** 用户类型 */
export const USER_TYPE_DICT: DictItem[] = [
  { value: SYS_USER_TYPE.SYS_DEF, label: '系统用户', tagType: 'danger' },
  { value: SYS_USER_TYPE.ORDINARY, label: '普通用户', tagType: 'info' }
]

/** 角色类型 */
export const ROLE_TYPE_DICT: DictItem[] = [
  { value: SYS_USER_TYPE.SYS_DEF, label: '系统角色', tagType: 'danger' },
  { value: SYS_USER_TYPE.ORDINARY, label: '普通角色', tagType: 'info' }
]

/** 菜单归属级别 */
export const MENU_OWNER_DICT: DictItem[] = [
  { value: SYS_MENU_TYPE.SYS_DEF, label: '系统菜单', tagType: 'danger' },
  { value: SYS_MENU_TYPE.ORDINARY, label: '普通', tagType: 'info' }
]

/** 菜单形态 */
export const MENU_TYPE_DICT: DictItem[] = [
  { value: 'M', label: '目录', tagType: 'primary' },
  { value: 'C', label: '菜单', tagType: 'success' },
  { value: 'F', label: '按钮', tagType: 'warning' },
  { value: 'A', label: '接口', tagType: 'info' }
]

/**
 * 数据范围。选择 1（全部数据权限）的角色会拿到全量菜单，
 * requireSysUser=true 的选项只有系统用户（00）创建角色时可选。
 */
export const DATA_SCOPE_DICT: (DictItem & { requireSysUser?: boolean })[] = [
  { value: DATA_SCOPE.ALL, label: '全部数据权限', tagType: 'danger', requireSysUser: true },
  { value: DATA_SCOPE.CUSTOM, label: '自定义数据权限', tagType: 'primary' },
  { value: DATA_SCOPE.DEPT, label: '本部门数据权限', tagType: 'warning' },
  { value: DATA_SCOPE.DEPT_CHILD, label: '本部门及以下数据权限', tagType: 'info' }
]

/**
 * 日志模块（对应 na-syslog 的 ModuleEnum，以及项目自建的 MyModelEnum）。
 *
 * 注意：OperationLogAspect 按 AutoLogConfig.isCn() 分支取值，而该开关**默认 false**，
 * 所以 sys_logs.module 落库的是英文 code（如 USER_MANAGEMENT），不是中文描述。
 * 若后端把 cn 配成 true，此处的 value 会失配——列表页的模块/操作筛选用 allow-create
 * 允许手工输入，以兼容两种配置。
 */
export const LOG_MODULE_DICT: DictItem[] = [
  { value: 'USER_MANAGEMENT', label: '用户管理' },
  { value: 'ROLE_MANAGEMENT', label: '角色管理' },
  { value: 'MENU_MANAGEMENT', label: '菜单管理' },
  { value: 'LOG_MANAGEMENT', label: '日志管理' },
  { value: 'SYSTEM_CONFIG', label: '系统配置' },
  { value: 'SYSTEM_MONITOR', label: '系统监控' },
  { value: 'LOGIN_OUT', label: '登录登出' },
  { value: 'AUTH_RESOURCE_MANAGEMENT', label: '权限资源管理' },
  { value: 'IMG', label: '图片模块' },
  { value: 'MODEL', label: '模型管理' },
  { value: 'REMOTE_API', label: '远程接口' },
  { value: 'BING', label: '绑定' },
  { value: 'UN_BIND', label: '解绑' },
  { value: 'AUTH', label: '授权' },
  { value: 'CONVERSION', label: '数据转换' },
  { value: 'DEF_MODEL', label: '默认模块' }
]

/** 日志操作类型（对应 OperateEnum code） */
export const LOG_OPERATE_DICT: DictItem[] = [
  { value: 'ADD', label: '新增' },
  { value: 'QUERY', label: '查询' },
  { value: 'UPDATE', label: '编辑' },
  { value: 'DELETE', label: '删除' },
  { value: 'BIND', label: '绑定' },
  { value: 'UN_BIND', label: '解绑' },
  { value: 'AUTH', label: '授权' },
  { value: 'UN_AUTH', label: '取消授权' },
  { value: 'LOGIN', label: '登录' },
  { value: 'LOGOUT', label: '登出' },
  { value: 'UPLOAD', label: '上传' },
  { value: 'DOWNLOAD', label: '下载' },
  { value: 'CHECK', label: '检查' },
  { value: 'CONVERSION', label: '数据转换' },
  { value: 'DEF_OPT', label: '默认操作' }
]

/** 日志执行结果：OPT 正常 / EX 异常（后端 SysLogDto.TYPE_NORMAL / TYPE_EX） */
export const LOG_TYPE_DICT: DictItem[] = [
  { value: 'OPT', label: '正常', tagType: 'success' },
  { value: 'EX', label: '异常', tagType: 'danger' }
]

/** 日志耗时比较方式（对应 consumingType） */
export const CONSUMING_TYPE_DICT: DictItem<number>[] = [
  { value: 1, label: '大于' },
  { value: 2, label: '大于等于' },
  { value: 3, label: '等于' },
  { value: 4, label: '小于' },
  { value: 5, label: '小于等于' }
]

/** 按 value 取字典项 */
export function findDict<V extends string | number | boolean>(
  list: DictItem<V>[],
  value?: V | null
) {
  return list.find((item) => item.value === value)
}
