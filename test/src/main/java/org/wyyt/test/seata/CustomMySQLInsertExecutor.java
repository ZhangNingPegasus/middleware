package org.wyyt.test.seata;


import com.google.common.collect.Lists;
import io.seata.common.exception.NotSupportYetException;
import io.seata.common.exception.ShouldNeverHappenException;
import io.seata.common.loader.LoadLevel;
import io.seata.common.loader.Scope;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.IOUtil;
import io.seata.rm.datasource.ColumnUtils;
import io.seata.rm.datasource.PreparedStatementProxy;
import io.seata.rm.datasource.StatementProxy;
import io.seata.rm.datasource.exec.StatementCallback;
import io.seata.rm.datasource.exec.mysql.MySQLInsertExecutor;
import io.seata.rm.datasource.sql.struct.ColumnMeta;
import io.seata.rm.datasource.sql.struct.TableRecords;
import io.seata.sqlparser.SQLInsertRecognizer;
import io.seata.sqlparser.SQLRecognizer;
import io.seata.sqlparser.util.JdbcConstants;
import org.wyyt.sharding.service.ShardingService;
import org.wyyt.test.util.SpringUtil;
import org.wyyt.tool.sql.SqlTool;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@LoadLevel(name = JdbcConstants.MYSQL, order = 2, scope = Scope.PROTOTYPE)
public class CustomMySQLInsertExecutor extends MySQLInsertExecutor {
    private final ShardingService shardingService;

    public CustomMySQLInsertExecutor(StatementProxy statementProxy, StatementCallback statementCallback, SQLRecognizer sqlRecognizer) {
        super(statementProxy, statementCallback, sqlRecognizer);
        this.shardingService = SpringUtil.getBean(ShardingService.class);
    }

    @Override
    protected Map<String, Integer> getPkIndex() {
        String tableName = SqlTool.removeMySqlQualifier(getTableMeta().getTableName());
        String primaryDimensionShardingColumn = this.shardingService.getPrimaryDimensionShardingColumn(tableName);

        Map<String, Integer> pkIndexMap = new HashMap<>();
        SQLInsertRecognizer recognizer = (SQLInsertRecognizer) sqlRecognizer;
        List<String> insertColumns = recognizer.getInsertColumns();
        if (CollectionUtils.isNotEmpty(insertColumns)) {
            final int insertColumnsSize = insertColumns.size();
            for (int paramIdx = 0; paramIdx < insertColumnsSize; paramIdx++) {
                String sqlColumnName = insertColumns.get(paramIdx);
                if (containPK(sqlColumnName)) {
                    pkIndexMap.put(getStandardColumnName(sqlColumnName), paramIdx);
                } else if (primaryDimensionShardingColumn.equalsIgnoreCase(SqlTool.removeMySqlQualifier(sqlColumnName))) {
                    pkIndexMap.put(SqlTool.removeMySqlQualifier(sqlColumnName), paramIdx);
                }
            }
            return pkIndexMap;
        }
        int pkIndex = -1;
        Map<String, ColumnMeta> allColumns = getTableMeta().getAllColumns();
        for (Map.Entry<String, ColumnMeta> entry : allColumns.entrySet()) {
            pkIndex++;
            if (containPK(entry.getValue().getColumnName())) {
                pkIndexMap.put(ColumnUtils.delEscape(entry.getValue().getColumnName(), getDbType()), pkIndex);
            } else if (primaryDimensionShardingColumn.equalsIgnoreCase(SqlTool.removeMySqlQualifier(entry.getValue().getColumnName()))) {
                pkIndexMap.put(ColumnUtils.delEscape(entry.getValue().getColumnName(), getDbType()), pkIndex);
            }
        }
        return pkIndexMap;
    }

    @Override
    protected Map<String, List<Object>> parsePkValuesFromStatement() {
        SQLInsertRecognizer recognizer = (SQLInsertRecognizer) sqlRecognizer;
        final Map<String, Integer> pkIndexMap = getPkIndex();
        if (pkIndexMap.isEmpty()) {
            throw new ShouldNeverHappenException("pkIndex is not found");
        }
        Map<String, List<Object>> pkValuesMap = new HashMap<>();
        boolean ps = true;
        if (statementProxy instanceof PreparedStatementProxy) {
            PreparedStatementProxy preparedStatementProxy = (PreparedStatementProxy) statementProxy;

            List<List<Object>> insertRows = recognizer.getInsertRows(pkIndexMap.values());
            if (insertRows != null && !insertRows.isEmpty()) {
                Map<Integer, ArrayList<Object>> parameters = preparedStatementProxy.getParameters();
                final int rowSize = insertRows.size();
                int totalPlaceholderNum = -1;
                for (List<Object> row : insertRows) {
                    // oracle insert sql statement specify RETURN_GENERATED_KEYS will append :rowid on sql end
                    // insert parameter count will than the actual +1
                    if (row.isEmpty()) {
                        continue;
                    }
                    int currentRowPlaceholderNum = -1;
                    for (Object r : row) {
                        if (PLACEHOLDER.equals(r)) {
                            totalPlaceholderNum += 1;
                            currentRowPlaceholderNum += 1;
                        }
                    }
                    for (String pkKey : pkIndexMap.keySet()) {
                        List pkValues = pkValuesMap.get(pkKey);
                        if (Objects.isNull(pkValues)) {
                            pkValues = new ArrayList(rowSize);
                        }
                        int pkIndex = pkIndexMap.get(pkKey);
                        Object pkValue = row.get(pkIndex);
                        if (PLACEHOLDER.equals(pkValue)) {
                            int currentRowNotPlaceholderNumBeforePkIndex = 0;
                            for (int n = 0, len = row.size(); n < len; n++) {
                                Object r = row.get(n);
                                if (n < pkIndex && !PLACEHOLDER.equals(r)) {
                                    currentRowNotPlaceholderNumBeforePkIndex++;
                                }
                            }
                            int idx = totalPlaceholderNum - currentRowPlaceholderNum + pkIndex - currentRowNotPlaceholderNumBeforePkIndex;
                            ArrayList<Object> parameter = parameters.get(idx + 1);
                            pkValues.addAll(parameter);
                        } else {
                            pkValues.add(pkValue);
                        }
                        if (!pkValuesMap.containsKey(ColumnUtils.delEscape(pkKey, getDbType()))) {
                            pkValuesMap.put(ColumnUtils.delEscape(pkKey, getDbType()), pkValues);
                        }
                    }
                }
            }
        } else {
            ps = false;
            List<List<Object>> insertRows = recognizer.getInsertRows(pkIndexMap.values());
            for (List<Object> row : insertRows) {
                for (String pkKey : pkIndexMap.keySet()) {
                    int pkIndex = pkIndexMap.get(pkKey);
                    List<Object> pkValues = pkValuesMap.get(pkKey);
                    if (Objects.isNull(pkValues)) {
                        pkValuesMap.put(ColumnUtils.delEscape(pkKey, getDbType()), Lists.newArrayList(row.get(pkIndex)));
                    } else {
                        pkValues.add(row.get(pkIndex));
                    }
                }
            }
        }
        if (pkValuesMap.isEmpty()) {
            throw new ShouldNeverHappenException();
        }
        boolean b = this.checkPkValues(pkValuesMap, ps);
        if (!b) {
            throw new NotSupportYetException(String.format("not support sql [%s]", sqlRecognizer.getOriginalSQL()));
        }
        return pkValuesMap;
    }

    @Override
    protected TableRecords buildTableRecords(Map pkValuesMap) throws SQLException {

        Map<String, List<Object>> valueMap = (Map<String, List<Object>>) pkValuesMap;

        List<String> pkColumnNameList = getTableMeta().getPrimaryKeyOnlyName();
        StringBuilder sql = new StringBuilder()
                .append("SELECT * FROM ")
                .append(getFromTableInSQL())
                .append(" WHERE ");

        String firstKey = valueMap.keySet().stream().findFirst().get();
        int rowSize = valueMap.get(firstKey).size();
        List<String> columnList = valueMap.keySet().stream().sorted().collect(Collectors.toList());

        for (int i = 0; i < rowSize; i++) {
            sql.append("(");
            for (int j = 0; j < columnList.size(); j++) {
                sql.append(String.format("%s=?", columnList.get(j)));
                if (j != columnList.size() - 1) {
                    sql.append(" AND ");
                }
            }
            sql.append(")");
            if (i != rowSize - 1) {
                sql.append(" OR ");
            }
        }

        PreparedStatement ps;
        ResultSet rs = null;
        try {
            ps = statementProxy.getConnection().prepareStatement(sql.toString());
            int paramIndex = 1;
            for (int r = 0; r < rowSize; r++) {

                for (String s : columnList) {
                    List<Object> pkColumnValueList = valueMap.get(s);
                    int dataType = getTableMeta().getColumnMeta(s).getDataType();
                    ps.setObject(paramIndex, pkColumnValueList.get(r), dataType);
                    paramIndex++;
                }

            }
            rs = ps.executeQuery();
            return TableRecords.buildRecords(getTableMeta(), rs);
        } finally {
            IOUtil.close(rs);
        }
    }


}