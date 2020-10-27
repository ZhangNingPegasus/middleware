package org.wyyt.test.seata;

import io.seata.common.exception.ShouldNeverHappenException;
import io.seata.common.loader.LoadLevel;
import io.seata.rm.datasource.ColumnUtils;
import io.seata.rm.datasource.sql.struct.ColumnMeta;
import io.seata.rm.datasource.sql.struct.IndexMeta;
import io.seata.rm.datasource.sql.struct.IndexType;
import io.seata.rm.datasource.sql.struct.TableMeta;
import io.seata.rm.datasource.sql.struct.cache.MysqlTableMetaCache;
import io.seata.sqlparser.util.JdbcConstants;
import org.wyyt.sharding.entity.ShardingResult;
import org.wyyt.sharding.service.ShardingService;
import org.wyyt.test.util.SpringUtil;
import org.wyyt.tool.sql.SqlTool;

import java.sql.*;

@LoadLevel(name = JdbcConstants.MYSQL, order = 2)
public class CustomMysqlTableMetaCache extends MysqlTableMetaCache {
    private final ShardingService shardingService;
    private static final String SHARDING_COLUMN_VALUE = "1";

    public CustomMysqlTableMetaCache() {
        this.shardingService = SpringUtil.getBean(ShardingService.class);
    }

    @Override
    protected String getCacheKey(Connection connection, String tableName, String resourceId) {
        tableName = SqlTool.removeMySqlQualifier(tableName);
        return super.getCacheKey(connection, tableName, resourceId);
    }

    @Override
    protected TableMeta fetchSchema(Connection connection, String tableName) throws SQLException {
        tableName = ColumnUtils.addEscape(SqlTool.removeMySqlQualifier(tableName), JdbcConstants.MYSQL);
        String primaryDimensionShardingColumn = this.shardingService.getPrimaryDimensionShardingColumn(tableName);

        String sql = String.format("SELECT * FROM `%s` WHERE `%s` = %s LIMIT 1", tableName, primaryDimensionShardingColumn, SHARDING_COLUMN_VALUE);

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return resultSetMetaToSchema(rs.getMetaData(), connection.getMetaData());
        } catch (SQLException sqlEx) {
            throw sqlEx;
        } catch (Exception e) {
            throw new SQLException(String.format("Failed to fetch schema of %s", tableName), e);
        }
    }

    private TableMeta resultSetMetaToSchema(ResultSetMetaData rsmd, DatabaseMetaData dbmd)
            throws SQLException {
        String schemaName = rsmd.getSchemaName(1);
        String catalogName = rsmd.getCatalogName(1);
        String tableName = SqlTool.removeMySqlQualifier(rsmd.getTableName(1));

        TableMeta tm = new TableMeta();
        tm.setTableName(tableName);

        ShardingResult shardingResult = this.shardingService.doSharding(tableName, SHARDING_COLUMN_VALUE);

        try (ResultSet rsColumns = dbmd.getColumns(catalogName, schemaName, shardingResult.getTableName(), "%");
             ResultSet rsIndex = dbmd.getIndexInfo(catalogName, schemaName, shardingResult.getTableName(), false, true)) {
            while (rsColumns.next()) {
                ColumnMeta col = new ColumnMeta();
                col.setTableCat(rsColumns.getString("TABLE_CAT"));
                col.setTableSchemaName(rsColumns.getString("TABLE_SCHEM"));
                col.setTableName(rsColumns.getString("TABLE_NAME"));
                col.setColumnName(rsColumns.getString("COLUMN_NAME"));
                col.setDataType(rsColumns.getInt("DATA_TYPE"));
                col.setDataTypeName(rsColumns.getString("TYPE_NAME"));
                col.setColumnSize(rsColumns.getInt("COLUMN_SIZE"));
                col.setDecimalDigits(rsColumns.getInt("DECIMAL_DIGITS"));
                col.setNumPrecRadix(rsColumns.getInt("NUM_PREC_RADIX"));
                col.setNullAble(rsColumns.getInt("NULLABLE"));
                col.setRemarks(rsColumns.getString("REMARKS"));
                col.setColumnDef(rsColumns.getString("COLUMN_DEF"));
                col.setSqlDataType(rsColumns.getInt("SQL_DATA_TYPE"));
                col.setSqlDatetimeSub(rsColumns.getInt("SQL_DATETIME_SUB"));
                col.setCharOctetLength(rsColumns.getInt("CHAR_OCTET_LENGTH"));
                col.setOrdinalPosition(rsColumns.getInt("ORDINAL_POSITION"));
                col.setIsNullAble(rsColumns.getString("IS_NULLABLE"));
                col.setIsAutoincrement(rsColumns.getString("IS_AUTOINCREMENT"));
                tm.getAllColumns().put(col.getColumnName(), col);
            }

            while (rsIndex.next()) {
                String indexName = rsIndex.getString("INDEX_NAME");
                String colName = rsIndex.getString("COLUMN_NAME");
                ColumnMeta col = tm.getAllColumns().get(colName);
                if (tm.getAllIndexes().containsKey(indexName)) {
                    IndexMeta index = tm.getAllIndexes().get(indexName);
                    index.getValues().add(col);
                } else {
                    IndexMeta index = new IndexMeta();
                    index.setIndexName(indexName);
                    index.setNonUnique(rsIndex.getBoolean("NON_UNIQUE"));
                    index.setIndexQualifier(rsIndex.getString("INDEX_QUALIFIER"));
                    index.setIndexName(rsIndex.getString("INDEX_NAME"));
                    index.setType(rsIndex.getShort("TYPE"));
                    index.setOrdinalPosition(rsIndex.getShort("ORDINAL_POSITION"));
                    index.setAscOrDesc(rsIndex.getString("ASC_OR_DESC"));
                    index.setCardinality(rsIndex.getInt("CARDINALITY"));
                    index.getValues().add(col);
                    if ("PRIMARY".equalsIgnoreCase(indexName)) {
                        index.setIndextype(IndexType.PRIMARY);
                    } else if (!index.isNonUnique()) {
                        index.setIndextype(IndexType.UNIQUE);
                    } else {
                        index.setIndextype(IndexType.NORMAL);
                    }
                    tm.getAllIndexes().put(indexName, index);
                }
            }
            if (tm.getAllIndexes().isEmpty()) {
                throw new ShouldNeverHappenException("Could not found any index in the table: " + tableName);
            }
        }
        return tm;
    }
}