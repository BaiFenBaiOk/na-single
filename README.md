# na · RBAC 权限管理平台

> 版本 **3.0.0** ｜ 后端 Java 17 + Spring Boot 3.5 ｜ 前端 Vue 3 + Vite 6 + Element Plus

na 是一个**单体** RBAC 权限管理平台：后端只有一个可运行模块 `na-upms`，直接连接 MySQL 与 Redis；前端通过 `/api/na-upms/**` 访问后端，不依赖 Nacos、注册中心、网关或独立工作流服务。

> [!IMPORTANT]
> **版权与授权**：本项目仅供**个人学习、研究**使用，**禁止任何形式的商业用途**。
> 如需商用，请先联系 **pg_tech@163.com** 获取书面授权。完整条款见 [LICENSE.md](./LICENSE.md)。

---

## 功能特性

| 模块 | 能力 |
|---|---|
| 登录认证 | 图形验证码、22 字符短 token + Redis 会话、登出、锁屏密码校验 |
| 用户管理 | 分页查询、新增 / 编辑 / 删除、分配角色、分配部门、分配岗位、踢下线 |
| 角色管理 | 分页查询、新增 / 编辑 / 删除、角色 - 菜单授权 |
| 菜单管理 | 目录 `M` / 菜单 `C` / 按钮 `F` / 接口 `A` 四种形态；`00` 系统菜单受保护 |
| 部门 / 岗位 | 组织标签维护，不参与业务数据范围计算 |
| 系统配置 | 全局配置项（`system_config`），支持 Redis 缓存与匿名读取接口 |
| 日志与监控 | 操作日志、服务信息、Redis 缓存面板 |
| 实时通信 | 原生 WebSocket（默认关闭），支持按用户 / 用户类型投递 |

前端支持 PC 与 H5 自适应布局，登录后按后端菜单动态生成路由，按钮级权限通过 `v-hasPermi` 指令控制。

## 技术栈

| 分类 | 组件 |
|---|---|
| 后端 | Java 17、Spring Boot 3.5.16、Undertow |
| 持久层 | MyBatis-Plus 3.5.9、MySQL 8.x、Druid |
| 缓存 / 会话 | Redis、Redisson |
| 安全 | Spring Security + 外部 `io.github.na-tool:na-auth` |
| 日志 | 外部 `io.github.na-tool:na-log` |
| 接口文档 | SpringDoc OpenAPI 3 + Knife4j |
| 前端 | Vue 3.5、Vite 6、TypeScript 5.7、Element Plus 2.9、Pinia、Vue Router |

## 系统架构

```text
浏览器 / H5
   │  /api/na-upms/**、/na-upms/ws、/na-upms/video/**
   ▼
Nginx（生产）或 Vite 代理（开发）
   │
   ▼
na-upms :10061  ──直连──▶  MySQL
   │
   └──直连──▶  Redis
```

- 后端只有一个 Maven 模块 `na-upms`，源码包结构仍按职责划分为 `com.na.admin`、`com.na.common`、`com.na.core`、`com.na.entity`、`com.na.model`、`com.na.params`。
- 所有 Controller 统一使用 `/api/na-upms/**` 前缀。
- 生产环境由 Nginx 同源托管前端并反向代理后端，配置参考 [`doc/nginx-direct.conf`](./doc/nginx-direct.conf)。

## 目录结构

```text
na/
├── pom.xml                     # 父 POM，仅聚合 na-upms
├── na-upms/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/na/
│       │   ├── admin/          # Controller / Service / VO / 日志格式化器
│       │   ├── common/         # 登录态、WebSocket、通用工具
│       │   ├── core/           # Mapper / Repo
│       │   ├── entity/         # 数据库实体
│       │   ├── model/          # 业务模型
│       │   └── params/         # 查询参数
│       └── resources/
│           ├── application.yml
│           ├── mapper/mysql/   # MyBatis XML
│           └── META-INF/spring # 自动配置注册
├── web/                        # Vue 3 前端
├── doc/
│   ├── na_app.sql              # 数据库初始化脚本
│   ├── login-token.md
│   ├── websocket.md
│   └── nginx-direct.conf
└── README.md
```

## 快速开始

### 1. 初始化数据库

在 MySQL 8.x 中执行：

```bash
mysql -uroot -p < doc/na_app.sql
```

脚本会创建 `na_app_single` 数据库及全部表，并写入：

- 内置管理员：`site / 123456`
- 内置超级管理员角色：`super_admin`，已绑定全部菜单
- 首页、系统管理、权限管理、角色 / 菜单 / 用户 / 部门 / 岗位 / 日志 / 配置等菜单

### 2. 启动后端

默认直连本机 MySQL / Redis，可用环境变量覆盖。`application.yml` 中的账号、密码默认值统一为占位符 `xxxx`，启动前必须通过环境变量注入真实值：

| 环境变量 | 默认值 | 说明 |
|---|---|---|
| `MYSQL_HOST` | `127.0.0.1` | MySQL 地址 |
| `MYSQL_PORT` | `3306` | MySQL 端口 |
| `MYSQL_DATABASE` | `na_app_single` | 数据库名 |
| `MYSQL_USERNAME` | `xxxx` | 数据库账号 |
| `MYSQL_PASSWORD` | `xxxx` | 数据库密码 |
| `REDIS_HOST` | `127.0.0.1` | Redis 地址 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `REDIS_PASSWORD` | `xxxx` | Redis 密码 |
| `REDIS_DATABASE` | `6` | Redis 数据库索引 |

```bash
mvn clean package -DskipTests
java -jar na-upms/target/na-upms.jar
```

启动后：

- 服务端口：`10061`
- 接口文档：`http://localhost:10061/doc.html`
- Druid 监控：`http://localhost:10061/druid/`（账号 / 密码默认占位为 `xxxx / xxxx`，请按需修改配置）

### 3. 启动前端

```bash
cd web
npm install
npm run dev
```

开发地址 `http://localhost:5173`，Vite 会把 `/api/na-upms` 与 `/na-upms` 代理到 `http://localhost:10061`。

生产构建：

```bash
npm run build
```

构建产物在 `web/dist`，由 Nginx 托管。

## 类型码约定

`sys_user.user_type`、`sys_role.role_type`、`sys_menu.sys_menu_type` 共用同一套两字符编码：

| 码 | 含义 | 说明 |
|---|---|---|
| `00` | 系统内置 | 初始化脚本 / DBA 维护，接口不可增删改 |
| `03` | 普通 | 通过接口创建的数据，建表默认值 |

- 接口创建用户 / 角色时一律落 `03`，不能通过接口伪造 `00`。
- `00` 用户（`TokenUtil.isDefAuth()`）拥有菜单管理与系统数据维护权限。
- 用户、角色、菜单的 `00` 数据在接口层受保护，删除 / 修改会返回 `SYS_DEF_DATA_BAN_OPT`。

## 权限模型

```text
用户 ──sys_map_role_user──▶ 角色 ──sys_map_role_menu──▶ 菜单
```

- 登录后按用户角色加载菜单；`00` 用户直接获得全部菜单。
- 角色可绑定的菜单为 `sys_menu` 全量未删除菜单。
- `sys_role.data_scope = '1'` 的角色也会获得全部菜单，仅系统用户（`00`）可创建该数据范围的角色。
- 部门 / 岗位目前只作为用户标签，不参与数据范围计算。

## 登录态与 Token

- 登录 token 为 22 字符短 token，登录态以 Redis 为准。
- 唯一入口：`com.na.common.login.LoginSessionService`。
- Redis key：
  - `token:login:index:{token}` → `userId`
  - `token:login:{userId}:{token}` → `ResLoginVO`（多点模式）
  - `token:login:{userId}` → `ResLoginVO`（单点模式）
- 单点开关：`na.login.single-token`（默认 `false`）。
- HTTP 请求头：`Authorization: Bearer <token>`。
- WebSocket：`ws://host/na-upms/ws?token=<token>`，经代理剥离 `/na-upms` 后到后端 `/ws`。

详细说明见 [`doc/login-token.md`](./doc/login-token.md) 与 [`doc/websocket.md`](./doc/websocket.md)。

## 数据库

初始化脚本 [`doc/na_app.sql`](./doc/na_app.sql) 包含以下表：

| 表 | 说明 |
|---|---|
| `sys_user` | 用户 |
| `sys_role` | 角色 |
| `sys_menu` | 菜单 / 按钮 / 接口 |
| `sys_dept` | 部门（标签） |
| `sys_post` | 岗位（标签） |
| `sys_map_role_user` | 用户 - 角色 |
| `sys_map_role_menu` | 角色 - 菜单 |
| `sys_map_user_dept` | 用户 - 部门 |
| `sys_map_user_post` | 用户 - 岗位 |
| `sys_logs` | 操作日志 |
| `system_form` | 配置分组 |
| `system_config` | 全局配置项 |

关键唯一约束：

| 表 | 索引 | 语义 |
|---|---|---|
| `sys_user` | `uk_sys_user_user_name(user_name)` | 全局唯一，逻辑删除后仍占用 |
| `sys_role` | `uk_sys_role_code(role_code)` | 全局唯一 |
| `sys_dept` | `uk_sys_dept_name(name)` | 全局唯一 |
| `sys_post` | `uk_sys_post_code(code)`、`uk_sys_post_name(name)` | 全局唯一 |
| `system_config` | `uk_system_config_code(code, is_del)` | 未删除配置项编码唯一 |

> **查重约定**：唯一索引不含 `is_del` 的表（用户 / 角色 / 部门 / 岗位），应用层查重 SQL 一律**不过滤 `is_del`**，否则会出现「应用层查不到 → 放行 → DB 撞唯一索引 → 500」。

## 前端页面

| 路径 | 页面 |
|---|---|
| `/dashboard` | 首页 |
| `/userList` | 用户管理 |
| `/roleList` | 角色管理 |
| `/menuList` | 菜单管理 |
| `/deptList` | 部门管理 |
| `/postList` | 岗位管理 |
| `/logsList` | 操作日志 |
| `/sysConfigList` | 系统配置 |
| `/monitor` | 服务 / 缓存监控 |
| `/profile` | 个人中心 |

## 常见问题

- **启动时报数据库连接失败**：确认 MySQL 已启动、`na_app_single` 已初始化，或通过 `MYSQL_*` 环境变量指定连接信息。
- **登录提示验证码错误**：验证码存放在 Redis `captcha:{uuid}`，确认 Redis 可连接。
- **登录成功但请求 401**：确认前后端使用同一个 Redis；修改 token 格式或单点模式会使旧会话立即失效。
- **菜单为空**：确认 `sys_map_role_menu` 已为当前角色绑定菜单；`00` 用户不依赖该表。
- **WebSocket 连接失败**：默认 `na.websocket.enable=false`，需要时在 `application.yml` 打开，并确认代理已放行 `/na-upms/ws`。

## 版权声明

版权所有 © 2025-2026 pg_tech。保留所有权利。

本项目仅供个人学习、研究使用，禁止任何形式的商业用途。完整条款见 [LICENSE.md](./LICENSE.md)。
