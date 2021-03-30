CREATE TABLE IF NOT EXISTS `sys_admin`  (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sys_role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色id(sys_role表的主键)',
  `username` VARCHAR(64) NOT NULL COMMENT '管理员的登陆用户名',
  `password` VARCHAR(128) NOT NULL COMMENT '管理员的登陆密码',
  `name` VARCHAR(64) NOT NULL COMMENT '管理员姓名',
  `phone_number` VARCHAR(64) NOT NULL COMMENT '管理员手机号码',
  `email` VARCHAR(128)  NOT NULL COMMENT '管理员邮箱',
  `remark` VARCHAR(255) NOT NULL COMMENT '管理员备注信息',
  `row_create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `row_update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_sys_admin_username`(`username`),
  INDEX `idx_sys_admin_sys_role_id`(`sys_role_id`)
) COMMENT='用户信息';

CREATE TABLE IF NOT EXISTS `sys_permission`  (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sys_role_id` BIGINT UNSIGNED NOT NULL COMMENT 'sys_role的主键id',
  `table_name` VARCHAR(64)  NOT NULL COMMENT '表名',
  `can_insert` TINYINT(1) NOT NULL COMMENT '是否能新增(true:能; false:不能)',
  `can_delete` TINYINT(1) NOT NULL COMMENT '是否能删除(true:能; false:不能)',
  `can_update` TINYINT(1) NOT NULL COMMENT '是否能修改(true:能; false:不能)',
  `can_select` TINYINT(1) NOT NULL COMMENT '是否能读取(true:能; false:不能)',
  `row_create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `row_update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_sys_role_id_table_name`(`sys_role_id`, `table_name`)
) COMMENT='用户权限信息';

CREATE TABLE IF NOT EXISTS `sys_role`  (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(64) NOT NULL COMMENT '角色名称',
  `super_admin` TINYINT(1) NOT NULL COMMENT '是否是超级管理员(1:是; 0:否)',
  `remark` VARCHAR(256) NOT NULL COMMENT '角色说明',
  `row_create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `row_update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `idx_sys_role_name`(`name`)
) COMMENT='角色信息';

CREATE TABLE IF NOT EXISTS `sys_sql`  (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sys_admin_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '管理员id(sys_admin的主键id)',
  `ip` VARCHAR(64) NOT NULL COMMENT '执行的机器ip',
  `short_sql` VARCHAR(128) NOT NULL COMMENT '完整SQL的前128个字符',
  `logic_sql` TEXT NOT NULL COMMENT '逻辑SQL',
  `fact_sql` TEXT  NOT NULL COMMENT '实际SQL',
  `execution_time` DATETIME(0) NOT NULL COMMENT '执行SQL的时间',
  `execution_duration` BIGINT UNSIGNED NOT NULL COMMENT 'SQL执行的时间, 单位:毫秒',
  `row_create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `row_update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
  PRIMARY KEY (`id`),
  INDEX `idx_sys_admin_id`(`sys_admin_id`),
  INDEX `idx__ip`(`ip`) ,
  INDEX `idx_execution_duration`(`execution_duration`)
) COMMENT='sql信息';

INSERT IGNORE INTO `sys_admin`(`id`, `sys_role_id`, `username`, `password`, `name`, `phone_number`, `email`, `remark`) VALUES (1, 1, 'admin', 'ebc255e6a0c6711a4366bc99ebafb54f', '超级管理员', '18000000000', 'administrator@sjb.com', '超级管理员');
INSERT IGNORE INTO `sys_role`(`id`, `name`, `super_admin`, `remark`) VALUES (1, '超级管理员', 1, '超级管理员, 拥有最高权限');
INSERT IGNORE INTO `sys_role`(`id`, `name`, `super_admin`, `remark`) VALUES (2, '研发人员', 0, '研发人员');