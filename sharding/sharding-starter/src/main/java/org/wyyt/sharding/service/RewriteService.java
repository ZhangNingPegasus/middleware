package org.wyyt.sharding.service;


import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.ShardingRuntimeContext;
import org.apache.shardingsphere.underlying.executor.context.ExecutionContext;
import org.apache.shardingsphere.underlying.pluggble.prepare.PreparedQueryPrepareEngine;
import org.wyyt.sharding.entity.FactSql;

import java.util.ArrayList;
import java.util.List;

/**
 * the service how to rewrite SQL statement according to Sharding algorithm
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public final class RewriteService {
    private final PreparedQueryPrepareEngine preparedQueryPrepareEngine;

    public RewriteService(final ShardingRuntimeContext shardingRuntimeContext) {
        this.preparedQueryPrepareEngine = new PreparedQueryPrepareEngine(
                shardingRuntimeContext.getRule().toRules(),
                shardingRuntimeContext.getProperties(),
                shardingRuntimeContext.getMetaData(),
                shardingRuntimeContext.getSqlParserEngine());
    }

    public final List<FactSql> rewriteSql(final String sql) {
        final List<FactSql> result = new ArrayList<>();
        final ExecutionContext executionContext = this.preparedQueryPrepareEngine.prepare(sql, new ArrayList<>());
        executionContext.getExecutionUnits().forEach(executionUnit -> result.add(new FactSql(
                executionUnit.getDataSourceName(),
                executionUnit.getSqlUnit().getSql()
        )));
        return result;
    }
}