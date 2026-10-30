/**
 * 前端兜底主键生成（技术债）。
 *
 * 背景：后端用户 / 角色 / 菜单新增接口的入参 VO 把 id 标成了 @NotNull，
 * 其中「用户新增」服务端会用全字段拷贝，传入的 id 会真实落库
 * （角色 / 菜单新增服务端忽略入参 id，由 MyBatis-Plus 雪花生成）。
 * 为统一通过校验，新增这三类数据时前端生成 19 位纯数字字符串。
 *
 * 根治方案：后端去掉这三个 save VO 的 id @NotNull，统一由 ASSIGN_ID 生成并回传。
 *
 * 结构：Date.now()(13) + 客户端编号(2) + 随机序列(4) = 19 位
 * 全程为字符串，禁止 Number() 转换（超过 Number.MAX_SAFE_INTEGER）。
 */

const CLIENT_NO_KEY = '__na_cid'
const MAX_BIGINT = 9223372036854775807n // bigint 最大值

function getClientNo(): string {
  let clientNo = localStorage.getItem(CLIENT_NO_KEY)
  if (!clientNo || !/^\d{2}$/.test(clientNo)) {
    clientNo = String(10 + Math.floor(Math.random() * 90)) // 10..99
    localStorage.setItem(CLIENT_NO_KEY, clientNo)
  }
  return clientNo
}

/** 生成 19 位数字字符串 ID（与雪花 ID 处于不同数值区段，碰撞概率可忽略） */
export function generateId(): string {
  const seq = Math.floor(Math.random() * 9000) + 1000 // 1000..9999
  const id = `${Date.now()}${getClientNo()}${seq}`
  if (!/^\d{1,19}$/.test(id) || BigInt(id) > MAX_BIGINT) {
    throw new Error('主键生成异常：超出 bigint 范围')
  }
  return id
}
