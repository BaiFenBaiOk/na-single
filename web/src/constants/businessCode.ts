/** 后端业务状态码（na-tool Status 枚举 + 项目 MyStatus），用于前端按码分流 */
export const BIZ_CODE = {
  /** 默认成功（R.successNotMsg） */
  SUCCESS_DEFAULT: 0,
  /** 成功 */
  SUCCESS: 1,
  /** 通用失败 */
  ERROR: -1,
  /** 业务异常（BusinessException） */
  EXCEPTION: -2,
  /** 未登录 */
  UNAUTHORIZED: 401,
  /** 无权限 */
  NO_ACCESS: 403,
  /** 参数无效 / 参数格式错误 */
  PARAMS_INVALID: 10004,
  /** 系统默认数据禁止操作 */
  SYS_DEF_BAN_OPT: 10008,
  /** 验证码错误（项目 MyStatus） */
  CAPTCHA_ERROR: 140003,

  /** 用户不存在 */
  USER_NOT_EXIST: 40000,
  /** 用户已存在 */
  USER_ALREADY_EXIST: 40001,
  /** 用户未开启 */
  USER_NOT_ENABLE: 40002,

  /** 角色已存在 */
  ROLE_ALREADY_EXIST: 50000,
  /** 菜单已存在 */
  MENU_ALREADY_EXIST: 70000
} as const

/** 系统数据类型码：00 系统内置（绝对保护），03 普通 */
export const SYS_USER_TYPE = {
  SYS_DEF: '00',
  ORDINARY: '03'
} as const

/** 菜单归属级别（sys_menu_type）：00 系统内置，03 普通 */
export const SYS_MENU_TYPE = {
  SYS_DEF: '00',
  ORDINARY: '03'
} as const

/** 数据范围（data_scope） */
export const DATA_SCOPE = {
  ALL: '1',
  CUSTOM: '2',
  DEPT: '3',
  DEPT_CHILD: '4'
} as const
