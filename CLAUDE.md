# na 项目开发规则

本项目是**单体** RBAC 权限系统：后端只有 `na-upms` 一个可运行模块，直接连接 MySQL / Redis；前端在 `web/`。以下约束是权限体系的核心，改动用户 / 角色 / 菜单相关代码时必须遵守。

## 项目结构

- 根 `pom.xml` 只聚合 `na-upms`，不再有 `na-common` / `na-entity` / `na-core` / `na-parent` 模块。
- 源码包仍按职责划分，但物理上都在 `na-upms/src/main/java`：
  - `com.na.admin`：Controller、Service、VO、日志格式化器
  - `com.na.common`：登录态、WebSocket、通用基础设施
  - `com.na.core`：Mapper / Repo
  - `com.na.entity` / `com.na.model` / `com.na.params`
- MyBatis XML 位于 `na-upms/src/main/resources/mapper/mysql`。
- 不再使用 Nacos、注册中心、网关、OpenFeign 远程调用或独立工作流模块。

## 类型码约定

`sys_user.user_type`、`sys_role.role_type`、`sys_menu.sys_menu_type` 共用同一套两字符编码：

| 码 | 含义 | 说明 |
|---|---|---|
| `00` | 系统内置 | 初始化脚本 / DBA 维护，接口不可增删改 |
| `03` | 普通 | 用户 / 业务自建数据，建表默认值 |

外部依赖 `io.github.na-tool` 的 `IGlobalConst.SECURITY.SYS_TYPE` 已定义常量（`SYS_DEF` / `ORDINARY` 等），不要另写 `"00"` / `"03"` 字面量。

**判断系统用户只认 `00`**：`IGlobalConst.SECURITY.isSysDef(type)` 的实现是 `"00".equals(type)`。

**`sys_menu` 有两个 type 字段，不要混用**：

- `menu_type`（char(1)）：`M` 目录 / `C` 菜单 / `F` 按钮 / `A` api —— 描述菜单的**形态**
- `sys_menu_type`（varchar(2)）：`00` / `03` —— 描述菜单的**归属级别**

## 字段取值约定

**`sys_role.data_scope`** 数据范围：

| 值 | 含义 |
|---|---|
| `1` | 全部数据权限（同时意味着登录后拿到全量菜单） |
| `2` | 自定数据权限 |
| `3` | 本部门数据权限 |
| `4` | 本部门及以下数据权限 |

> **注意**：`data_scope='1'` 是“拿到全量菜单”的开关。`MyAuthCheckedServiceImpl` 登录鉴权只看这一个字段：
>
> ```java
> Boolean exists = roleModels.stream()
>         .anyMatch(role -> IGlobalConst.SECURITY.SYS_MENU_DATA_SCOPE.ALL.equals(role.getDataScope()));
> if (exists || IGlobalConst.SECURITY.isSysDef(loginVO.getUserType())) {
>     menuAll = sysMenuRepo.queryMenuAll(params);       // 全量菜单
> } else {
>     menuAll = sysMenuRepo.selectMenuTreeByUserId(userId);  // 只给角色已绑菜单
> }
> ```
>
> 为避免普通用户自造全量菜单角色，`SysRoleServiceImpl.saveObj` 只允许 `00` 用户创建 `data_scope='1'` 的角色。`3` / `4` 目前未落地业务逻辑。

**部门与岗位（`sys_dept` / `sys_post` / `sys_map_user_dept` / `sys_map_user_post`）目前只作为用户的标签，不建立业务逻辑。**

## 数据保护规则

| 对象类型码 | 可修改 / 删除？ |
|---|---|
| `00` 系统内置 | **否** —— 返回 `Status.SYS_DEF_DATA_BAN_OPT`，对所有操作者一视同仁 |
| `03` 普通 | 是 |

- `SysUserServiceImpl.updateObj/deleteObj`、`SysRoleServiceImpl.updateObj/deleteObj`、`SysMenuServiceImpl.updateObj/deleteObj` 均有 `isSysDef` 保护。
- **接口创建用户 / 角色一律落 `03`**：`saveObj` 会强制覆盖 `userType` / `roleType`，防止通过接口伪造 `00` 提权；`00` 只由 `doc/na_app.sql` 或 DBA 维护。
- 系统菜单（`sys_menu_type='00'`）的保护同时存在于前端隐藏操作入口与后端校验，二者必须同时保留。

## 菜单与角色授权

- 菜单授权以**角色**为单位：`sys_map_role_menu(role_id, menu_id)`。
- `roleMenuPaging` 返回全量未删除菜单树 + 该角色已绑定菜单 id。
- `saveRoleMenu` 校验：
  1. 角色存在且未删除；
  2. `menuIds` 全部存在且 `is_del=0`；
  3. 删除该角色原有绑定后批量写入。
- 没有“租户菜单”这一层，不存在 `sys_map_tenant_menu`。
- 删除菜单时同步清理 `sys_map_role_menu`；删除角色时同步清理其菜单绑定。
- 普通用户（`03`）不能查看 / 修改角色菜单授权，接口返回 `USER_NO_ACCESS`。

## 删除前置条件

- 删除角色前必须解除用户绑定（`sys_map_role_user`），否则提示「请先解除用户与该角色的绑定关系，再删除角色」。
- 删除部门 / 岗位前必须解除用户绑定（`sys_map_user_dept` / `sys_map_user_post`）。
- 删除用户前必须解除角色绑定；不允许删除当前登录账号。
- 删除菜单前必须无未删除子菜单。

## 登录态与 Token

登录 token 是 **22 字符短 token**，登录态以 Redis 为准。修改登录 / 鉴权相关代码时遵守：

- **唯一入口**：token 的读写统一走 `LoginSessionService`（`com.na.common.login`），不要在业务代码里直接拼 Redis key。
- **Redis key**：
  - `token:login:index:{token}` → `userId`
  - `token:login:{userId}:{token}` → `ResLoginVO`（多点模式）
  - `token:login:{userId}` → `ResLoginVO`（单点模式）
- **单点开关**：`na.login.single-token`（默认 `false`）。
- **不要再引入 JWT 解析**：`JwtUtil` / `UserDto` 仍可能留在外部依赖里，但鉴权链路已不使用。
- **HTTP 与 WebSocket 的 token 写法不同**：HTTP 用 `Authorization: Bearer <token>`（`TokenUtil.token()` 会去掉前缀）；WebSocket 只收纯 token，`?token=<token>` 或 `Authorization: <token>`。
- **登出语义**：`loginOut` 清理该用户所有会话 + 当前 token 索引。

## WebSocket

- 由 `na.common.websocket` 自动配置提供，默认 `na.websocket.enable=false`。
- 会话索引按 `userType:userId` 维护，不再有租户维度。
- 发送 API：`sendToSession` / `sendToUser(userType, userId, ...)` / `sendToUserType` / `broadcast`。
- 前端连接地址：`/na-upms/ws?token=...`，由 Vite/Nginx 剥离 `/na-upms` 前缀后转发到后端 `/ws`。
- 修改 `WebSocketMessageSender` 或 Redis 消息体时，需同步 `WebSocketDisabledConfiguration`、Redis 监听器与示例监听器。

## 唯一约束（已定）

| 表 | 索引 | 语义 |
|---|---|---|
| `sys_role` | `uk_sys_role_code (role_code)` | 全局唯一 |
| `sys_user` | `uk_sys_user_user_name (user_name)` | **全局唯一，逻辑删除后仍永久占用** |
| `sys_dept` | `uk_sys_dept_name (name)` | 全局唯一 |
| `sys_post` | `uk_sys_post_code (code)`、`uk_sys_post_name (name)` | 全局唯一 |
| `system_config` | `uk_system_config_code (code, is_del)` | 未删除配置项编码唯一 |

### `is_del` 与查重

唯一索引**不含 `is_del`** 的表，逻辑删除的行照样占着索引位。所以「先查重再插入」的 SQL **必须不带 `is_del` 过滤**，否则会出现「应用层查不到 → 放行 → DB 撞唯一索引 → `DuplicateKeyException` 500」。

按此规则实现的查重：

- 建用户 `queryUserByUserName` —— 不过滤 `is_del`
- 建角色 `queryRoleByRoleObj` —— 不带 `is_del`
- 建部门 `queryDeptByName` —— 不带 `is_del`
- 建岗位 `queryPostByCode` / `queryPostByName` —— 不带 `is_del`
- 登录 `queryUserByUserNameOrPhone` —— 不过滤 `is_del`，由 `SysUserServiceImpl.login` 在应用层拒绝已删除用户

`system_config` 的唯一索引包含 `is_del`，查重使用 `queryByCode`（过滤 `is_del=0`）。

## 数据库初始化

- 初始化脚本：`doc/na_app.sql`，用于空库初始化。
- 脚本会创建 `na_app_single` 数据库、12 张表，并写入管理员 `site / 123456` 与超级管理员角色。
- 不再包含 `sys_tenant`、`sys_map_tenant_menu`、`tenant_id`、`sensitive_word`、`sys_user_token`。
- 菜单种子全部为 `sys_menu_type='00'`（内置保护），新菜单由接口创建，默认 `03`。

## 已知限制

- `data_scope` 的 `3` / `4` 仅存储，未参与实际数据过滤。
- 部门 / 岗位不参与业务逻辑，仅作标签。
- `sys_menu` 种子覆盖全部内置菜单，因此菜单管理页对内置菜单只读；新增菜单可正常编辑 / 删除。
- 外部 `io.github.na-tool:na-common` 的 `IGlobalConst.SECURITY.SYS_MENU_DATA_SCOPE.DEPT_CHILD` 被写成了 `"3"`（与 `DEPT` 同值），按语义应为 `"4"`。属外部 jar 的问题，本项目无法直接改，用到时留意。

## 版权声明

版权所有 © 2025-2026 pg_tech。保留所有权利。

本项目仅供**个人学习、研究**使用，**禁止任何形式的商业用途**。如需商用，请先联系 **pg_tech@163.com** 取得书面授权。

完整条款见 [LICENSE.md](./LICENSE.md)，项目说明见 [README.md](./README.md)。
