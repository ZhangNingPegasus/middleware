package org.wyyt.sharding.interceptor.plugin;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.wyyt.sharding.constant.Name;
import org.wyyt.sharding.entity.AnalyseResult;
import org.wyyt.sharding.exception.ShardingException;
import org.wyyt.tool.sql.SqlTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The adapter of the interface MybatisInterceptor
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public abstract class BaseMybatisInterceptorAdapter implements MybatisInterceptor, InitializingBean, DisposableBean {
    private Map<String, Object> parameters;

    protected AnalyseResult analyseSql(final Invocation invocation) throws Exception {
        final AnalyseResult result = new AnalyseResult();

        final MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        final SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        final String sql = SqlTool.getSql(invocation);
        final List<SQLStatement> sqlStatementList = SqlTool.analyseMySql(sql);

        final List<String> tableNameList = new ArrayList<>(sqlStatementList.size());
        final List<CaseInsensitiveMap<String, Object>> updateValueMapList = new ArrayList<>(64);
        final List<CaseInsensitiveMap<String, List<Object>>> whereValueMapList = new ArrayList<>(64);
        final List<CaseInsensitiveMap<String, Object>> insertValueMapList = new ArrayList<>(64);
        final List<Object> primaryKeyValueList = new ArrayList<>(64);

        result.setSql(sql);
        result.setSqlStatementList(sqlStatementList);
        result.setSqlCommandType(sqlCommandType);
        result.setTableNameList(tableNameList);
        result.setUpdateValueMapList(updateValueMapList);
        result.setWhereValueMapList(whereValueMapList);
        result.setInsertValueMapList(insertValueMapList);
        result.setPrimaryKeyValueList(primaryKeyValueList);

        if (StrUtil.isBlank(sql)) {
            return result;
        }

        switch (sqlCommandType) {
            case INSERT:
                for (final SQLStatement sqlStatement : sqlStatementList) {
                    //获取表名
                    final MySqlInsertStatement insertStatement = (MySqlInsertStatement) sqlStatement;
                    final String logicTableName = SqlTool.removeMySqlQualifier(insertStatement.getTableName().getSimpleName());
                    tableNameList.add(logicTableName.trim());

                    //获取INSERT的字段和值
                    final List<SQLExpr> columns = insertStatement.getColumns();
                    final List<SQLInsertStatement.ValuesClause> valuesList = insertStatement.getValuesList();

                    for (final SQLInsertStatement.ValuesClause valuesClause : valuesList) {
                        final CaseInsensitiveMap<String, Object> insertValue = new CaseInsensitiveMap<>();
                        for (int i = 0; i < columns.size(); i++) {
                            final String columnName = SqlTool.removeMySqlQualifier(columns.get(i).toString());
                            if (Name.FIELD_PRIMARY_KEY.equalsIgnoreCase(columnName)) {
                                primaryKeyValueList.add(SqlTool.getValueBySqlExpr(valuesClause.getValues().get(i)));
                            } else {
                                insertValue.put(columnName, SqlTool.getValueBySqlExpr(valuesClause.getValues().get(i)));
                            }
                        }
                        insertValueMapList.add(insertValue);
                    }
                }
                break;
            case DELETE:
                for (final SQLStatement sqlStatement : sqlStatementList) {
                    //获取表名
                    final MySqlDeleteStatement mySqlDeleteStatement = (MySqlDeleteStatement) sqlStatement;
                    final String logicTableName = SqlTool.removeMySqlQualifier(mySqlDeleteStatement.getTableName().getSimpleName());
                    tableNameList.add(logicTableName.trim());

                    //获取WHERE条件
                    final CaseInsensitiveMap<String, List<Object>> whereMap = new CaseInsensitiveMap<>();
                    SqlTool.getWhereFieldAndValue(mySqlDeleteStatement.getWhere(), whereMap);
                    whereValueMapList.add(whereMap);
                }
                break;
            case UPDATE:
                for (final SQLStatement sqlStatement : sqlStatementList) {
                    //获取表名
                    final MySqlUpdateStatement updateStatement = (MySqlUpdateStatement) sqlStatement;
                    final String logicTableName = SqlTool.removeMySqlQualifier(updateStatement.getTableName().getSimpleName());
                    tableNameList.add(logicTableName.trim());

                    //获取SET条件
                    final CaseInsensitiveMap<String, Object> updateValueMap = new CaseInsensitiveMap<>();
                    for (final SQLUpdateSetItem item : updateStatement.getItems()) {
                        updateValueMap.put(SqlTool.removeMySqlQualifier(item.getColumn().toString()), SqlTool.getValueBySqlExpr(item.getValue()));
                    }
                    updateValueMapList.add(updateValueMap);

                    //获取WHERE语句
                    final CaseInsensitiveMap<String, List<Object>> whereMap = new CaseInsensitiveMap<>();
                    SqlTool.getWhereFieldAndValue(updateStatement.getWhere(), whereMap);
                    whereValueMapList.add(whereMap);
                }
                break;
            case SELECT:
                for (final SQLStatement sqlStatement : sqlStatementList) {
                    final SQLSelectStatement sqlSelectStatement = (SQLSelectStatement) sqlStatement;
                    final SQLSelectQuery sqlSelectQuery = sqlSelectStatement.getSelect().getQuery();
                    final MySqlSelectQueryBlock mySqlSelectQueryBlock = (MySqlSelectQueryBlock) sqlSelectQuery;
                    //获取表名
                    final String logicTableName = SqlTool.removeMySqlQualifier(getTableName(mySqlSelectQueryBlock.getFrom()));
                    tableNameList.add(logicTableName.trim());
                    //获取WHERE语句
                    final CaseInsensitiveMap<String, List<Object>> whereMap = new CaseInsensitiveMap<>();
                    SqlTool.getWhereFieldAndValue(mySqlSelectQueryBlock.getWhere(), whereMap);
                    whereValueMapList.add(whereMap);
                }
                break;
            default:
                throw new ShardingException("操作类型未知");
        }

        for (final Map<String, List<Object>> pair : whereValueMapList) {
            final List<Object> pkValueList = pair.get(Name.FIELD_PRIMARY_KEY);
            if (pkValueList != null && !pkValueList.isEmpty()) {
                primaryKeyValueList.addAll(pkValueList);
                pair.remove(Name.FIELD_PRIMARY_KEY);
            }
        }
        return result;
    }

    @Override
    public void afterPropertiesSet() {
        parameters = new HashMap<>();
    }

    @Override
    public void destroy() {
        clearParameter();
    }

    @Override
    public void setParameter(final String name, final Object value) {
        this.parameters.put(name, value);
    }

    @Override
    public void clearParameter() {
        this.parameters.clear();
    }

    @Override
    public <T> T getParameter(final String name) {
        Object result = this.parameters.get(name);
        if (result == null) {
            return null;
        }
        return (T) result;
    }

    @Override
    public Object[] before(final Invocation invocation, final Object[] variables) throws Exception {
        return variables;
    }

    @Override
    public Object[] success(final Invocation invocation, final Object result, final Object[] variables) {
        return variables;
    }

    @Override
    public Object[] failure(final Throwable e, final Invocation invocation, final Object[] variables) {
        return variables;
    }

    @Override
    public Object[] complete(final Invocation invocation, final Object[] variables) {
        return variables;
    }

    @Override
    public boolean enabled(final Invocation invocation) {
        return true;
    }

    private static String getTableName(SQLTableSource sqlTableSource) {
        if (sqlTableSource instanceof SQLExprTableSource) {
            return ((SQLExprTableSource) sqlTableSource).getName().getSimpleName();
        }
        return sqlTableSource.toString();
    }
}