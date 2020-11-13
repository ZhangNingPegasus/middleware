package org.wyyt.sharding.interceptor.plugin.impl;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.plugin.Invocation;
import org.springframework.util.ObjectUtils;
import org.wyyt.sharding.entity.AnalyseResult;
import org.wyyt.sharding.exception.ShardingException;
import org.wyyt.sharding.interceptor.plugin.BaseMybatisInterceptorAdapter;
import org.wyyt.sharding.service.ShardingService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wyyt.sharding.constant.Name.FIELD_ROW_CREATE_TIME;
import static org.wyyt.sharding.constant.Name.FIELD_ROW_UPDATE_TIME;

/**
 * Check whether the SQL statement after splitting database and table conforms to the specification
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public final class CheckSqlInterceptor extends BaseMybatisInterceptorAdapter {
    private final ShardingService shardingService;

    public CheckSqlInterceptor(final ShardingService shardingService) {
        this.shardingService = shardingService;
    }

    @Override
    public final Object[] before(final Invocation invocation,
                                 final Object[] variables) throws Exception {
        final AnalyseResult result = this.analyseSql(invocation);

        final String tableName = result.getTableNameList().get(0);
        //不需要分库分表的数据表不需要检查
        if (!this.shardingService.needSharding(tableName)) {
            return variables;
        }

        //逻辑表对应的主维度字段
        final String primaryDimensionShardingColumn = this.shardingService.getPrimaryDimensionShardingColumn(tableName);
        switch (result.getSqlCommandType()) {
            case INSERT:
                for (final CaseInsensitiveMap<String, Object> pair : result.getInsertValueMapList()) {
                    if (!pair.containsKey(primaryDimensionShardingColumn)) {
                        throw new ShardingException(String.format("缺少主维度拆分键[%s]的指定", primaryDimensionShardingColumn));
                    } else if (pair.containsKey(FIELD_ROW_CREATE_TIME)) {
                        throw new ShardingException(String.format("不支持对字段[%s]进行插入操作", FIELD_ROW_CREATE_TIME));
                    } else if (pair.containsKey(FIELD_ROW_UPDATE_TIME)) {
                        throw new ShardingException(String.format("不支持对字段[%s]进行插入操作", FIELD_ROW_UPDATE_TIME));
                    } else if (isBlank(pair.get(primaryDimensionShardingColumn))) {
                        throw new ShardingException(String.format("主维度拆分键[%s]的值不允许为空", primaryDimensionShardingColumn));
                    }
                }
                break;
            case DELETE:
                for (final SQLStatement sqlStatement : result.getSqlStatementList()) {
                    final MySqlDeleteStatement mySqlDeleteStatement = (MySqlDeleteStatement) sqlStatement;
                    for (final CaseInsensitiveMap<String, List<Object>> pair : result.getWhereValueMapList()) {
                        if (!pair.containsKey(primaryDimensionShardingColumn)
                                ||
                                (
                                        (mySqlDeleteStatement.getWhere().getClass().isAssignableFrom(SQLBinaryOpExpr.class) &&
                                                ((SQLBinaryOpExpr) mySqlDeleteStatement.getWhere()).getOperator() == SQLBinaryOperator.BooleanOr)
                                )
                        ) {
                            throw new ShardingException(String.format("WHERE子句中, 缺少主维度的拆分键[%s]或它被包含在OR中", primaryDimensionShardingColumn));
                        } else if (isBlank(pair.get(primaryDimensionShardingColumn))) {
                            throw new ShardingException(String.format("WHERE子句中, 主维度拆分键[%s]的值不允许为空", primaryDimensionShardingColumn));
                        }
                    }
                }
                break;
            case UPDATE:
                for (final SQLStatement sqlStatement : result.getSqlStatementList()) {
                    final MySqlUpdateStatement updateStatement = (MySqlUpdateStatement) sqlStatement;
                    for (final CaseInsensitiveMap<String, List<Object>> pair : result.getWhereValueMapList()) {
                        if (!pair.containsKey(primaryDimensionShardingColumn)
                                ||
                                (
                                        updateStatement.getWhere().getClass().isAssignableFrom(SQLBinaryOpExpr.class) &&
                                                ((SQLBinaryOpExpr) updateStatement.getWhere()).getOperator() == SQLBinaryOperator.BooleanOr
                                )
                        ) {
                            throw new ShardingException(String.format("WHERE子句中, 缺少主维度的拆分键[%s]或它被包含在OR中", primaryDimensionShardingColumn));
                        } else if (isBlank(pair.get(primaryDimensionShardingColumn))) {
                            throw new ShardingException(String.format("WHERE子句中, 主维度拆分键[%s]的值不允许为空", primaryDimensionShardingColumn));
                        }

                        final List<SQLUpdateSetItem> items = updateStatement.getItems();
                        for (final SQLUpdateSetItem item : items) {
                            if (FIELD_ROW_CREATE_TIME.equalsIgnoreCase(item.getColumn().toString())) {
                                throw new ShardingException(String.format("不支持对字段[%s]进行UPDATE操作", FIELD_ROW_CREATE_TIME));
                            } else if (FIELD_ROW_UPDATE_TIME.equalsIgnoreCase(item.getColumn().toString())) {
                                throw new ShardingException(String.format("不支持对字段[%s]进行UPDATE操作", FIELD_ROW_UPDATE_TIME));
                            }
                        }
                    }
                    final Set<String> shardingColumns = shardingService.listShardingColumns(tableName);
                    for (final String shardingColumn : shardingColumns) {
                        for (final CaseInsensitiveMap<String, Object> pair : result.getUpdateValueMapList()) {
                            if (pair.containsKey(shardingColumn)) {
                                throw new ShardingException(String.format("不允许对拆分键字段[%s]进行修改。可以替换成先删除，再新增", shardingColumn));
                            }
                        }
                    }
                }
                break;
            case SELECT:
                for (final SQLStatement sqlStatement : result.getSqlStatementList()) {
                    final SQLSelectStatement sqlSelectStatement = (SQLSelectStatement) sqlStatement;
                    final SQLSelectQuery sqlSelectQuery = sqlSelectStatement.getSelect().getQuery();
                    final MySqlSelectQueryBlock mySqlSelectQueryBlock = (MySqlSelectQueryBlock) sqlSelectQuery;

                    final Set<String> shardingColumns = this.shardingService.listShardingColumns(tableName);

                    for (final CaseInsensitiveMap<String, List<Object>> pair : result.getWhereValueMapList()) {
                        final String columnName = existsKeyOfList(pair.keySet(), shardingColumns);
                        final List<Object> objects = pair.get(columnName);
                        if (ObjectUtils.isEmpty(columnName) ||
                                (
                                        mySqlSelectQueryBlock.getWhere().getClass().isAssignableFrom(SQLBinaryOpExpr.class) &&
                                                ((SQLBinaryOpExpr) mySqlSelectQueryBlock.getWhere()).getOperator() == SQLBinaryOperator.BooleanOr
                                )
                        ) {
                            throw new ShardingException(String.format("WHERE子句中, 必须包含拆分键字段[%s]中的任意一个, 或它们被包含在OR中", StringUtils.join(shardingColumns, ", ")));
                        }
                        for (final Object object : objects) {
                            if (isBlank(object)) {
                                throw new ShardingException(String.format("主维度拆分键[%s]的值不允许为空", columnName));
                            }
                        }
                        if (objects.size() > 1) {
                            Map<Integer, Set<Object>> splitMap = this.shardingService.doDatabaseSharding(tableName, objects);
                            if (splitMap.size() > 1) {
                                throw new ShardingException(String.format("拆分键字段[%s]在IN中的值列表必须定位在同一数据源下", columnName));
                            }
                        }
                    }
                }
                break;
            default:
                throw new ShardingException("操作类型未知");
        }
        return variables;
    }

    private String existsKeyOfList(final Set<String> mapKey,
                                   final Set<String> valueList) {
        for (final String value : valueList) {
            if (mapKey.contains(value)) {
                return value;
            }
        }
        return null;
    }

    @Override
    public final int order() {
        return 0;
    }

    private static boolean isBlank(Object value) {
        if (null == value) {
            return true;
        } else if (value instanceof Collection) {
            for (final Object o : (Collection) value) {
                if (isBlank(o)) {
                    return true;
                }
            }
        }
        return ObjectUtils.isEmpty(value.toString());
    }
}