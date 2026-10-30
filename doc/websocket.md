# WebSocket 实时通信

na-upms 内置一套基于 Spring 原生 WebSocket 的实时推送能力，默认关闭。它与 HTTP 共用同一套 Redis 登录态，不依赖网关或独立工作流服务。

## 1. 配置

```yaml
na:
  websocket:
    enable: false              # 是否启用
    path: /ws                  # 服务内连接路径
    sender-type: redis         # local 单机 / redis 多实例广播
    allowed-origins: "*"
    send-time-limit: 5000
    buffer-size-limit: 102400
    auth-recheck-interval-ms: 60000
    redis-channel: na:websocket:broadcast
```

- 前端连接地址：`ws://host/na-upms/ws?token=<token>`。
- Vite / Nginx 负责把 `/na-upms` 前缀剥离，后端实际注册路径为 `/ws`。
- `na.auth.skipApis` 中已放行 `/ws`、`/ws/**`；`WebSocketAutoConfiguration` 启动时也会程序化追加，避免配置被覆盖。

## 2. 相关代码

| 文件 | 职责 |
|---|---|
| `com.na.common.websocket.config.WebSocketAutoConfiguration` | 自动配置、注册 Handler / 拦截器 / 发送器 |
| `com.na.common.websocket.config.WebSocketDisabledConfiguration` | 关闭时提供 no-op 发送器 |
| `com.na.common.websocket.core.security.WebSocketAuthService` | 握手 token 校验 |
| `com.na.common.websocket.core.security.LoginUserHandshakeInterceptor` | 握手拦截、写入登录用户 |
| `com.na.common.websocket.core.session.WebSocketSessionManager(Impl)` | 会话索引 |
| `com.na.common.websocket.core.sender.*` | 本地 / Redis 消息发送 |
| `com.na.admin.websocket.DemoWebSocketMessageListener` | 示例监听器 |

## 3. 握手鉴权

浏览器原生 WebSocket 无法设置 `Authorization` 请求头，因此支持：

1. 查询参数：`/na-upms/ws?token=<token>`（浏览器主通道）；
2. 请求头：`Authorization: <token>`（非浏览器客户端，纯 token，不带 `Bearer `）。

握手流程：

1. 从 query / header 读取 token；
2. `LoginSessionService.get(token)` 读取 Redis 登录态；
3. 校验通过后把 `MyAuthDto` 写入 session attributes；
4. 校验失败返回 HTTP 401。

长连接期间会按 `auth-recheck-interval-ms` 周期性复核 token，失效后关闭连接（关闭码 `4001`）。

## 4. 消息协议

文本帧为 JSON：

```json
{
  "type": "demo",
  "content": "{\"text\":\"hello\"}"
}
```

- `type`：消息类型，用于路由到对应 `WebSocketMessageListener`；
- `content`：字符串；业务对象需先序列化为 JSON 字符串。

心跳：

- 客户端发送文本 `ping`；
- 服务端回复文本 `pong`；
- 客户端也可发送 `{"type":"pong"}`。

## 5. 服务端发送 API

`WebSocketMessageSender`：

```java
void sendToSession(String sessionId, String type, Object content);

void sendToUser(String userType, Long userId, String type, Object content);

void sendToUserType(String userType, String type, Object content);

void broadcast(String type, Object content);
```

- `userType` 传 `null` 表示不限制用户类型，按 userId 查找全部会话。
- 会话索引按 `userType:userId` 维护，不再包含租户维度。
- `content` 为 `CharSequence` 时原样下发，其他对象由 Jackson 序列化。

示例：

```java
webSocketMessageSender.sendToUser(null, userId, "notice", Map.of("text", "hello"));
```

## 6. 会话管理

`WebSocketSessionManager` 维护三组索引：

| 索引 | 说明 |
|---|---|
| `sessionId -> WebSocketSession` | 按连接定向发送 |
| `userType:userId -> sessions` | 按用户类型 + 用户发送 |
| `userId -> sessions` | userType 为空时按用户发送 |
| `userType -> sessions` | 按用户类型群发 |

连接建立 / 关闭由 `WebSocketSessionHandlerDecorator` 自动维护。

## 7. 多实例广播

`sender-type=redis` 时：

1. 发送方把消息发布到 `na:websocket:broadcast`；
2. 各实例的 `RedisWebSocketMessageListener` 收到后，只投递给本实例持有的连接；
3. 消息体中的 `targetType` 为 `SESSION` / `USER` / `USER_TYPE` / `ALL`。

单机部署可使用 `sender-type=local`。

## 8. 前端接入

`web/src/utils/websocket.ts` 已封装：

- 自动读取本地持久化 token；
- 指数退避重连；
- 心跳保活；
- 按 `type` 订阅 / 取消订阅；
- 鉴权失败（4001）时清理 token 并跳转登录。

```ts
wsClient.connect()
const off = wsClient.on('notice', (content) => {
  console.log(JSON.parse(content))
})
off()
```

## 9. 代理配置

开发环境 `vite.config.ts`：

```ts
'/na-upms': {
  target: 'http://localhost:10061',
  changeOrigin: true,
  ws: true,
  rewrite: (path) => path.replace(/^\/na-upms/, '')
}
```

Nginx：

```nginx
location /na-upms/ {
    rewrite ^/na-upms(/.*)$ $1 break;
    proxy_pass http://na_upms;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection $connection_upgrade;
    proxy_read_timeout 3600s;
    proxy_send_timeout 3600s;
    proxy_buffering off;
}
```

## 10. 常见问题

| 现象 | 排查 |
|---|---|
| 连接被直接拒绝 | `na.websocket.enable` 是否为 `true` |
| 握手 401 | token 是否有效；query 中的 token 是否误加 `Bearer ` 前缀 |
| 连接建立后立刻断开 | 代理是否正确转发 `Upgrade` / `Connection` 头 |
| 收不到消息 | 检查 `type` 是否与监听器 `getType()` 一致；多实例时是否配置了 redis 发送器 |
| 一段时间后自动断开 | 客户端心跳是否正常；`auth-recheck-interval-ms` 到期后 token 是否仍有效 |
