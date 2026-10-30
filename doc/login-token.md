# 登录态与 Token

na 使用 **22 字符短 token + Redis 会话**，token 本身不携带任何业务信息，鉴权以 Redis 中的登录态为准。

## 1. 相关代码

| 文件 | 职责 |
|---|---|
| `com.na.common.login.LoginSessionService` | token 读写唯一入口 |
| `com.na.common.login.LoginProperties` | `na.login.*` 配置 |
| `com.na.common.login.LoginConfig` | 注册 `LoginSessionService` |
| `com.na.common.utils.ShortTokenUtil` | 生成 22 字符 token |
| `com.na.common.utils.TokenUtil` | 从请求头提取 token、读取当前登录用户 |
| `com.na.admin.controller.LoginController` | 登录 / 登出接口 |
| `com.na.admin.service.impl.SysUserServiceImpl` | 校验账号密码、构造 `ResLoginVO` |
| `com.na.admin.service.MyAuthCheckedServiceImpl` | 每次请求从 Redis 恢复登录态 |

## 2. Redis key

| key | value | 说明 |
|---|---|---|
| `token:login:index:{token}` | `userId` | token 索引，鉴权时先用它反查用户 |
| `token:login:{userId}:{token}` | `ResLoginVO` | 多点模式会话 |
| `token:login:{userId}` | `ResLoginVO` | 单点模式会话 |

- 单点 / 多点由 `na.login.single-token` 控制，默认 `false`（允许多端同时在线）。
- 单点模式下，新登录会使旧 token 立即失效。

## 3. 登录流程

1. 前端请求验证码：`GET /api/na-upms/systemInfo/get/{uuid}`，验证码写入 Redis `captcha:{uuid}`，有效期 5 分钟。
2. 前端提交 `POST /api/na-upms/login`：

   ```json
   {
     "userName": "site",
     "passWord": "123456",
     "uuid": "验证码uuid",
     "captcha": "abcd"
   }
   ```

3. 后端校验验证码、账号、密码、账号状态与 `is_del`。
4. 生成 22 字符 token，构造 `ResLoginVO`（userId、userName、nickName、userType、token、登录时间/IP/归属地等）。
5. `LoginSessionService.save` 写入 token 索引与会话。
6. 返回：

   ```json
   {
     "code": 0,
     "data": {
       "userId": 1,
       "userName": "site",
       "nickName": "系统管理员",
       "userType": "00",
       "token": "AbCdEfGhIjKlMnOpQrStUv",
       "loginTime": "2026-10-28T10:00:00",
       "loginIp": "127.0.0.1",
       "loginAddress": ["中国", "本地", "局域网"]
     }
   }
   ```

## 4. 请求鉴权

- HTTP 请求头：

  ```http
  Authorization: Bearer AbCdEfGhIjKlMnOpQrStUv
  ```

- `TokenUtil.token()` 会去掉 `Bearer ` 前缀。
- `MyAuthCheckedServiceImpl` 通过 `LoginSessionService.get(token)` 从 Redis 恢复 `ResLoginVO`，再加载用户、角色、菜单与权限集合，写入 Spring Security 上下文。
- 未登录 / token 过期返回 401。

## 5. 登出

`GET /api/na-upms/login/loginOut`：

1. 清理该用户的所有会话（`token:login:{userId}*`）；
2. 清理当前 token 的索引；
3. 多点模式下其他终端的索引会随 TTL 自然过期。

## 6. WebSocket 共用登录态

WebSocket 握手复用同一套 Redis 登录态：

- 浏览器连接：`ws://host/na-upms/ws?token=<token>`；
- 代理层剥离 `/na-upms` 后，后端实际路径为 `/ws`；
- 详细说明见 [`websocket.md`](./websocket.md)。

## 7. 配置

```yaml
na:
  login:
    single-token: false
```

- `single-token: false`：多点模式，同一账号可在多个终端登录。
- `single-token: true`：单点模式，只保留最新登录。

> 切换 token 格式或单点模式会使已有会话失效，用户需要重新登录。

## 8. 常见问题

| 现象 | 排查 |
|---|---|
| 登录提示验证码错误 | Redis 是否可连接；`captcha:{uuid}` 是否已过期 |
| 登录成功但后续请求 401 | 前后端是否使用同一个 Redis / database；token 是否被代理截断 |
| 多端登录后旧终端 401 | 是否开启了 `single-token: true` |
| WebSocket 握手 401 | token 是否放在 query 参数中且未加 `Bearer ` 前缀 |
| 修改配置后仍使用旧逻辑 | 清理 Redis 登录态并重新登录 |
