package org.wyyt.sharding.sqltool.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.wyyt.sharding.anno.TranRead;
import org.wyyt.sharding.anno.TranSave;
import org.wyyt.sharding.context.DbContext;
import org.wyyt.sharding.entity.FactSql;
import org.wyyt.sharding.exception.ShardingException;
import org.wyyt.sharding.service.RewriteService;
import org.wyyt.sharding.service.ShardingService;
import org.wyyt.sharding.sqltool.database.Db;
import org.wyyt.sharding.sqltool.entity.dto.SysSql;
import org.wyyt.sharding.sqltool.entity.vo.AdminVo;
import org.wyyt.sharding.sqltool.entity.vo.DataTableVo;
import org.wyyt.sharding.sqltool.mapper.DbMapper;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.sql.SqlTool;

import java.util.*;

/**
 * the service which providing the ability of execute SQL statement
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
@Service
public class SqlService {
    private final Db db;
    private final DbMapper dbMapper;
    private final RewriteService rewriteService;
    private final ShardingService shardingService;

    public SqlService(final Db db,
                      final DbMapper dbMapper,
                      final RewriteService rewriteService,
                      final ShardingService shardingService) {
        this.db = db;
        this.dbMapper = dbMapper;
        this.rewriteService = rewriteService;
        this.shardingService = shardingService;
    }

    public final List<DataTableVo> exec(final String ip,
                                        final AdminVo adminVo,
                                        String sql,
                                        final Integer limit) {
        sql = sql.replaceAll("\\n", " ")
                .replaceAll("\\r", " ")
                .replaceAll("\\t", " ")
                .replaceAll(" +", " ")
                .trim();

        if (StrUtil.isBlank(sql)) {
            return null;
        }
        final String[] sqls = sql.split(";");
        final List<DataTableVo> result = new ArrayList<>(sqls.length);
        for (String singleSql : sqls) {
            if (null == singleSql || StrUtil.isBlank(singleSql)) {
                continue;
            }
            final List<SQLStatement> sqlStatements = SqlTool.analyseMySql(singleSql);
            singleSql = sqlStatements.get(0).toString().replaceAll("\\n", " ").trim();
            final DataTableVo dataTableVo = doExec(ip, adminVo, singleSql, limit);
            if (null != dataTableVo) {
                result.add(dataTableVo);
            }
        }
        return result;
    }

    private DataTableVo doExec(final String ip,
                               final AdminVo adminVo,
                               final String singleSql,
                               final Integer limit) {
        try {
            if (singleSql.toLowerCase().startsWith("select")) {
                return this.select(ip, adminVo, singleSql, limit);
            } else if (singleSql.toLowerCase().startsWith("insert")) {
                return this.insert(ip, adminVo, singleSql);
            } else if (singleSql.toLowerCase().startsWith("update")) {
                return this.update(ip, adminVo, singleSql);
            } else if (singleSql.toLowerCase().startsWith("delete")) {
                return this.delete(ip, adminVo, singleSql);
            }
            throw new ShardingException("检测到不支持的SQL语句");
        } catch (final Exception e) {
            log.error(String.format("SqlService: run SQL meet error with %s", ExceptionTool.getRootCauseMessage(e)), e);
            return exception(e);
        }
    }

    @TranRead
    public DataTableVo select(final String ip,
                              final AdminVo adminVo,
                              String singleSql,
                              final Integer limit) throws Exception {
        if (!adminVo.getRole().getSuperAdmin()) {
            final List<SQLStatement> sqlStatementList = SqlTool.analyseMySql(singleSql);
            final SQLSelectStatement sqlSelectStatement = (SQLSelectStatement) sqlStatementList.get(0);
            final String tableName = sqlSelectStatement.getSelect().getQueryBlock().getFrom().toString();
        }
        final DataTableVo result = new DataTableVo();
        final List<String> columnNameList = new ArrayList<>();
        final List<List<String>> valueList = new ArrayList<>();
        if (limit > 0) {
            singleSql = singleSql.trim();
            if (!singleSql.toUpperCase().contains(" LIMIT ")) {
                if (singleSql.lastIndexOf(";") == singleSql.length() - 1) {
                    singleSql = singleSql.substring(0, singleSql.length() - 1);
                }
                singleSql = singleSql + " LIMIT " + limit;
            }
        }

        final Date start = new Date();
        final List<LinkedHashMap<String, Object>> queryResult = this.dbMapper.select(singleSql);
        final Date end = new Date();

        int rowIndex = 1;
        for (final Map<String, Object> resultMap : queryResult) {
            if (null == resultMap) {
                continue;
            }
            final List<String> rows = new ArrayList<>(resultMap.size());

            for (final Map.Entry<String, Object> pair : resultMap.entrySet()) {
                if (rowIndex == 1) {
                    columnNameList.add(pair.getKey());
                }
                rows.add(pair.getValue().toString());
            }
            valueList.add(rows);
            rowIndex++;
        }

        result.setGoodSql(true);
        result.setFactSql(this.rewriteService.rewriteSql(singleSql));
        result.setColumnNameList(columnNameList);
        result.setValueList(valueList);
        result.setExecTime(end.getTime() - start.getTime());
        addAudit(ip, start, adminVo, result, singleSql);
        return result;
    }

    @TranSave
    public DataTableVo insert(final String ip,
                              final AdminVo adminVo,
                              final String singleSql) throws Exception {
        if (!adminVo.getRole().getSuperAdmin()) {
            final List<SQLStatement> sqlStatementList = SqlTool.analyseMySql(singleSql);
            final MySqlInsertStatement insertStatement = (MySqlInsertStatement) sqlStatementList.get(0);
            final String tableName = insertStatement.getTableName().getSimpleName();
        }
        final DataTableVo result = new DataTableVo();
        final List<String> columnNameList = new ArrayList<>();
        final List<List<String>> valueList = new ArrayList<>();

        DbContext.set(this.shardingService.getPrimaryDimension().getName());
        final Date start = new Date();
        final Integer rows = this.dbMapper.insert(singleSql);
        final Date end = new Date();
        DbContext.clear();

        columnNameList.add("result");
        valueList.add(Collections.singletonList(String.format("%s行被新增", rows)));

        result.setGoodSql(true);
        result.setFactSql(this.rewriteService.rewriteSql(singleSql));
        result.setColumnNameList(columnNameList);
        result.setValueList(valueList);
        result.setExecTime(end.getTime() - start.getTime());
        this.addAudit(ip, start, adminVo, result, singleSql);
        return result;
    }

    @TranSave
    public DataTableVo update(final String ip,
                              final AdminVo adminVo,
                              final String singleSql) throws Exception {
        if (!adminVo.getRole().getSuperAdmin()) {
            final List<SQLStatement> sqlStatementList = SqlTool.analyseMySql(singleSql);
            final MySqlUpdateStatement updateStatement = (MySqlUpdateStatement) sqlStatementList.get(0);
            final String tableName = updateStatement.getTableName().getSimpleName();
        }

        final DataTableVo result = new DataTableVo();
        final List<String> columnNameList = new ArrayList<>();
        final List<List<String>> valueList = new ArrayList<>();

        DbContext.set(this.shardingService.getPrimaryDimension().getName());
        final Date start = new Date();
        final Integer rows = this.dbMapper.update(singleSql);
        final Date end = new Date();
        DbContext.clear();

        columnNameList.add("result");
        valueList.add(Collections.singletonList(String.format("%s行被修改", rows)));

        result.setGoodSql(true);
        result.setFactSql(this.rewriteService.rewriteSql(singleSql));
        result.setColumnNameList(columnNameList);
        result.setValueList(valueList);
        result.setExecTime(end.getTime() - start.getTime());
        this.addAudit(ip, start, adminVo, result, singleSql);
        return result;
    }

    @TranSave
    public DataTableVo delete(final String ip,
                              final AdminVo adminVo,
                              final String singleSql) throws Exception {
        if (!adminVo.getRole().getSuperAdmin()) {
            final List<SQLStatement> sqlStatementList = SqlTool.analyseMySql(singleSql);
            final MySqlDeleteStatement mySqlDeleteStatement = (MySqlDeleteStatement) sqlStatementList.get(0);
            final String tableName = mySqlDeleteStatement.getTableName().getSimpleName();
        }

        final DataTableVo result = new DataTableVo();
        final List<String> columnNameList = new ArrayList<>();
        final List<List<String>> valueList = new ArrayList<>();

        DbContext.set(this.shardingService.getPrimaryDimension().getName());
        final Date start = new Date();
        final Integer rows = this.dbMapper.delete(singleSql);
        final Date end = new Date();
        DbContext.clear();

        columnNameList.add("result");
        valueList.add(Collections.singletonList(String.format("%s行被删除", rows)));

        result.setGoodSql(true);
        result.setFactSql(this.rewriteService.rewriteSql(singleSql));
        result.setColumnNameList(columnNameList);
        result.setValueList(valueList);
        result.setExecTime(end.getTime() - start.getTime());
        this.addAudit(ip, start, adminVo, result, singleSql);
        return result;
    }

    private void addAudit(final String ip,
                          final Date start,
                          final AdminVo adminVo,
                          final DataTableVo dataTableVo,
                          final String logicSql) throws Exception {
        final SysSql sysSql = new SysSql();
        sysSql.setSysAdminId(adminVo.getId());
        sysSql.setIp(ip);
        sysSql.setShortSql((logicSql.length() > 128) ? logicSql.substring(0, 124).concat("...") : logicSql);
        sysSql.setLogicSql(logicSql);

        final List<String> factSqlList = new ArrayList<>();
        for (final FactSql factSql : dataTableVo.getFactSql()) {
            factSqlList.add(String.format("%s:%s", factSql.getDatasourceName(), factSql.getSql()));
        }
        sysSql.setFactSql(StringUtils.join(factSqlList, ";"));
        sysSql.setExecutionTime(start);
        sysSql.setExecutionDuration(dataTableVo.getExecTime());
        this.db.addSql(sysSql);
    }

    public DataTableVo exception(final Exception exception) {
        final DataTableVo result = new DataTableVo();
        final List<String> columnNameList = new ArrayList<>();
        final List<List<String>> valueList = new ArrayList<>();

        columnNameList.add("result");
        result.setGoodSql(false);
        valueList.add(Collections.singletonList(toVoString(ExceptionTool.getRootCauseMessage(exception))));
        result.setGoodSql(false);
        result.setColumnNameList(columnNameList);
        result.setValueList(valueList);
        result.setExecTime(0L);
        return result;
    }

    private String toVoString(final String value) {
        return value.replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
    }
}