package org.wyyt.sql.tool.database;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.sql.tool.common.Constants;
import org.wyyt.sql.tool.common.Utils;
import org.wyyt.sql.tool.entity.dto.SysAdmin;
import org.wyyt.sql.tool.entity.dto.SysPermission;
import org.wyyt.sql.tool.entity.dto.SysRole;
import org.wyyt.sql.tool.entity.dto.SysSql;
import org.wyyt.sql.tool.entity.vo.AdminVo;
import org.wyyt.tool.db.CrudService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The service which providing common SQL statement functions
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Slf4j
@Service
public final class Db {
    private final CrudService crudService;

    public Db(final CrudService crudService) {
        this.crudService = crudService;
    }

    public final void deleteExpired() throws Exception {
        final Date now = new Date();
        final Date date = DateUtils.addDays(now, -3);
        this.crudService.execute("DELETE FROM `sys_sql` WHERE `create_time` < ?", date);
    }

    public final SysSql getSqlById(final Long id) throws Exception {
        return this.crudService.selectOne(SysSql.class, "SELECT s.*, a.`name` FROM `sys_sql` s LEFT OUTER JOIN `sys_admin` a ON s.sys_admin_id=a.id WHERE s.id=?", id);
    }

    public final void addSql(final SysSql sysSql) throws Exception {
        this.crudService.execute("INSERT INTO `sys_sql`(`sys_admin_id`, `ip`, `short_sql`, `logic_sql`, `fact_sql`, execution_time, `execution_duration`) VALUES (?,?,?,?,?,?,?)",
                sysSql.getSysAdminId(),
                sysSql.getIp(),
                sysSql.getShortSql(),
                sysSql.getLogicSql(),
                sysSql.getFactSql(),
                sysSql.getExecutionTime(),
                sysSql.getExecutionDuration());
    }

    public final SysSql getPreviousSql(final Long sysAdminId,
                                       final Long id) throws Exception {
        return this.crudService.selectOne(SysSql.class,
                "SELECT id,logic_sql FROM `sys_sql` WHERE `sys_admin_id`=? AND id<=(SELECT MAX(id)-? FROM `sys_sql` WHERE `sys_admin_id`=1) ORDER BY id DESC LIMIT 1",
                sysAdminId, id);
    }

    public final SysSql getNextSql(final Long sysAdminId,
                                   final Long id) throws Exception {
        return this.crudService.selectOne(SysSql.class,
                "SELECT id,logic_sql FROM `sys_sql` WHERE `sys_admin_id`=? AND id>(SELECT MAX(id)-? FROM `sys_sql` WHERE `sys_admin_id`=1) ORDER BY id ASC LIMIT 1",
                sysAdminId,
                id);
    }


    public final IPage<SysSql> listSql(final Integer pageNum,
                                       final Integer pageSize,
                                       final Long sysAdminId,
                                       final String ip,
                                       final Long fromExecutionTime,
                                       final Long toExecutionTime) throws Exception {
        final IPage<SysSql> result = new Page<>();
        final StringBuilder strWhere = new StringBuilder();
        final List<Object> params = new ArrayList<>();
        if (null != sysAdminId) {
            strWhere.append(" AND s.sys_admin_id=?");
            params.add(sysAdminId);
        }
        if (!ObjectUtils.isEmpty(ip)) {
            strWhere.append(" AND s.ip=?");
            params.add(ip);
        }
        if (null != fromExecutionTime) {
            strWhere.append(" AND s.execution_duration >= ?");
            params.add(fromExecutionTime);
        }
        if (null != toExecutionTime) {
            strWhere.append(" AND s.execution_duration <= ?");
            params.add(toExecutionTime);
        }

        final List<SysSql> sysSqls = this.crudService.select(SysSql.class,
                String.format("SELECT s.id,s.sys_admin_id,a.`name`,s.ip,s.short_sql,s.execution_time,s.execution_duration FROM `sys_sql` s LEFT OUTER JOIN `sys_admin` a ON s.sys_admin_id=a.id WHERE 1=1 %s ORDER BY s.create_time DESC %s", strWhere.toString(), limit(pageNum, pageSize)),
                params.toArray(new Object[]{}));
        final long rowCount = this.crudService.executeScalar(Long.class, String.format("SELECT COUNT(*) FROM `sys_sql` s WHERE 1=1 %s", strWhere.toString()), params.toArray(new Object[]{}));
        result.setTotal(rowCount);
        result.setRecords(sysSqls);
        return result;
    }

    public final List<SysPermission> listPermission(final Integer pageNum,
                                                    final Integer pageSize,
                                                    final Long sysRoleId,
                                                    final String tableName) throws Exception {
        final List<Object> params = new ArrayList<>();
        final StringBuilder strWhere = new StringBuilder();
        if (null != sysRoleId) {
            strWhere.append(" AND p.`sys_role_id`=?");
            params.add(sysRoleId);
        }
        if (!ObjectUtils.isEmpty(tableName)) {
            strWhere.append(" AND p.`table_name`=?");
            params.add(tableName);
        }

        return this.crudService.select(SysPermission.class,
                String.format("SELECT p.*,r.`name` AS `role_name` FROM `sys_permission` p LEFT OUTER JOIN `sys_role` r ON p.sys_role_id=r.id WHERE 1=1 %s %s", strWhere.toString(), limit(pageNum, pageSize)), params.toArray(new Object[]{}));
    }

    public final SysPermission getPermissionByRoleIdAndTableName(final Long sysRoleId,
                                                                 final String tableName) throws Exception {
        return this.crudService.selectOne(SysPermission.class,
                "SELECT * FROM `sys_permission` WHERE `sys_role_id`=? AND `table_name`=?",
                sysRoleId,
                tableName);
    }

    public final void addPermission(final SysPermission sysPermission) throws Exception {
        this.crudService.execute("INSERT INTO `sys_permission`(`sys_role_id`, `table_name`, `can_insert`, `can_delete`, `can_update`, `can_select`) VALUES (?,?,?,?,?,?)",
                sysPermission.getSysRoleId(),
                sysPermission.getTableName(),
                sysPermission.getCanInsert(),
                sysPermission.getCanDelete(),
                sysPermission.getCanUpdate(),
                sysPermission.getCanSelect());
    }

    public final SysPermission getPermissionById(final Long id) throws Exception {
        return this.crudService.selectOne(SysPermission.class,
                "SELECT * FROM `sys_permission` WHERE id=?",
                id);
    }

    public final void updatePermissionById(final SysPermission sysPermission) throws Exception {
        this.crudService.execute("UPDATE `sys_permission` SET `sys_role_id`=?,`table_name`=?,`can_insert`=?,`can_delete`=?,`can_update`=?,`can_select`=? WHERE `id`=?",
                sysPermission.getSysRoleId(),
                sysPermission.getTableName(),
                sysPermission.getCanInsert(),
                sysPermission.getCanDelete(),
                sysPermission.getCanUpdate(),
                sysPermission.getCanSelect(),
                sysPermission.getId());
    }

    public final void removePermissionByIds(final List<Long> idsList) throws Exception {
        this.crudService.execute(String.format("DELETE FROM `sys_permission` WHERE id IN (%s)", StringUtils.join(idsList, ",")));
    }

    public final List<SysRole> listRole() throws Exception {
        return this.crudService.select(SysRole.class, "SELECT * FROM `sys_role` ORDER BY `create_time` ASC");
    }

    public final IPage<SysRole> listRole(final Integer pageNum,
                                         final Integer pageSize,
                                         final String name) throws Exception {
        final IPage<SysRole> result = new Page<>();
        List<SysRole> sysRoleList;
        Long total;
        if (ObjectUtils.isEmpty(name)) {
            sysRoleList = this.crudService.select(SysRole.class, String.format("SELECT * FROM `sys_role` ORDER BY `create_time` ASC %s", limit(pageNum, pageSize)));
            total = this.crudService.executeScalar(Long.class, "SELECT COUNT(*) FROM `sys_role`");
        } else {
            Object[] params = new Object[]{String.format("%%%s%%", name)};
            sysRoleList = this.crudService.select(SysRole.class, String.format("SELECT * FROM `sys_role` WHERE `name` LIKE ? ORDER BY `create_time` ASC %s", limit(pageNum, pageSize)), params);
            total = this.crudService.executeScalar(Long.class, "SELECT COUNT(*) FROM `sys_role` WHERE `name` LIKE ?", params);
        }
        final List<SysRole> adminVoList = Utils.toVoList(sysRoleList, SysRole.class);
        result.setTotal(total);
        result.setRecords(adminVoList);
        return result;
    }

    public final SysRole getRoleById(final Long id) throws Exception {
        return this.crudService.selectOne(SysRole.class, "SELECT * FROM `sys_role` WHERE `id`=?", id);
    }

    public final void addRole(final String name,
                              final boolean superAdmin,
                              final String remark) throws Exception {
        this.crudService.execute("INSERT INTO `sys_role`(`name`, `super_admin`, `remark`) VALUES (?,?,?)",
                name,
                superAdmin,
                remark);
    }

    public final void editRole(final Long id,
                               final String name,
                               final boolean superAdmin,
                               final String remark) throws Exception {
        this.crudService.execute("UPDATE `sys_role` SET `name`=?,`super_admin`=?,`remark`=? WHERE `id`=?",
                name,
                superAdmin,
                remark,
                id);
    }

    public final void removeRoleById(final Long id) throws Exception {
        this.crudService.execute("DELETE FROM `sys_role` WHERE `id`=?", id);
    }

    public final SysAdmin getAdminById(final Long id) throws Exception {
        return this.crudService.selectOne(SysAdmin.class, "SELECT * FROM `sys_admin` WHERE `id`=?", id);
    }

    public final SysAdmin getAdminByUsernameAndPassword(final String username,
                                                        final String password) throws Exception {
        return this.crudService.selectOne(SysAdmin.class, "SELECT * FROM `sys_admin` WHERE `username`=? AND `password`=?", username, Utils.hash(password));
    }

    public final IPage<AdminVo> listAdmin(final Integer pageNum,
                                          final Integer pageSize,
                                          final String name) throws Exception {
        final IPage<AdminVo> result = new Page<>();
        List<SysAdmin> sysAdminList;
        Long total;
        if (ObjectUtils.isEmpty(name)) {
            sysAdminList = this.crudService.select(SysAdmin.class, String.format("SELECT a.*, r.`name` AS role_name FROM `sys_admin` a INNER JOIN `sys_role` r ON a.sys_role_id=r.id ORDER BY a.`create_time` ASC %s", limit(pageNum, pageSize)));
            total = this.crudService.executeScalar(Long.class, "SELECT COUNT(*) FROM `sys_admin`");
        } else {
            final Object[] params = new Object[]{String.format("%%%s%%", name)};
            sysAdminList = this.crudService.select(SysAdmin.class, String.format("SELECT a.*,r.`name` AS role_name FROM `sys_admin` a INNER JOIN `sys_role` r ON a.sys_role_id=r.id WHERE a.`name` LIKE ? ORDER BY a.`create_time` ASC %s", limit(pageNum, pageSize)), params);
            total = this.crudService.executeScalar(Long.class, "SELECT COUNT(*) FROM `sys_admin` WHERE `name` LIKE ?", params);
        }
        final List<AdminVo> adminVoList = Utils.toVoList(sysAdminList, AdminVo.class);
        result.setTotal(total);
        result.setRecords(adminVoList);
        return result;
    }


    public final List<SysAdmin> listAdmin() throws Exception {
        return crudService.select(SysAdmin.class, "SELECT * FROM `sys_admin` ORDER BY create_time ASC");
    }

    public final List<SysAdmin> getAdminByRoleId(final Long roleId) throws Exception {
        return crudService.select(SysAdmin.class, "SELECT * FROM `sys_admin` WHERE `sys_role_id`=?", roleId);
    }

    public final void addAdmin(final Long roleId,
                               final String username,
                               final String password,
                               final String name,
                               final String phoneNumber,
                               final String email,
                               final String remark) throws Exception {
        this.crudService.execute("INSERT INTO `sys_admin`(`sys_role_id`, `username`, `password`, `name`, `phone_number`, `email`, `remark`) VALUES (?,?,?,?,?,?,?)",
                roleId,
                username,
                Utils.hash(password),
                name,
                phoneNumber,
                email,
                remark);
    }

    public final void editAdmin(final Long id,
                                final Long roleId,
                                final String username,
                                final String name,
                                final String phoneNumber,
                                final String email,
                                final String remark) throws Exception {
        this.crudService.execute("UPDATE `sys_admin` SET `sys_role_id`=?,`username`=?,`name`=?,`phoneNumber`=?,`email`=?,`remark`=? WHERE `id`=?",
                roleId,
                username,
                name,
                phoneNumber,
                email,
                remark,
                id);
    }

    public final void resetPassword(final Long id) throws Exception {
        this.crudService.execute("UPDATE `sys_admin` SET `password`=? WHERE `id`=?",
                Constants.DEFAULT_ADMIN_PASSWORD,
                id);
    }

    public final void removeAdminById(final Long id) throws Exception {
        this.crudService.execute("DELETE FROM `sys_admin` WHERE id = ?", id);
    }

    private String limit(final Integer pageNum,
                         final Integer pageSize) {
        return String.format("LIMIT %s,%s", (pageNum - 1) * pageSize, pageSize);
    }
}