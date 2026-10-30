// 与后端 RequestParamsValidUtil 保持一致的校验正则

/** 账号：仅字母数字 */
export const REG_USERNAME = /^[a-zA-Z0-9]+$/

/** 密码：字母数字及允许的符号 */
export const REG_PASSWORD = /^[a-zA-Z0-9!@#$%^&*()_+\-=]+$/

/** 邮箱 */
export const REG_EMAIL = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/

/** 手机号 */
export const REG_PHONE = /^1[3-9]\d{9}$/

export const isAlphaNumeric = (v?: string | null): boolean =>
  !!v && REG_USERNAME.test(v)

export const isValidPassword = (v?: string | null): boolean =>
  !!v && REG_PASSWORD.test(v)

export const isValidEmail = (v?: string | null): boolean =>
  v === undefined || v === null || v === '' ? true : REG_EMAIL.test(v)

export const isValidPhone = (v?: string | null): boolean =>
  v === undefined || v === null || v === '' ? true : REG_PHONE.test(v)
