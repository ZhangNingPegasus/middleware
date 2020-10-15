CREATE TABLE IF NOT EXISTS `sys_admin`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sys_role_id` bigint(20) UNSIGNED NOT NULL COMMENT '角色id(sys_role表的主键)',
  `username` varchar(64) NOT NULL COMMENT '管理员的登陆用户名',
  `password` varchar(128) NOT NULL COMMENT '管理员的登陆密码',
  `name` varchar(64) NOT NULL COMMENT '管理员姓名',
  `phone_number` varchar(64) NOT NULL COMMENT '管理员手机号码',
  `email` varchar(128)  NOT NULL COMMENT '管理员邮箱',
  `remark` varchar(255) NOT NULL COMMENT '管理员备注信息',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '记录创建时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_sys_admin_username`(`username`),
  INDEX `idx_sys_admin_sys_role_id`(`sys_role_id`)
) ENGINE = InnoDB  AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='用户信息';

CREATE TABLE IF NOT EXISTS `sys_permission`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sys_role_id` bigint(20) UNSIGNED NOT NULL COMMENT 'sys_role的主键id',
  `table_name` varchar(64)  NOT NULL COMMENT '表名',
  `can_insert` tinyint(1) NOT NULL COMMENT '是否能新增(true:能; false:不能)',
  `can_delete` tinyint(1) NOT NULL COMMENT '是否能删除(true:能; false:不能)',
  `can_update` tinyint(1) NOT NULL COMMENT '是否能修改(true:能; false:不能)',
  `can_select` tinyint(1) NOT NULL COMMENT '是否能读取(true:能; false:不能)',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '记录创建时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_sys_role_id_table_name`(`sys_role_id`, `table_name`)
) ENGINE = InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='用户权限信息';

CREATE TABLE IF NOT EXISTS `sys_role`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) NOT NULL COMMENT '角色名称',
  `super_admin` tinyint(1) NOT NULL COMMENT '是否是超级管理员(1:是; 0:否)',
  `remark` varchar(256) NOT NULL COMMENT '角色说明',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '记录创建时间',
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `idx_sys_role_name`(`name`)
) ENGINE = InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='角色信息';

CREATE TABLE IF NOT EXISTS `sys_sql`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sys_admin_id` bigint(20) UNSIGNED NOT NULL COMMENT '管理员id(sys_admin的主键id)',
  `ip` varchar(64) NOT NULL COMMENT '执行的机器ip',
  `short_sql` varchar(128) NOT NULL COMMENT '完整SQL的前128个字符',
  `logic_sql` text NOT NULL COMMENT '逻辑SQL',
  `fact_sql` text  NOT NULL COMMENT '实际SQL',
  `execution_time` datetime(0) NOT NULL COMMENT '执行SQL的时间',
  `execution_duration` bigint(20) UNSIGNED NOT NULL COMMENT 'SQL执行的时间, 单位:毫秒',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '记录创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_sys_admin_id`(`sys_admin_id`),
  INDEX `idx__ip`(`ip`) ,
  INDEX `idx_execution_duration`(`execution_duration`)
) ENGINE = InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='sql信息';

INSERT IGNORE INTO `sys_admin`(`id`, `sys_role_id`, `username`, `password`, `name`, `phone_number`, `email`, `remark`) VALUES (1, 1, 'admin', 'ebc255e6a0c6711a4366bc99ebafb54f', '超级管理员', '18000000000', 'administrator@sjb.com', '超级管理员');
INSERT IGNORE INTO `sys_role`(`id`, `name`, `super_admin`, `remark`) VALUES (1, '超级管理员', 1, '超级管理员, 拥有最高权限');