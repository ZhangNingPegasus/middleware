package org.wyyt.sharding.db2es.client.db;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.reflections.ReflectionUtils;
import org.wyyt.sharding.db2es.client.entity.Db2EsLog;
import org.wyyt.sharding.db2es.core.entity.domain.Config;
import org.wyyt.sharding.db2es.core.entity.domain.TableMap;
import org.wyyt.sharding.db2es.core.entity.persistent.Property;
import org.wyyt.sharding.db2es.core.entity.persistent.Table;
import org.wyyt.sharding.db2es.core.entity.persistent.Topic;
import org.wyyt.sharding.db2es.core.exception.Db2EsException;
import org.wyyt.tool.date.DateTool;
import org.wyyt.tool.db.DataSourceTool;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.sql.SqlTool;

import javax.sql.DataSource;
import java.io.Closeable;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

/**
 * CRUD for db2es' database
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class DbWrapper implements Closeable {
    private static final String DB2ES_LOG_INSERT_SQL = "INSERT IGNORE INTO `t_error_log`(`primary_key_value`, `database_name`, `table_name`, `index_name`, `error_message`, `consumer_record`, `topic_name`, `partition`, `offset`, `is_resolved`) VALUES (?,?,?,?,?,?,?,?,?,?)";
    private final DataSource dataSource;

    public DbWrapper(final Config config) {
        this.dataSource = DataSourceTool.createHikariDataSource(
                config.getDbHost(),
                config.getDbPort().toString(),
                config.getDbName(),
                config.getDbUid(),
                config.getDbPwd(),
                10,
                30
        );
    }

    public final List<Property> listProperty() throws Exception {
        return this.select(Property.class, "SELECT * FROM `t_property`");
    }

    public final TableMap listTableMap() throws Exception {
        List<Table> tableList = this.select(Table.class, "SELECT * FROM `t_table`");
        for (Table table : tableList) {
            return JSON.parseObject(table.getInfo(), TableMap.class);
        }
        return null;
    }

    public final Set<Topic> listTopics(final Integer db2EsId) throws Exception {
        return new HashSet<>(this.select(Topic.class, "SELECT `b`.* FROM `t_topic_db2es` `a` INNER JOIN `t_topic` `b` ON `a`.`topic_id` = `b`.`id` WHERE `a`.`db2es_id` = ?", db2EsId));
    }

    public final Topic getTopicById(final Long topicId) throws Exception {
        return this.selectOne(Topic.class, "SELECT * FROM `t_topic` WHERE `id` = ?", topicId);
    }

    public final void insertLogs(final List<Db2EsLog> db2EsLogList) {
        if (null == db2EsLogList || db2EsLogList.isEmpty()) {
            return;
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = this.dataSource.getConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(DB2ES_LOG_INSERT_SQL);
            for (final Db2EsLog db2EsLog : db2EsLogList) {
                pstmt.setString(1, substring(db2EsLog.getPrimaryKeyValue(), 128));
                pstmt.setString(2, substring(db2EsLog.getDatabaseName(), 255));
                pstmt.setString(3, substring(db2EsLog.getTableName(), 255));
                pstmt.setString(4, substring(db2EsLog.getIndexName(), 128));
                pstmt.setString(5, substring(db2EsLog.getErrorMessage(), 4000));
                pstmt.setString(6, substring(db2EsLog.getConsumerRecord(), 8000));
                pstmt.setString(7, substring(db2EsLog.getTopicName(), 255));
                pstmt.setString(8, substring(db2EsLog.getPartition().toString(), -1));
                pstmt.setString(9, substring(db2EsLog.getOffset().toString(), -1));
                pstmt.setString(10, "0");
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
        } catch (final Exception exception) {
            log.error(String.format("DbWrapper: log the message with error, caused %s, db2EsLogList's content like [%s]", ExceptionTool.getRootCauseMessage(exception), db2EsLogList), exception);
            if (null != conn) {
                try {
                    conn.rollback();
                } catch (final SQLException e) {
                    log.error(String.format("DbWrapper: rollback meet error, caused %s", ExceptionTool.getRootCauseMessage(e)), e);
                }
            }
        } finally {
            DataSourceTool.close(pstmt);
            DataSourceTool.close(conn);
        }
    }

    @Override
    public final void close() {
        DataSourceTool.close(this.dataSource);
    }

    private <T> List<T> select(final Class<T> cls,
                               final String sql,
                               final Object... params) throws Exception {
        final List<T> result = new ArrayList<>();
        final Set<Field> allFields = ReflectionUtils.getAllFields(cls);

        query(kvs -> {
            for (final Map<String, Object> kv : kvs) {
                final T t = cls.newInstance();
                for (final Field field : allFields) {
                    final TableField annoTableField = field.getAnnotation(TableField.class);
                    final TableId annoTableId = field.getAnnotation(TableId.class);
                    String columnName = null;
                    if (annoTableField != null) {
                        columnName = annoTableField.value();
                    } else if (annoTableId != null) {
                        columnName = annoTableId.value();
                    }
                    if (null == columnName) {
                        continue;
                    }
                    field.setAccessible(true);
                    final Object value = kv.get(SqlTool.removeMySqlQualifier(columnName));
                    if (null == value) {
                        field.set(t, null);
                    } else {
                        field.set(t, ConvertUtils.convert(value, field.getType()));
                    }
                }
                result.add(t);
            }
        }, sql, params);
        return result;
    }

    private <T> T selectOne(final Class<T> cls,
                            final String sql,
                            final Object... params) throws Exception {
        final List<T> result = select(cls, sql, params);
        if (result.isEmpty()) {
            return null;
        } else if (result.size() > 1) {
            throw new Db2EsException("期待只获取一条记录, 但查出了多条符合条件的记录");
        }
        return result.get(0);
    }

    private void query(final Handle handle,
                       final String sql,
                       final Object... params) throws Exception {
        List<Map<String, Object>> kv;
        ResultSet resultSet = null;
        try (
                final Connection connection = dataSource.getConnection();
                final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            if (null != params) {
                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }
            }
            resultSet = preparedStatement.executeQuery();
            final ResultSetMetaData metaData = resultSet.getMetaData();
            final int columnCount = metaData.getColumnCount();
            kv = new ArrayList<>();
            final List<String> columnLabelList = new ArrayList<>(columnCount);
            for (int i = 0; i < columnCount; i++) {
                columnLabelList.add(metaData.getColumnLabel(i + 1));
            }
            while (resultSet.next()) {
                final Map<String, Object> row = new HashMap<>();
                for (final String columnLabel : columnLabelList) {
                    Object value = resultSet.getObject(columnLabel);
                    if (null != value && value.getClass().isAssignableFrom(Timestamp.class)) {
                        value = DateTool.parse(resultSet.getString(columnLabel));
                    }
                    row.put(columnLabel, value);
                }
                kv.add(row);
            }
        } finally {
            DataSourceTool.close(resultSet);
        }
        handle.process(kv);
    }

    private static String substring(final String value, final int length) {
        if (null == value) {
            return "";
        }
        if (length < 0) {
            return value;
        }
        if (value.length() > length) {
            return value.substring(0, length);
        }
        return value;
    }

    public interface Handle {
        void process(final List<Map<String, Object>> kvs) throws Exception;
    }
}