CREATE TABLE IF NOT EXISTS `oauth_access_token`  (
    `token_id` VARCHAR(256) NULL DEFAULT NULL,
    `token` BLOB NULL,
    `authentication_id` VARCHAR(256) NOT NULL,
    `user_name` VARCHAR(256) NULL DEFAULT NULL,
    `client_id` VARCHAR(256) NULL DEFAULT NULL,
    `authentication` BLOB NULL,
    `refresh_token` VARCHAR(256) NULL DEFAULT NULL,
    PRIMARY KEY (`authentication_id`) USING BTREE
) COMMENT ='OAuth2.0需要的表';

CREATE TABLE IF NOT EXISTS `oauth_approvals`  (
    `userId` VARCHAR(256)  NULL DEFAULT NULL,
    `clientId` VARCHAR(256) NULL DEFAULT NULL,
    `scope` VARCHAR(256) NULL DEFAULT NULL,
    `status` VARCHAR(10) NULL DEFAULT NULL,
    `expiresAt` TIMESTAMP(0) NULL DEFAULT NULL,
    `lastModifiedAt` TIMESTAMP(0) NULL DEFAULT NULL
) COMMENT ='OAuth2.0需要的表';

CREATE TABLE IF NOT EXISTS `oauth_client_details`  (
    `client_id` VARCHAR(256) NOT NULL,
    `resource_ids` VARCHAR(256) NULL DEFAULT NULL,
    `client_secret` VARCHAR(256) NULL DEFAULT NULL,
    `scope` VARCHAR(256) NULL DEFAULT NULL,
    `authorized_grant_types` VARCHAR(256) NULL DEFAULT NULL,
    `web_server_redirect_uri` VARCHAR(256) NULL DEFAULT NULL,
    `authorities` VARCHAR(256) NULL DEFAULT NULL,
    `access_token_validity` INT(0) NULL DEFAULT NULL,
    `refresh_token_validity` INT(0) NULL DEFAULT NULL,
    `additional_information` VARCHAR(4096) NULL DEFAULT NULL,
    `autoapprove` VARCHAR(256) NULL DEFAULT NULL,
    PRIMARY KEY (`client_id`) USING BTREE
) COMMENT ='OAuth2.0需要的表';

CREATE TABLE IF NOT EXISTS `oauth_client_token`  (
    `token_id` VARCHAR(256) NULL DEFAULT NULL,
    `token` blob NULL,
    `authentication_id` VARCHAR(256) NOT NULL,
    `user_name` VARCHAR(256) NULL DEFAULT NULL,
    `client_id` VARCHAR(256) NULL DEFAULT NULL,
    PRIMARY KEY (`authentication_id`) USING BTREE
) COMMENT ='OAuth2.0需要的表';

CREATE TABLE IF NOT EXISTS  `oauth_code`  (
    `code` VARCHAR(256) NULL DEFAULT NULL,
    `authentication` BLOB NULL
) COMMENT ='OAuth2.0需要的表';

CREATE TABLE IF NOT EXISTS `oauth_refresh_token`  (
    `token_id` VARCHAR(256) NULL DEFAULT NULL,
    `token` BLOB NULL,
    `authentication` blob NULL
) COMMENT ='OAuth2.0需要的表';

CREATE TABLE IF NOT EXISTS `sys_admin`
(
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sys_role_id`     BIGINT UNSIGNED NOT NULL COMMENT '角色id(sys_role表的主键)',
    `username`        VARCHAR(64)         NOT NULL COMMENT '管理员的登陆用户名',
    `password`        VARCHAR(128)        NOT NULL COMMENT '管理员的登陆密码',
    `name`            VARCHAR(64)         NOT NULL COMMENT '管理员姓名',
    `phone_number`    VARCHAR(64)         NOT NULL COMMENT '管理员手机号码',
    `email`           VARCHAR(128)        NOT NULL COMMENT '管理员邮箱',
    `remark`          VARCHAR(255)        NOT NULL COMMENT '管理员备注信息',
    `row_create_time` DATETIME(3)         NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `row_update_time` DATETIME(3)         NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_sys_admin_username` (`username`),
    INDEX `idx_sys_admin_sys_role_id` (`sys_role_id`)
) COMMENT ='管理员信息';

CREATE TABLE IF NOT EXISTS `sys_page`
(
    `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`            VARCHAR(64)          NOT NULL COMMENT '页面名称',
    `url`             VARCHAR(256)         NOT NULL COMMENT '页面地址',
    `is_menu`         BIT(1)               NOT NULL COMMENT '页面是否出现在菜单栏',
    `is_default`      BIT(1)               NOT NULL COMMENT '是否是默认页(只允许有一个默认页，如果设置多个，以第一个为准)',
    `is_blank`        BIT(1)               NOT NULL COMMENT '是否新开窗口打开页面',
    `icon_class`      VARCHAR(64)          NOT NULL COMMENT 'html中的图标样式',
    `parent_id`       BIGINT(20) UNSIGNED  NOT NULL COMMENT '父级id(即本表的主键id)',
    `order_num`       BIGINT(128) UNSIGNED NOT NULL COMMENT '顺序号(值越小, 排名越靠前)',
    `remark`          VARCHAR(256)         NOT NULL COMMENT '备注',
    `row_create_time` DATETIME(3)          NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `row_update_time` DATETIME(3)          NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_url` (`url`) USING BTREE
) COMMENT ='页面配置';

CREATE TABLE IF NOT EXISTS `sys_permission`
(
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sys_role_id`     BIGINT UNSIGNED NOT NULL COMMENT 'sys_role的主键id',
    `sys_page_id`     BIGINT UNSIGNED NOT NULL COMMENT 'sys_page的主键id',
    `can_insert`      TINYINT(1)          NOT NULL COMMENT '是否能新增(true:能; false:不能)',
    `can_delete`      TINYINT(1)          NOT NULL COMMENT '是否能删除(true:能; false:不能)',
    `can_update`      TINYINT(1)          NOT NULL COMMENT '是否能修改(true:能; false:不能)',
    `can_select`      TINYINT(1)          NOT NULL COMMENT '是否能读取(true:能; false:不能)',
    `row_create_time` DATETIME(3)         NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `row_update_time` DATETIME(3)         NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_sys_role_id_page_id` (`sys_role_id`, `sys_page_id`)
) COMMENT ='用户权限信息';

CREATE TABLE IF NOT EXISTS `sys_role`
(
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`            VARCHAR(64)         NOT NULL COMMENT '角色名称',
    `super_admin`     TINYINT(1)          NOT NULL COMMENT '是否是超级管理员(1:是; 0:否)',
    `remark`          VARCHAR(256)        NOT NULL COMMENT '角色说明',
    `row_create_time` DATETIME(3)         NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `row_update_time` DATETIME(3)         NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_sys_role_name` (`name`)
) COMMENT ='角色信息';

CREATE TABLE IF NOT EXISTS `t_api`  (
    `id` BIGINT(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` VARCHAR(128) NOT NULL COMMENT '接口名称',
    `method` VARCHAR(64) NOT NULL COMMENT '请求方式',
    `service_id` VARCHAR(128) NOT NULL COMMENT '所属服务名称',
    `path` VARCHAR(128) NOT NULL COMMENT '请求路径',
    `row_create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `row_update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `idx_service_id_path`(`service_id`, `path`) USING BTREE,
    INDEX `idx_name`(`name`) USING BTREE
) COMMENT ='SpringCloud接口信息';

CREATE TABLE IF NOT EXISTS `t_app`  (
    `id` BIGINT(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `client_id` VARCHAR(256) NOT NULL COMMENT '换取token的id',
    `client_secret` VARCHAR(256) NOT NULL COMMENT '换取token的secret',
    `name` VARCHAR(255) NOT NULL COMMENT '应用名称',
    `is_admin` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否是管理员(具有所有接口的调用权限)',
    `description` VARCHAR(255) NOT NULL COMMENT '描述信息',
    `row_create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `row_update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `idx_client_id`(`client_id`) USING BTREE,
    INDEX `idx_name`(`name`) USING BTREE
) COMMENT = 'SpringCloud应用信息';

CREATE TABLE IF NOT EXISTS `t_auth`  (
    `id` BIGINT(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `app_id` BIGINT(0) UNSIGNED NOT NULL COMMENT 't_app表的主键',
    `api_id` BIGINT(0) UNSIGNED NOT NULL COMMENT 't_api表的主键',
    `row_create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `row_update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `idx_app_id_api_id`(`app_id`, `api_id`) USING BTREE,
    INDEX `idx_api_id`(`api_id`) USING BTREE
) COMMENT = '应用所具有的接口权限';

CREATE TABLE IF NOT EXISTS `t_gray`  (
    `id` BIGINT(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `gray_id` VARCHAR(64) NOT NULL COMMENT '分支标识',
    `description` VARCHAR(256) NOT NULL COMMENT '分支描述信息',
    `row_create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `row_update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `idx_gray_id`(`gray_id`) USING BTREE
) COMMENT = '存储灰度发布描述信息';

CREATE TABLE IF NOT EXISTS `t_ignore_url`  (
    `id` BIGINT(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `url` VARCHAR(128) NOT NULL COMMENT '符合AntPathMatcher表达式的url',
    `description` VARCHAR(128) NOT NULL COMMENT '描述信息',
    `row_create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `row_update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `idx_url`(`url`) USING BTREE
) COMMENT = '网关白名单的接口, 不需要授权可直接调用';

CREATE TABLE IF NOT EXISTS `t_route`  (
    `id` BIGINT(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `route_id` VARCHAR(64) NOT NULL COMMENT '路由id',
    `uri` VARCHAR(256) NOT NULL COMMENT '转发目标uri',
    `predicates` TEXT NOT NULL COMMENT '断言字符串',
    `filters` TEXT NOT NULL COMMENT '过滤器字符串',
    `order_num` INT(0) UNSIGNED NOT NULL COMMENT '路由执行顺序',
    `enabled` BIT(1) NOT NULL COMMENT '是否启用',
    `description` VARCHAR(256) NOT NULL COMMENT '描述信息',
    `row_create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `row_update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `idx_route_id`(`route_id`) USING BTREE
) COMMENT = '网关的路由信息';





INSERT IGNORE INTO `sys_admin`(`id`, `sys_role_id`, `username`, `password`, `name`, `phone_number`, `email`, `remark`)VALUES (1, 1, 'admin', 'ebc255e6a0c6711a4366bc99ebafb54f', '超级管理员', '18000000000', 'administrator@sjb.com', '超级管理员');

INSERT IGNORE INTO `sys_role`(`id`, `name`, `super_admin`, `remark`) VALUES (1, '超级管理员', 1, '超级管理员, 拥有最高权限');


INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`, `order_num`, `remark`) VALUES (1, '网关管理', '', b'1', b'0', b'0', 'layui-icon-website', 0, 1, '网关管理');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`, `order_num`, `remark`) VALUES (2, '路由配置', '/route/tolist', b'1', b'0', b'0', '', 1, 1, '动态路由配置');

INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`, `order_num`, `remark`) VALUES (3, '灰度发布', '/gray/tolist', b'1', b'1', b'0', 'layui-icon-release', 0, 2, '动态灰度发布');

INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`, `order_num`, `remark`) VALUES (4, '授权管理', '', b'1', b'0', b'0', 'layui-icon-app', 0, 3, '授权管理');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`, `order_num`, `remark`) VALUES (5, '接口列表', '/api/tolist', b'1', b'0', b'0', '', 4, 1, '接口列表');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`, `order_num`, `remark`) VALUES (6, '应用列表', '/app/tolist', b'1', b'0', b'0', '', 4, 2, '应用列表');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`, `order_num`, `remark`) VALUES (7, '权限列表', '/auth/tolist', b'1', b'0', b'0', '', 4, 3, '权限列表');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`, `order_num`, `remark`) VALUES (8, '白名单列表', '/ignoreurl/tolist', b'1', b'0', b'0', '', 4, 4, '白名单列表');

INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`, `order_num`, `remark`) VALUES (9, '链路跟踪', 'http://192.168.6.167:9411', b'1', b'0', b'1', 'layui-icon-location', 0, 4, 'Zipkin链路跟踪');

INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`, `order_num`, `remark`) VALUES (10, '配置中心', 'http://apollo.wyyt.com/signin', b'1', b'0', b'1', 'layui-icon-form', 0, 5, 'Apollo配置中心');

INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`, `order_num`, `remark`) VALUES (11, '注册中心', 'http://192.168.5.21:8500', b'1', b'0', b'1', 'layui-icon-component', 0, 6, 'Consul注册中心');

INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`, `order_num`, `remark`) VALUES (12, '监控中心', 'http://springadmin.wyyt.com', b'1', b'0', b'1', 'layui-icon-chart-screen', 0, 7, 'SpringBootAdmin监控中心');

INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`, `order_num`, `remark`) VALUES (13, '文档中心', 'http://127.0.0.1:80/doc.html', b'1', b'0', b'1', 'layui-icon-read', 0, 8, '接口文档中心');

INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`, `order_num`, `remark`) VALUES (14, '工具集合', '', b'1', b'0', b'0', 'layui-icon-util', 0, 9, '工具集合');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`, `order_num`, `remark`) VALUES (15, '注册中心管理', '/consul/tolist', b'1', b'0', b'0', '', 14, 1, '注册中心管理');

INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`, `order_num`, `remark`) VALUES (16, '系统设置', '', b'1', b'0', b'0', 'layui-icon-set', 0, 10, '系统设置');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`, `order_num`, `remark`) VALUES (17, '页面配置', '/page/tolist', b'1', b'0', b'0', '', 16, 1, '页面配置');

INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`, `order_num`, `remark`) VALUES (18, '权限设置', '', b'1', b'0', b'0', 'layui-icon-password', 0, 11, '权限设置');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`, `order_num`, `remark`) VALUES (19, '管理员配置', '/admin/tolist', b'1', b'0', b'0', '', 18, 1, '管理员配置');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`, `order_num`, `remark`) VALUES (20, '角色管理', '/role/tolist', b'1', b'0', b'0', '', 18, 2, '角色管理');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`, `order_num`, `remark`) VALUES (21, '权限管理', '/permission/tolist', b'1', b'0', b'0', '', 18, 3, '权限管理');

INSERT IGNORE INTO `t_ignore_url`(`id`, `url`, `description`) VALUES (1, '/**/inspector/inspect/**', '链路探测(用于灰度发布的测试使用)');
INSERT IGNORE INTO `t_ignore_url`(`id`, `url`, `description`) VALUES (2, '/**/actuator/**', '心跳接口');
INSERT IGNORE INTO `t_ignore_url`(`id`, `url`, `description`) VALUES (3, '/**/v2/api-docs/**', 'api文档');
INSERT IGNORE INTO `t_ignore_url`(`id`, `url`, `description`) VALUES (4, '/**/v1/oauth/token/**', '授权获取access token');
INSERT IGNORE INTO `t_ignore_url`(`id`, `url`, `description`) VALUES (5, '/**/v1/oauth/logout/**', '注销已授权的access token');

INSERT IGNORE INTO `t_route`(`id`, `route_id`, `uri`, `predicates`, `filters`, `order_num`, `enabled`, `description`) VALUES (1, 'auth', 'lb://auth', 'Path=/auth/**', 'StripPrefix=1', 0, b'1', 'SpringCloud的授权服务');