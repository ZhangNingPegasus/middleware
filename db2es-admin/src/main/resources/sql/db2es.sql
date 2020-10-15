CREATE TABLE IF NOT EXISTS `sys_admin`
(
    `id`              BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sys_role_id`     BIGINT(20) UNSIGNED NOT NULL COMMENT '角色id(sys_role表的主键)',
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
) ENGINE = InnoDB COMMENT ='管理员信息';
INSERT IGNORE INTO `sys_admin`(`id`, `sys_role_id`, `username`, `password`, `name`, `phone_number`, `email`, `remark`)
VALUES (1, 1, 'admin', 'ebc255e6a0c6711a4366bc99ebafb54f', '超级管理员', '18000000000', 'administrator@sjb.com', '超级管理员');



CREATE TABLE IF NOT EXISTS `sys_page`
(
    `id`              BIGINT(20) UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
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
) ENGINE = InnoDB COMMENT ='页面配置';
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`,
                              `order_num`, `remark`)
VALUES (1, '服务列表', '/server/tolist', b'1', b'1', b'0', 'layui-icon-template-1', 0, 1, '展现db2es_server中所有已配置的kafka主题');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`,
                              `order_num`, `remark`)
VALUES (2, 'ES索引列表', '/topic/tolist', b'1', b'0', b'0', 'layui-icon-app', 0, 1, '维护所有的kafka主题');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`,
                              `order_num`, `remark`)
VALUES (3, '索引重建', '/rebuild/tolist', b'1', b'0', b'0', 'layui-icon-cols', 0, 2, '维护所有的kafka主题');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`,
                              `order_num`, `remark`)
VALUES (4, '错误列表', '/error/tolist', b'1', b'0', b'0', 'layui-icon-survey', 0, 3, '展现db2es同步过程中遇到的错误');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`,
                              `order_num`, `remark`)
VALUES (5, 'Kafka监控平台', 'http://192.168.6.167:9999', b'1', b'0', b'1', 'layui-icon-chart-screen', 0, 4, 'Kafka监控平台');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`,
                              `order_num`, `remark`)
VALUES (6, 'SQL工具', 'http://192.168.6.12:10086', b'1', b'0', b'1', 'layui-icon-layer', 0, 5, 'SQL工具');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`,
                              `order_num`, `remark`)
VALUES (7, 'Kibana', 'http://192.168.6.166:5601', b'1', b'0', b'1', 'layui-icon-senior', 0, 6, 'Kibana');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`,
                              `order_num`, `remark`)
VALUES (8, '权限设置', '', b'1', b'0', b'0', 'layui-icon-password', 0, 7, '权限设置');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`,
                              `order_num`, `remark`)
VALUES (9, '管理员配置', '/admin/tolist', b'1', b'0', b'0', '', 8, 1, '管理员配置');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`,
                              `order_num`, `remark`)
VALUES (10, '角色管理', '/role/tolist', b'1', b'0', b'0', '', 8, 2, '角色管理');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`,
                              `order_num`, `remark`)
VALUES (11, '权限管理', '/permission/tolist', b'1', b'0', b'0', '', 8, 3, '权限管理');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`,
                              `order_num`, `remark`)
VALUES (12, '系统设置', '', b'1', b'0', b'0', 'layui-icon-set', 0, 8, '系统设置');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`,
                              `order_num`, `remark`)
VALUES (13, '参数配置', '/params/tolist', b'1', b'0', b'0', '', 12, 1, '页面配置');
INSERT IGNORE INTO `sys_page`(`id`, `name`, `url`, `is_menu`, `is_default`, `is_blank`, `icon_class`, `parent_id`,
                              `order_num`, `remark`)
VALUES (14, '页面配置', '/page/tolist', b'1', b'0', b'0', '', 12, 2, '页面配置');



CREATE TABLE IF NOT EXISTS `sys_permission`
(
    `id`              BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sys_role_id`     BIGINT(20) UNSIGNED NOT NULL COMMENT 'sys_role的主键id',
    `sys_page_id`     BIGINT(20) UNSIGNED NOT NULL COMMENT 'sys_page的主键id',
    `can_insert`      TINYINT(1)          NOT NULL COMMENT '是否能新增(true:能; false:不能)',
    `can_delete`      TINYINT(1)          NOT NULL COMMENT '是否能删除(true:能; false:不能)',
    `can_update`      TINYINT(1)          NOT NULL COMMENT '是否能修改(true:能; false:不能)',
    `can_select`      TINYINT(1)          NOT NULL COMMENT '是否能读取(true:能; false:不能)',
    `row_create_time` DATETIME(3)         NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `row_update_time` DATETIME(3)         NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_sys_role_id_page_id` (`sys_role_id`, `sys_page_id`)
) ENGINE = InnoDB COMMENT ='用户权限信息';



CREATE TABLE IF NOT EXISTS `sys_role`
(
    `id`              BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`            VARCHAR(64)         NOT NULL COMMENT '角色名称',
    `super_admin`     TINYINT(1)          NOT NULL COMMENT '是否是超级管理员(1:是; 0:否)',
    `remark`          VARCHAR(256)        NOT NULL COMMENT '角色说明',
    `row_create_time` DATETIME(3)         NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `row_update_time` DATETIME(3)         NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_sys_role_name` (`name`)
) ENGINE = InnoDB COMMENT ='角色信息';
INSERT IGNORE INTO `sys_role`(`id`, `name`, `super_admin`, `remark`)
VALUES (1, '超级管理员', 1, '超级管理员, 拥有最高权限');



CREATE TABLE IF NOT EXISTS `t_error_log`
(
    `id`                BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `primary_key_value` VARCHAR(128)        NOT NULL DEFAULT '' COMMENT '主键值',
    `database_name`     VARCHAR(255)        NOT NULL DEFAULT '' COMMENT '数据库名称',
    `table_name`        VARCHAR(255)        NOT NULL DEFAULT '' COMMENT '数据库表名称',
    `index_name`        VARCHAR(128)        NOT NULL DEFAULT '' COMMENT '索引名称',
    `error_message`     VARCHAR(4000)       NOT NULL DEFAULT '' COMMENT '异常消息',
    `consumer_record`   VARCHAR(8000)       NOT NULL DEFAULT '' COMMENT 'kafka消费失败的原始内容',
    `topic_name`        VARCHAR(255)        NOT NULL COMMENT 'kafka消息所在的主题',
    `partition`         INT(10) UNSIGNED    NOT NULL COMMENT 'kakfa消息所在的主题分区',
    `offset`            BIGINT(20) UNSIGNED NOT NULL COMMENT 'kafka消息的偏移量',
    `is_resolved`       TINYINT(4) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否已解决(0: 否; 1:是)',
    `row_create_time`   DATETIME(3)         NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `row_update_time`   DATETIME(3)         NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_primary_key_value` (`primary_key_value`) USING BTREE,
    INDEX `idx_database_name` (`database_name`) USING BTREE,
    INDEX `idx_table_name` (`table_name`) USING BTREE,
    INDEX `idx_index_name` (`index_name`) USING BTREE,
    UNIQUE INDEX `idx_topic_name_partition_offset` (`topic_name`, `partition`, `offset`) USING BTREE
) ENGINE = INNODB COMMENT ='错误日志';



CREATE TABLE IF NOT EXISTS `t_property`
(
    `id`              BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`            VARCHAR(128)        NOT NULL DEFAULT '' COMMENT '配置项名称',
    `value`           VARCHAR(255)        NOT NULL DEFAULT '' COMMENT '配置项的值',
    `description`     VARCHAR(255)        NOT NULL DEFAULT '' COMMENT '配置项描述信息',
    `row_create_time` DATETIME(3)         NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `row_update_time` DATETIME(3)         NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `idx_name` (`name`) USING BTREE
) ENGINE = INNODB COMMENT ='db2es的配置项信息';
INSERT IGNORE INTO `t_property`(`id`, `name`, `value`, `description`)
VALUES (1, 'primary_key', 'id', '业务表的主键字段名');
INSERT IGNORE INTO `t_property`(`id`, `name`, `value`, `description`)
VALUES (2, 'row_create_time', 'row_create_time', '业务表的记录创建时间的字段名');
INSERT IGNORE INTO `t_property`(`id`, `name`, `value`, `description`)
VALUES (3, 'row_update_time', 'row_update_time', '业务表的记录最后一次修改时间的字段名');
INSERT IGNORE INTO `t_property`(`id`, `name`, `value`, `description`)
VALUES (4, 'db2es_admin_host', '127.0.0.1', 'db2es_admin的ip地址');
INSERT IGNORE INTO `t_property`(`id`, `name`, `value`, `description`)
VALUES (5, 'db2es_admin_port', '80', 'db2es_admin的端口');
INSERT IGNORE INTO `t_property`(`id`, `name`, `value`, `description`)
VALUES (6, 'ding_access_token', 'daeff0d6c93786fecdaab1443195310bf42ed4e2ddd31d3172fc12cff9b3793c',
        '钉钉机器人的access_token');
INSERT IGNORE INTO `t_property`(`id`, `name`, `value`, `description`)
VALUES (7, 'ding_secret', 'SECc0ba4dc9a6d0b2b5b7d7e814ead2d767f7f39cceee8439298135a937384154f4', '钉钉机器人的加签秘钥');
INSERT IGNORE INTO `t_property`(`id`, `name`, `value`, `description`)
VALUES (8, 'ding_mobiles', '', '钉钉机器人发送的对象(手机号), 如果有多个用逗号分隔(例如:18XXXXXXXXX,18XXXXXXXXX), 如果为空, 则发送全体成员');



CREATE TABLE IF NOT EXISTS `t_topic`
(
    `id`                 BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`               VARCHAR(128)        NOT NULL DEFAULT '' COMMENT '主题名称',
    `number_of_shards`   INT(10) UNSIGNED    NOT NULL COMMENT '主分片的个数',
    `number_of_replicas` INT(10) UNSIGNED    NOT NULL COMMENT '每个主分片的副本分片的个数',
    `refresh_interval`   VARCHAR(64)         NOT NULL DEFAULT '1s' COMMENT '数据刷盘的间隔时间',
    `alias_of_years`     INT(10) UNSIGNED    NOT NULL COMMENT '将多少年的索引归为同一个索引别名',
    `mapping`            TEXT                NOT NULL COMMENT '主题对应的ES索引的MAPPING',
    `description`        VARCHAR(255)        NOT NULL DEFAULT '' COMMENT '主题描述信息',
    `row_create_time`    DATETIME(3)         NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `row_update_time`    DATETIME(3)         NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `idx_name` (`name`) USING BTREE
) ENGINE = InnoDB COMMENT = '主题以及对应的ElasticSearch信息';



CREATE TABLE IF NOT EXISTS `t_topic_db2es`
(
    `id`              BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `db2es_id`        INT(10) UNSIGNED    NOT NULL COMMENT 'db2es_server的分布式id',
    `topic_id`        BIGINT(10) UNSIGNED NOT NULL COMMENT '数据表t_topic的主键id',
    `row_create_time` DATETIME(3)         NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `row_update_time` DATETIME(3)         NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `idx_topic_id` (`topic_id`) USING BTREE,
    INDEX `idx_db2es_id` (`db2es_id`) USING BTREE
) ENGINE = InnoDB COMMENT = 'db2es_server与主题之间的关系';