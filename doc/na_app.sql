/*
============================================================
na 单体权限管理平台 - 数据库初始化脚本
------------------------------------------------------------
适用数据库：MySQL 8.x
说明：
  1. 本脚本用于空库初始化，会先删除同名旧表；
  2. 不再包含租户相关表 / 字段（sys_tenant、sys_map_tenant_menu、tenant_id）；
  3. 内置管理员：site / 123456（user_type = '00'）；
  4. 内置超级管理员角色：super_admin（role_type = '00'），已绑定全部菜单；
  5. 新增数据默认类型码为 '03'（普通），'00' 仅由脚本 / DBA 维护。
============================================================
*/

CREATE DATABASE IF NOT EXISTS `na_app_single`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE `na_app_single`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- 清理旧表（含已下线的租户 / 工作流相关表）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `sys_map_tenant_menu`;
DROP TABLE IF EXISTS `sys_tenant`;
DROP TABLE IF EXISTS `sensitive_word`;
DROP TABLE IF EXISTS `sys_user_token`;
DROP TABLE IF EXISTS `sys_map_role_menu`;
DROP TABLE IF EXISTS `sys_map_role_user`;
DROP TABLE IF EXISTS `sys_map_user_dept`;
DROP TABLE IF EXISTS `sys_map_user_post`;
DROP TABLE IF EXISTS `sys_logs`;
DROP TABLE IF EXISTS `system_config`;
DROP TABLE IF EXISTS `system_form`;
DROP TABLE IF EXISTS `sys_menu`;
DROP TABLE IF EXISTS `sys_post`;
DROP TABLE IF EXISTS `sys_dept`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `sys_user`;

-- ------------------------------------------------------------
-- 用户表
-- ------------------------------------------------------------
CREATE TABLE `sys_user` (
    `id`          bigint       NOT NULL,
    `user_name`   varchar(30)  NOT NULL COMMENT '用户账号（全局唯一，逻辑删除后仍占用）',
    `pass_word`   varchar(100) NOT NULL COMMENT '密码',
    `nick_name`   varchar(30)  NOT NULL COMMENT '用户昵称',
    `user_type`   varchar(2)   NOT NULL DEFAULT '03' COMMENT '用户类型（00 系统内置 / 03 普通）',
    `email`       varchar(50)           DEFAULT NULL COMMENT '邮箱地址',
    `phone`       varchar(11)           DEFAULT NULL COMMENT '手机号',
    `avatar`      varchar(100)          DEFAULT NULL COMMENT '头像',
    `sex`         tinyint               DEFAULT '0' COMMENT '0默认 1男 2女',
    `status`      tinyint      NOT NULL DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
    `login_ip`    varchar(100)          DEFAULT NULL COMMENT '登录IP',
    `login_time`  datetime              DEFAULT NULL COMMENT '登录时间',
    `creator_id`  bigint       NOT NULL DEFAULT '0' COMMENT '创建人',
    `create_time` datetime              DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime              DEFAULT NULL COMMENT '更新时间',
    `remark`      varchar(300)          DEFAULT NULL COMMENT '备注',
    `is_del`      tinyint               DEFAULT '0' COMMENT '0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_user_name` (`user_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户表';

-- ------------------------------------------------------------
-- 角色表
-- ------------------------------------------------------------
CREATE TABLE `sys_role` (
    `id`          bigint      NOT NULL,
    `role_name`   varchar(30) NOT NULL COMMENT '角色名称',
    `role_code`   varchar(30) NOT NULL COMMENT '角色编码（全局唯一）',
    `role_sort`   int         NOT NULL DEFAULT '0' COMMENT '显示顺序',
    `data_scope`  varchar(1)  NOT NULL DEFAULT '2' COMMENT '数据范围（1全部 2自定 3本部门 4本部门及以下）',
    `status`      tinyint     NOT NULL DEFAULT '0' COMMENT '角色状态（0正常 1停用）',
    `create_time` datetime             DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime             DEFAULT NULL COMMENT '更新时间',
    `role_type`   varchar(2)  NOT NULL DEFAULT '03' COMMENT '角色类型（00 系统内置 / 03 普通）',
    `is_del`      tinyint              DEFAULT '0' COMMENT '0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_role_code` (`role_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='角色表';

-- ------------------------------------------------------------
-- 菜单表
-- ------------------------------------------------------------
CREATE TABLE `sys_menu` (
    `id`            bigint       NOT NULL COMMENT '菜单ID',
    `name`          varchar(50)           DEFAULT NULL COMMENT '路由 name（全局唯一）',
    `menu_name`     varchar(50)  NOT NULL COMMENT '菜单名称',
    `parent_id`     bigint       NOT NULL DEFAULT '0' COMMENT '父菜单ID',
    `order_num`     int          NOT NULL DEFAULT '9999' COMMENT '显示顺序',
    `path`          varchar(200)          DEFAULT '' COMMENT '路由地址',
    `component`     varchar(255)          DEFAULT NULL COMMENT '组件路径',
    `request_uri`   varchar(200)          DEFAULT NULL COMMENT 'api 地址',
    `perms`         varchar(50)           DEFAULT NULL COMMENT '权限标识',
    `query`         varchar(255)          DEFAULT NULL COMMENT '路由参数',
    `is_frame`      int          NOT NULL DEFAULT '1' COMMENT '是否为外链（0是 1否）',
    `is_cache`      int          NOT NULL DEFAULT '0' COMMENT '是否缓存（0缓存 1不缓存）',
    `menu_type`     char(1)      NOT NULL DEFAULT '' COMMENT '菜单形态（M目录 C菜单 F按钮 Aapi）',
    `visible`       char(1)      NOT NULL DEFAULT '0' COMMENT '是否显示（0显示 1隐藏）',
    `status`        char(1)      NOT NULL DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
    `icon`          varchar(100) NOT NULL DEFAULT '#' COMMENT '菜单图标',
    `create_time`   datetime              DEFAULT NULL COMMENT '创建时间',
    `update_time`   datetime              DEFAULT NULL COMMENT '更新时间',
    `remark`        varchar(500)          DEFAULT '' COMMENT '备注',
    `sys_menu_type` varchar(2)   NOT NULL DEFAULT '03' COMMENT '归属级别（00 系统内置 / 03 普通）',
    `is_del`        tinyint               DEFAULT '0' COMMENT '0未删除 1已删除',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='菜单表';

-- ------------------------------------------------------------
-- 部门表
-- ------------------------------------------------------------
CREATE TABLE `sys_dept` (
    `id`          bigint      NOT NULL,
    `parent_id`   bigint      NOT NULL DEFAULT '0' COMMENT '上级部门ID（0=顶级）',
    `name`        varchar(20) NOT NULL COMMENT '部门名称（全局唯一）',
    `create_time` datetime             DEFAULT NULL,
    `update_time` datetime             DEFAULT NULL,
    `is_del`      tinyint     NOT NULL DEFAULT '0',
    `order_num`   int         NOT NULL DEFAULT '9999' COMMENT '显示顺序',
    `remark`      varchar(200)         DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_dept_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='部门表';

-- ------------------------------------------------------------
-- 岗位表
-- ------------------------------------------------------------
CREATE TABLE `sys_post` (
    `id`          bigint      NOT NULL,
    `parent_id`   bigint      NOT NULL DEFAULT '0' COMMENT '上级岗位ID（平铺结构，恒为 0）',
    `name`        varchar(20) NOT NULL COMMENT '岗位名称（全局唯一）',
    `create_time` datetime             DEFAULT NULL,
    `update_time` datetime             DEFAULT NULL,
    `is_del`      tinyint     NOT NULL DEFAULT '0',
    `code`        varchar(20) NOT NULL COMMENT '岗位编码（全局唯一）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_post_name` (`name`),
    UNIQUE KEY `uk_sys_post_code` (`code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='岗位表';

-- ------------------------------------------------------------
-- 操作日志表
-- ------------------------------------------------------------
CREATE TABLE `sys_logs` (
    `id`             varchar(64) NOT NULL DEFAULT '' COMMENT '主键',
    `request_ip`     varchar(50)          DEFAULT '' COMMENT 'IP地址',
    `type`           varchar(20)          DEFAULT '' COMMENT '操作类型 OPT EX',
    `user_id`        bigint               DEFAULT NULL COMMENT '用户ID',
    `description`    varchar(200)         DEFAULT '' COMMENT 'api描述信息',
    `class_path`     varchar(200)         DEFAULT '' COMMENT '类名',
    `action_method`  varchar(30)          DEFAULT '' COMMENT '方法名',
    `request_uri`    varchar(200)         DEFAULT '' COMMENT '请求api',
    `http_method`    varchar(10)          DEFAULT '' COMMENT '请求方式',
    `params`         text COMMENT '请求参数',
    `result`         text,
    `ex_desc`        text COMMENT '异常描述',
    `ex_detail`      text COMMENT '异常对象',
    `start_time`     datetime             DEFAULT NULL COMMENT '请求时间',
    `finish_time`    datetime             DEFAULT NULL COMMENT '响应时间',
    `consuming_time` bigint               DEFAULT NULL COMMENT '消耗时间',
    `module`         varchar(30)          DEFAULT '' COMMENT '模块名',
    `operate`        varchar(30)          DEFAULT '' COMMENT '操作名',
    `content`        varchar(255)         DEFAULT NULL COMMENT '其他描述信息',
    `ua`             text COMMENT '浏览器',
    `operate_time`   datetime             DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `user_from`      tinyint              DEFAULT '0' COMMENT '用户来源 0授权系统 1外部',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='操作日志表';

-- ------------------------------------------------------------
-- 用户-角色关联
-- ------------------------------------------------------------
CREATE TABLE `sys_map_role_user` (
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `role_id` bigint NOT NULL COMMENT '角色ID',
    UNIQUE KEY `uk_role_user` (`user_id`, `role_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户角色关联表';

-- ------------------------------------------------------------
-- 角色-菜单关联
-- ------------------------------------------------------------
CREATE TABLE `sys_map_role_menu` (
    `role_id` bigint NOT NULL COMMENT '角色ID',
    `menu_id` bigint NOT NULL COMMENT '菜单ID',
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='角色菜单关联表';

-- ------------------------------------------------------------
-- 用户-部门关联（单部门）
-- ------------------------------------------------------------
CREATE TABLE `sys_map_user_dept` (
    `user_id` bigint NOT NULL,
    `dept_id` bigint NOT NULL COMMENT '部门ID',
    UNIQUE KEY `uk_user_dept` (`user_id`, `dept_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户部门关联表';

-- ------------------------------------------------------------
-- 用户-岗位关联（多岗位）
-- ------------------------------------------------------------
CREATE TABLE `sys_map_user_post` (
    `user_id` bigint NOT NULL,
    `post_id` bigint NOT NULL COMMENT '岗位ID',
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户岗位关联表';

-- ------------------------------------------------------------
-- 配置分组表
-- ------------------------------------------------------------
CREATE TABLE `system_form` (
    `id`   bigint      NOT NULL,
    `name` varchar(20) DEFAULT NULL,
    `code` varchar(20) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='配置分组表';

-- ------------------------------------------------------------
-- 配置项表
-- ------------------------------------------------------------
CREATE TABLE `system_config` (
    `id`      bigint       NOT NULL,
    `form_id` bigint       NOT NULL,
    `code`    varchar(100) NOT NULL,
    `name`    varchar(100) NOT NULL,
    `value`   longtext     NOT NULL,
    `remarks` varchar(50)           DEFAULT NULL,
    `is_del`  tinyint               DEFAULT '0' COMMENT '0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_system_config_code` (`code`, `is_del`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='系统配置表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 配置分组
INSERT INTO `system_form` (`id`, `name`, `code`)
VALUES (1, '协议', NULL),
       (2, '阿里云', NULL);

-- 配置项
INSERT INTO `system_config` (`id`, `form_id`, `code`, `name`, `value`, `remarks`, `is_del`)
VALUES (1, 1, 'jiyiguize', 'jiyiguize', '平台交易规则', '平台交易规则', 0),
       (2, 1, 'zhucexieyi', 'zhucexieyi', '用户注册协议', '用户注册协议', 0),
       (3, 2, 'aliAccessKey', '阿里AccessKey', '阿里秘钥', '0', 0),
       (4, 2, 'aliSecretKey', '阿里SecretKey', '阿里秘钥', '0', 0),
       (5, 2, 'aliEndpoint', '阿里云OSS地域节点', 'oss-cn-beijing.aliyuncs.com', '0', 0),
       (6, 2, 'aliBucketName', '空间名称', '空间名称', '0', 0),
       (7, 2, 'aliDomain', '自定义域名', 'dev.oss-cn-beijing.aliyuncs.com', '0', 0),
       (8, 2, 'aliMaxSize', '文件大小', '100000000000', '0', 0),
       (9, 2, 'aliWhite', '白名单', '.jpg,.png,.doc,.docx,.ppt,.pptx,.xlx,.xlxs,.zip,.rar,.pdf,.mp4', '0', 0);

-- 部门
INSERT INTO `sys_dept` (`id`, `parent_id`, `name`, `create_time`, `update_time`, `is_del`, `order_num`, `remark`)
VALUES (1, 0, '研发部门', NULL, NULL, 0, 9999, NULL);

-- 岗位
INSERT INTO `sys_post` (`id`, `parent_id`, `name`, `create_time`, `update_time`, `is_del`, `code`)
VALUES (1, 0, '项目经理', NULL, NULL, 0, 'TEAM_LEADER');

-- 角色（系统内置，保护数据）
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `role_sort`, `data_scope`, `status`,
                        `create_time`, `update_time`, `role_type`, `is_del`)
VALUES (1, '超级管理员', 'super_admin', 0, '1', 0, NOW(), NOW(), '00', 0);

-- 管理员（系统内置，保护数据）
INSERT INTO `sys_user` (`id`, `user_name`, `pass_word`, `nick_name`, `user_type`, `email`, `phone`,
                        `avatar`, `sex`, `status`, `login_ip`, `login_time`, `creator_id`,
                        `create_time`, `update_time`, `remark`, `is_del`)
VALUES (1, 'site', '123456', '系统管理员', '00', 'na_tech@163.com', NULL,
        NULL, 0, 0, NULL, NULL, 0, NOW(), NOW(), NULL, 0);

-- 管理员绑定超级管理员角色
INSERT INTO `sys_map_role_user` (`user_id`, `role_id`)
VALUES (1, 1);

-- ------------------------------------------------------------
-- 菜单数据（全部为系统内置 00，接口不可增删改）
-- 目录 / 菜单 / 按钮 / Api 四种形态与前端动态路由保持一致
-- ------------------------------------------------------------
INSERT INTO `sys_menu` (`id`, `name`, `menu_name`, `parent_id`, `order_num`, `path`, `component`,
                        `request_uri`, `perms`, `query`, `is_frame`, `is_cache`, `menu_type`,
                        `visible`, `status`, `icon`, `create_time`, `update_time`, `remark`,
                        `sys_menu_type`, `is_del`)
VALUES
-- 首页
(1, 'home', '首页', 0, 1, '/dashboard', '/Dashboard', NULL, NULL, NULL, 1, 0, 'M', '0', '0', 'House', '2024-06-24 15:13:56', NULL, '', '00', 0),
-- 系统管理
(2, 'sysManage', '系统管理', 0, 2, '/system', '', NULL, NULL, '', 1, 0, 'M', '0', '0', 'OfficeBuilding', '2024-06-24 16:55:43', '2025-11-08 16:06:46', '', '00', 0),
(3, 'authManage', '权限管理', 2, 1, '/auth', '', NULL, NULL, '', 1, 0, 'M', '0', '0', 'OfficeBuilding', '2024-06-24 16:55:43', '2025-11-08 16:06:46', '', '00', 0),
-- 角色
(6, 'roleManage', '角色管理', 3, 2, '/role', NULL, NULL, NULL, NULL, 1, 0, 'M', '0', '0', 'Key', '2024-06-26 12:39:30', NULL, '', '00', 0),
(7, 'roleList', '角色列表', 6, 1, '/roleList', '/role/list', '', '', NULL, 1, 0, 'C', '0', '0', 'Key', '2024-06-26 12:43:17', NULL, '', '00', 0),
(21, 'roleAdd', '新增', 7, 1, '', '', '/api/na-upms/sysrole/save', 'role:save:post', '', 1, 0, 'F', '0', '0', 'success', '2025-12-11 17:51:19', '2025-12-11 17:51:26', '', '00', 0),
(22, 'roleEdit', '编辑', 7, 2, '', '', '/api/na-upms/sysrole/update', 'role:update:post', '', 1, 0, 'F', '0', '0', 'primary', '2025-12-11 17:51:21', '2025-12-11 17:51:28', '', '00', 0),
(23, 'roleDel', '删除', 7, 3, '', '', '/api/na-upms/sysrole/delete/*', 'role:delete:delete', '', 1, 0, 'F', '0', '0', 'danger', '2025-12-11 17:51:24', '2025-12-11 17:51:31', '', '00', 0),
(31, 'roleBindMenu', '分配菜单', 7, 4, '', NULL, '/api/na-upms/sysmenu/save-role-menu', 'role:bind:menu', NULL, 1, 0, 'F', '0', '0', '#', NULL, NULL, '', '00', 0),
(34, 'rolePage', '列表', 7, 5, '', NULL, '/api/na-upms/sysrole/list-paging', 'role:list:post', NULL, 1, 0, 'F', '0', '0', '#', NULL, NULL, '', '00', 0),
-- 菜单
(8, 'menuManage', '菜单管理', 3, 3, '/menu', NULL, NULL, NULL, NULL, 1, 0, 'M', '0', '0', 'Menu', '2024-06-26 12:40:11', NULL, '', '00', 0),
(9, 'menuList', '菜单列表', 8, 1, '/menuList', '/menu/list', '', '', NULL, 1, 0, 'C', '0', '0', 'Menu', '2024-06-26 12:43:04', NULL, '', '00', 0),
(24, 'menuAdd', '新增', 9, 1, '', '', '/api/na-upms/sysmenu/save', 'menu:save:post', '', 1, 0, 'F', '0', '0', 'success', '2025-12-11 17:51:19', '2025-12-11 17:51:26', '', '00', 0),
(25, 'menuEdit', '编辑', 9, 2, '', '', '/api/na-upms/sysmenu/update', 'menu:update:post', '', 1, 0, 'F', '0', '0', 'primary', '2025-12-11 17:51:21', '2025-12-11 17:51:28', '', '00', 0),
(26, 'menuDel', '删除', 9, 3, '', '', '/api/na-upms/sysmenu/delete/*', 'menu:delete:delete', '', 1, 0, 'F', '0', '0', 'danger', '2025-12-11 17:51:24', '2025-12-11 17:51:31', '', '00', 0),
(35, 'menuPage', '列表', 9, 4, '', NULL, '/api/na-upms/sysmenu/list-paging', 'menu:list:post', NULL, 1, 0, 'F', '0', '0', '#', NULL, NULL, '', '00', 0),
-- 用户
(10, 'userManage', '用户管理', 3, 4, '/user', NULL, NULL, NULL, NULL, 1, 0, 'M', '0', '0', 'User', '2024-06-26 10:48:15', NULL, '', '00', 0),
(11, 'userList', '用户列表', 10, 1, '/userList', '/user/list', '', '', NULL, 1, 0, 'C', '0', '0', 'User', '2024-06-26 10:50:53', NULL, '', '00', 0),
(27, 'userAdd', '新增', 11, 1, '', '', '/api/na-upms/sysuser/save', 'user:save:post', '', 1, 0, 'F', '0', '0', 'success', '2025-12-11 17:51:19', '2025-12-11 17:51:26', '', '00', 0),
(28, 'userEdit', '编辑', 11, 2, '', '', '/api/na-upms/sysuser/update', 'user:update:post', '', 1, 0, 'F', '0', '0', 'primary', '2025-12-11 17:51:21', '2025-12-11 17:51:28', '', '00', 0),
(29, 'userDel', '删除', 11, 3, '', '', '/api/na-upms/sysuser/delete/*', 'user:delete:delete', '', 1, 0, 'F', '0', '0', 'danger', '2025-12-11 17:51:24', '2025-12-11 17:51:31', '', '00', 0),
(32, 'userBindRole', '分配角色', 11, 4, '', NULL, '/api/na-upms/sysuser/bindRole', 'user:bind:role', NULL, 1, 0, 'F', '0', '0', '#', NULL, NULL, '', '00', 0),
(36, 'userPage', '列表', 11, 5, '', NULL, '/api/na-upms/sysuser/list-paging', 'user:list:post', NULL, 1, 0, 'F', '0', '0', '#', NULL, NULL, '', '00', 0),
(66, 'userClearToen', '清空用户token', 11, 6, '', '', '/api/na-upms/sysuser/clear/*', 'user:clearToken:get', '', 1, 0, 'F', '0', '0', 'danger', '2025-12-11 17:51:24', '2025-12-11 17:51:31', '', '00', 0),
-- 部门
(12, 'deptManage', '部门管理', 3, 5, '/dept', NULL, NULL, NULL, NULL, 1, 0, 'M', '0', '0', 'Management', '2024-06-26 12:42:55', NULL, '', '00', 0),
(13, 'deptList', '部门列表', 12, 1, '/deptList', '/dept/list', NULL, NULL, NULL, 1, 0, 'C', '0', '0', 'Management', '2024-06-26 12:42:51', NULL, '', '00', 0),
(54, 'deptPage', '列表', 13, 1, '', '', '/api/na-upms/dept/tree', 'dept:list:get', '', 1, 0, 'A', '0', '0', '#', '2026-01-07 13:51:31', '2026-01-07 13:51:33', '', '00', 0),
(55, 'deptAdd', '新增', 13, 2, '', '', '/api/na-upms/dept/save', 'dept:save:post', '', 1, 0, 'F', '0', '0', 'success', '2026-01-07 16:06:00', '2026-01-07 16:06:04', '', '00', 0),
(56, 'deptEdit', '编辑', 13, 3, '', '', '/api/na-upms/dept/update', 'dept:update:post', '', 1, 0, 'F', '0', '0', 'primary', '2026-01-07 16:06:02', '2026-01-07 16:06:05', '', '00', 0),
(57, 'deptBind', '绑定部门', 11, 7, '', '', '/api/na-upms/sysuser/bindDept', 'user:bind:dept', '', 1, 0, 'F', '0', '0', 'primary', '2026-01-07 16:06:02', '2026-01-07 16:06:05', '', '00', 0),
(58, 'deptDel', '删除', 13, 4, '', '', '/api/na-upms/dept/delete/*', 'dept:delete:delete', '', 1, 0, 'F', '0', '0', 'primary', '2026-01-07 16:06:02', '2026-01-07 16:06:05', '', '00', 0),
-- 岗位
(14, 'postManage', '岗位管理', 3, 6, '/post', '', NULL, NULL, '', 1, 0, 'M', '0', '0', 'Management', '2024-06-26 12:42:55', '2025-11-11 14:22:24', '', '00', 0),
(15, 'postList', '岗位列表', 14, 1, '/postList', '/post/list', NULL, NULL, '', 1, 0, 'C', '0', '0', 'Management', '2024-06-26 12:42:51', '2025-11-11 14:23:13', '', '00', 0),
(78, 'postPage', '列表', 15, 1, '', '', '/api/na-upms/post/list-paging', 'post:list:post', '', 1, 0, 'F', '0', '0', '#', '2026-10-15 14:16:26', NULL, '', '00', 0),
(79, 'postAdd', '新增', 15, 2, '', '', '/api/na-upms/post/save', 'post:save:post', '', 1, 0, 'F', '0', '0', 'success', '2026-10-15 14:16:26', NULL, '', '00', 0),
(80, 'postEdit', '编辑', 15, 3, '', '', '/api/na-upms/post/update', 'post:update:post', '', 1, 0, 'F', '0', '0', 'primary', '2026-10-15 14:16:26', NULL, '', '00', 0),
(81, 'postDel', '删除', 15, 4, '', '', '/api/na-upms/post/delete/*', 'post:delete:delete', '', 1, 0, 'F', '0', '0', 'danger', '2026-10-15 14:16:26', NULL, '', '00', 0),
(82, 'postBind', '绑定岗位', 11, 8, '', '', '/api/na-upms/sysuser/bindPost', 'user:bind:post', '', 1, 0, 'F', '0', '0', 'primary', '2026-10-15 14:16:26', NULL, '', '00', 0),
-- 日志
(16, 'LogManage', '日志管理', 3, 7, '/logs', NULL, NULL, NULL, NULL, 1, 0, 'M', '0', '0', 'Document', '2024-06-26 12:40:57', NULL, '', '00', 0),
(17, 'LogList', '日志列表', 16, 1, '/logsList', '/logs/list', '/api/na-upms/log/list-paging', 'log:list:post', NULL, 1, 0, 'C', '0', '0', 'Document', '2024-06-26 12:42:19', NULL, '', '00', 0),
(71, 'logPage', '列表', 17, 1, '', '', '/api/na-upms/log/list-paging', 'log:list:post', '', 1, 0, 'A', '0', '0', '#', '2026-01-14 21:00:02', '2026-01-14 21:00:03', '', '00', 0),
(84, 'logDetail', '详情', 17, 2, '', NULL, '/api/na-upms/log/list-paging', 'log:detail:get', NULL, 1, 0, 'F', '0', '0', 'primary', '2026-10-19 15:37:47', NULL, '', '00', 0),
-- 系统配置
(67, 'sysConfigManage', '配置管理', 3, 8, '/sysConfig', '', '', '', '', 1, 0, 'C', '0', '0', 'Document', '2024-06-26 12:40:57', '2026-01-14 19:50:06', '', '00', 0),
(68, 'sysConfigList', '配置列表', 67, 1, '/sysConfigList', '/sysConfig/list', '/api/na-upms/sys-config/list-paging', 'sysConfig:list:post', '', 1, 0, 'C', '0', '0', 'Document', '2024-06-26 12:42:19', '2026-01-14 19:51:12', '', '00', 0),
(69, 'sysConfigAdd', '新增', 68, 1, '', '', '/api/na-upms/sys-config/save', 'sysConfig:save:post', '', 1, 0, 'F', '0', '0', 'success', '2026-01-14 19:52:50', '2026-01-14 19:52:52', '', '00', 0),
(70, 'sysConfigEdit', '编辑', 68, 2, '', '', '/api/na-upms/sys-config/update', 'sysConfig:update:post', '', 1, 0, 'F', '0', '0', 'primary', '2026-01-14 19:53:51', '2026-01-14 19:53:52', '', '00', 0),
(83, 'sysConfigDel', '删除', 68, 3, '', NULL, '/api/na-upms/sys-config/delete/*', 'sysConfig:delete:delete', NULL, 1, 0, 'F', '0', '0', 'danger', '2026-10-19 15:37:47', NULL, '', '00', 0);

-- 超级管理员绑定全部菜单
INSERT INTO `sys_map_role_menu` (`role_id`, `menu_id`)
SELECT 1, `id` FROM `sys_menu` WHERE `is_del` = 0;

SET FOREIGN_KEY_CHECKS = 1;
