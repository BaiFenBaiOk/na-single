# 贡献指南

感谢你关注本项目。提交代码前请阅读以下约定。

## 开发环境

- JDK 17
- Maven 3.9+
- Node.js 20+
- MySQL 8.x
- Redis
- 后端直连 MySQL 与 Redis（连接信息见 `application.yml`）

## 分支与提交

- 从 `dev` 分支创建功能分支：`feature/xxx`、`fix/xxx`
- 提交信息使用**中文**，简明描述改动内容
- 一个提交只做一件事，避免把格式化与功能修改混在一起
- 不要提交数据库密码、Redis 密码、Token、日志与真实环境连接信息

## 代码规范

- 后端遵循现有分层：`controller` / `service` / `repo` / `entity` / `model`
- 前端使用 Vue 3 + TypeScript + Element Plus，保持现有组件与目录风格
- 注释、错误提示使用中文；API/SDK 等专业术语可保留英文
- 修改权限、角色、菜单相关逻辑前，先阅读根目录 `CLAUDE.md`

## 本地验证

提交 PR 前请至少执行：

```bash
# 后端编译
mvn -DskipTests compile

# 前端类型检查与构建
cd web
npm ci
npm run type-check
npm run build
```

> GitHub Actions 的后端 job 需要解析外部私有构件 `io.github.na-tool:*`；请在仓库 Secrets 中配置 `MAVEN_SETTINGS`（完整的 settings.xml 内容）。

## Pull Request

- PR 目标分支：`dev`
- 描述中说明：改动目的、影响范围、验证方式
- 关联相关 Issue
- 涉及数据库结构变更时，同步更新 `doc/*.sql` 并在描述中标注
- 涉及配置变更时，同步更新 `na-upms/src/main/resources/application.yml` 与 README

## 安全

发现安全漏洞请不要公开提交 Issue，按 [SECURITY.md](./SECURITY.md) 的方式私下联系。

## 许可

提交 PR 即表示你同意你的贡献按照本项目 [LICENSE.md](./LICENSE.md) 的授权条款进行授权。
