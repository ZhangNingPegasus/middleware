package org.wyyt.sharding.sqltool.database;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.sharding.sqltool.entity.dto.SysSql;
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
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
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

    private String limit(final Integer pageNum,
                         final Integer pageSize) {
        return String.format("LIMIT %s,%s", (pageNum - 1) * pageSize, pageSize);
    }
}