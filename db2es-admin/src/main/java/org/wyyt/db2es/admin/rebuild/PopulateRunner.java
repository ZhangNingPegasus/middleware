package org.wyyt.db2es.admin.rebuild;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.index.VersionType;
import org.springframework.util.Assert;
import org.wyyt.db2es.core.entity.domain.Config;
import org.wyyt.db2es.core.entity.domain.IndexName;
import org.wyyt.db2es.core.entity.domain.TableInfo;
import org.wyyt.db2es.core.exception.Db2EsException;
import org.wyyt.db2es.core.util.CommonUtils;
import org.wyyt.tool.date.DateTool;
import org.wyyt.tool.db.DataSourceTool;
import org.wyyt.tool.sql.SqlTool;

import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * the thread used for polling the records from db to es
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
public final class PopulateRunner implements Runnable, Closeable {
    private final AtomicBoolean terminated;
    private final CountDownLatch countDownLatch;
    private final DataSource dataSource;
    private final TableInfo tableInfo;
    @Getter
    private final String tableName;
    private final Map<Integer, IndexName> rebuildIndexMap;
    private final ElasticSearchBulk elasticSearchBulk;
    private final String sql;
    @Getter
    private long count = 0L;
    @Getter
    @Setter
    private volatile Exception exception;

    public PopulateRunner(final ElasticSearchBulk elasticSearchBulk,
                          final CountDownLatch countDownLatch,
                          final DataSource dataSource,
                          final String tableName,
                          final Config config,
                          final Map<Integer, IndexName> rebuildIndexMap,
                          final Date minDate,
                          final Date maxDate,
                          final AtomicBoolean terminated) {
        this.elasticSearchBulk = elasticSearchBulk;
        this.countDownLatch = countDownLatch;
        this.dataSource = dataSource;
        this.tableName = tableName;
        this.rebuildIndexMap = rebuildIndexMap;
        this.terminated = terminated;
        this.tableInfo = config.getTableMap().getByFactTableName(tableName);

        this.sql = String.format("SELECT * FROM `%s` WHERE `%s` >= '%s' AND `%s` <= '%s' ORDER BY `%s` ASC",
                SqlTool.removeMySqlQualifier(tableName),
                this.tableInfo.getRowCreateTimeFieldName(),
                CommonUtils.formatMs(minDate),
                this.tableInfo.getRowCreateTimeFieldName(),
                CommonUtils.formatMs(maxDate),
                this.tableInfo.getPrimaryKeyFieldName());
    }

    @SneakyThrows
    @Override
    public final void run() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, String> columns = null;

        try {
            Thread.currentThread().setName(String.format("thread-for-populate-%s", tableName));
            conn = this.dataSource.getConnection();
            ps = conn.prepareStatement(this.sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ps.setFetchSize(Integer.MIN_VALUE);
            rs = ps.executeQuery();
            while (!this.terminated.get() && rs.next()) {
                if (null == columns) {
                    columns = getColumnType(rs.getMetaData());
                }
                final String id = rs.getString(this.tableInfo.getPrimaryKeyFieldName());
                final Date rowUpdateTime = DateTool.parse(rs.getString(this.tableInfo.getRowUpdateTimeFieldName()));
                Assert.notNull(rowUpdateTime, String.format("在表[%s]中,主键为[%s]的数据缺少字段[%s]的值", this.tableName, id, this.tableInfo.getRowUpdateTimeFieldName()));

                final Map<String, String> datum = new HashMap<>();
                for (Map.Entry<String, String> pair : columns.entrySet()) {
                    datum.put(pair.getKey(), rs.getString(pair.getKey()));
                }
                this.elasticSearchBulk.add(new IndexRequest(getIndexName(rs))
                        .id(id)
                        .source(datum)
                        .versionType(VersionType.EXTERNAL)
                        .version(CommonUtils.toEsVersion(rowUpdateTime)));
                count++;
            }
        } catch (final Exception exception) {
            this.exception = exception;
        } finally {
            DataSourceTool.close(rs);
            DataSourceTool.close(ps);
            DataSourceTool.close(conn);
            this.countDownLatch.countDown();
        }
    }

    private String getIndexName(final ResultSet rs) throws SQLException {
        final String id = rs.getString(this.tableInfo.getPrimaryKeyFieldName());
        final Date rowCreateTime = DateTool.parse(rs.getString(this.tableInfo.getRowCreateTimeFieldName()));
        Assert.notNull(id, String.format("在表[%s]中,缺少主键为[%s]的数据", this.tableName, this.tableInfo.getPrimaryKeyFieldName()));
        Assert.notNull(rowCreateTime, String.format("在表[%s]中,主键为[%s]的数据缺少字段[%s]的值", this.tableName, id, this.tableInfo.getRowCreateTimeFieldName()));
        final String result = Tool.getIndexName(this.rebuildIndexMap, rowCreateTime);
        if (StringUtils.isEmpty(result)) {
            throw new Db2EsException(String.format("在表%s中, 主键是[%s]的记录无法定位到对应的索引", this.tableName, id));
        }
        return result;
    }

    private static Map<String, String> getColumnType(final ResultSetMetaData resultSetMetaData) throws SQLException {
        final Map<String, String> result = new HashMap<>((int) (resultSetMetaData.getColumnCount() / 0.75));
        final int count = resultSetMetaData.getColumnCount();
        for (int i = 0; i < count; i++) {
            final String name = resultSetMetaData.getColumnName(i + 1);
            final String type = resultSetMetaData.getColumnTypeName(i + 1);
            result.put(name, type);
        }
        return result;
    }

    @Override
    public void close() {
    }
}