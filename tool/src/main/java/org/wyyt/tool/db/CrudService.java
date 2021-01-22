package org.wyyt.tool.db;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.reflections.ReflectionUtils;
import org.springframework.beans.factory.DisposableBean;
import org.wyyt.tool.date.DateTool;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.sql.SqlTool;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The database service used for dynamic sql
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class CrudService implements DisposableBean {
    @Getter
    private final DataSource dataSource;

    public CrudService(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public CrudService(final String host,
                       final String port,
                       final String dbName,
                       final String username,
                       final String password,
                       final Integer minIdle,
                       final Integer maximum) {
        this(DataSourceTool.createHikariDataSource(
                host,
                port,
                dbName,
                username,
                password,
                minIdle,
                maximum));
    }

    public CrudService(final String host,
                       final String port,
                       final String dbName,
                       final String username,
                       final String password) {
        this(host, port, dbName, username, password, 10, 20);
    }

    public final <T> T executeScalar(final Class<T> cls,
                                     final String sql,
                                     final Object... params) throws Exception {
        final AtomicReference<T> result = new AtomicReference<>();
        query(kvs -> {
            if (null != kvs && !kvs.isEmpty()) {
                final Map<String, Object> kv = kvs.get(0);
                for (final Map.Entry<String, Object> pair : kv.entrySet()) {
                    if (null == pair.getValue()) {
                        result.set(null);
                    } else {
                        result.set((T) ConvertUtils.convert(pair.getValue(), cls));
                    }
                    return;
                }
            }
        }, sql, params);
        return result.get();
    }

    public final <T> List<T> select(final Class<T> cls,
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
                    if (null != annoTableField) {
                        columnName = annoTableField.value();
                    } else if (null != annoTableId) {
                        columnName = annoTableId.value();
                    }
                    if (null == columnName) {
                        continue;
                    }
                    field.setAccessible(true);
                    Object value = kv.get(SqlTool.removeMySqlQualifier(columnName));
                    if (null == value) {
                        field.set(t, null);
                    } else {
                        if (value.getClass().isAssignableFrom(LocalDateTime.class)) {
                            final LocalDateTime localDateTime = (LocalDateTime) value;
                            value = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                        }
                        field.set(t, ConvertUtils.convert(value, field.getType()));
                    }
                }
                result.add(t);
            }
        }, sql, params);
        return result;
    }

    public final List<Map<String, Object>> select(final String sql, final Object... params) throws Exception {
        final AtomicReference<List<Map<String, Object>>> result = new AtomicReference<>(null);
        this.query(result::set, sql, params);
        return result.get();
    }

    public final <T> CrudPage<T> page(final Class<T> cls,
                                      final Integer pageNum,
                                      final Integer pageSize,
                                      final String sql,
                                      final Object... params) throws Exception {
        final int startIndex = (Math.max(pageNum, 1) - 1) * pageSize;


        final String querySql = String.format("SELECT * FROM (%s) crud_service_query LIMIT %s, %s", sql, startIndex, pageSize);
        final String totalSql = String.format("SELECT COUNT(*) FROM (%s) crud_service_count", sql);

        final List<T> records = this.select(cls, querySql, params);
        final Long total = this.executeScalar(Long.class, totalSql, params);
        final CrudPage<T> crudPage = new CrudPage<>();
        crudPage.setRecrods(records);
        crudPage.setTotal(total);
        return crudPage;
    }


    public final <T> T selectOne(final Class<T> cls, final String sql, final Object... params) throws Exception {
        final List<T> result = this.select(cls, sql, params);
        if (result.isEmpty()) {
            return null;
        } else if (result.size() > 1) {
            throw new RuntimeException("期待只获取一条记录, 但查出了多条符合条件的记录");
        }
        return result.get(0);
    }

    public final Map<String, Object> selectOne(final String sql, final Object... params) throws Exception {
        final List<Map<String, Object>> result = this.select(sql, params);
        if (null == result || result.isEmpty()) {
            return null;
        } else if (result.size() > 1) {
            throw new RuntimeException("期待只获取一条记录, 但查出了多条符合条件的记录");
        }
        return result.get(0);
    }

    public final boolean execute(final String sql, final Object... params) throws Exception {
        try (
                final Connection connection = this.dataSource.getConnection();
                final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            return preparedStatement.execute();
        }
    }

    public final void queryInTransaction(final Handle handle, final String sql, final Object... params) throws Exception {
        List<Map<String, Object>> kv;
        ResultSet resultSet = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = this.dataSource.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            connection.setAutoCommit(false);
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
                for (String columnLabel : columnLabelList) {
                    Object value = resultSet.getObject(columnLabel);
                    if (resultSet.getObject(columnLabel).getClass().isAssignableFrom(Timestamp.class)) {
                        value = DateTool.parse(resultSet.getString(columnLabel));
                    }
                    row.put(columnLabel, value);
                }
                kv.add(row);
            }
            handle.process(kv);
            connection.commit();
        } catch (final Exception exception) {
            if (null != connection) {
                connection.rollback();
            }
            log.error(ExceptionTool.getRootCauseMessage(exception), exception);
            throw exception;
        } finally {
            DataSourceTool.close(resultSet);
            DataSourceTool.close(preparedStatement);
            DataSourceTool.close(connection);
        }
    }

    private void query(final Handle handle, final String sql, final Object... params) throws Exception {
        List<Map<String, Object>> kv;
        ResultSet resultSet = null;
        try (
                Connection connection = this.dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            if (null != params) {
                final List<Object> parameters = new ArrayList<>();
                for (Object param : params) {
                    if (null == param) {
                        continue;
                    }
                    parameters.add(param);
                }

                for (int i = 0; i < parameters.size(); i++) {
                    preparedStatement.setObject(i + 1, parameters.get(i));
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

    @Override
    public final void destroy() {
        DataSourceTool.close(this.dataSource);
    }

    public interface Handle {
        void process(final List<Map<String, Object>> kvs) throws Exception;
    }
}