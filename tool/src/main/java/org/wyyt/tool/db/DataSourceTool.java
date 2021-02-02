package org.wyyt.tool.db;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;

/**
 * Providing common functions of database.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class DataSourceTool {
    private static final String DATA_BASE_URL = "jdbc:mysql://%s/%s?allowPublicKeyRetrieval=true&serverTimezone=GMT%%2B8&characterEncoding=UTF-8&useUnicode=true&autoReconnect=true&allowMultiQueries=true&useSSL=false&rewriteBatchedStatements=true&zeroDateTimeBehavior=CONVERT_TO_NULL";
    private static final String CONNECTION_TEST_QUERY = "SELECT 1";
    private static final String CONNECTION_INIT_SQL = "SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci";

    public static DataSource createDruidDataSource(final String poolName,
                                                   final String host,
                                                   final String port,
                                                   final String databaseName,
                                                   final String userName,
                                                   final String password,
                                                   final Integer initialSize,
                                                   final Integer minIdle,
                                                   final Integer maxActive) {
        final DruidDataSource result = new DruidDataSource();
        if (!StrUtil.isBlank(poolName)) {
            result.setName(poolName); //连接池名称
        }
        result.setDriverClassName(com.mysql.cj.jdbc.Driver.class.getCanonicalName());
        result.setUrl(String.format(DATA_BASE_URL, getDbUrl(host, port), databaseName));
        result.setUsername(userName);
        result.setPassword(password);
        result.setInitialSize(initialSize); //配置初始化大小
        result.setMinIdle(minIdle); // 配置连接池中最小可用连接的个数
        result.setMaxActive(maxActive); //配置连接池中最大可用连接的个数
        result.setMaxWait(60000L); //配置获取连接等待超时的时间, 单位是毫秒
        result.setTimeBetweenEvictionRunsMillis(60000L); //配置间隔多久才进行一次检测, 检测需要关闭的空闲连接, 单位是毫秒
        result.setMinEvictableIdleTimeMillis(300000); //配置一个连接在池中最小生存的时间, 单位是毫秒
        result.setValidationQueryTimeout(60000);
        result.setTestWhileIdle(true);
        result.setTestOnBorrow(false);
        result.setTestOnReturn(false);
        result.setPoolPreparedStatements(false); //打开PSCache, 并且指定每个连接上PSCache的大小.分库分表较多的数据库，建议配置为false
        result.setMaxPoolPreparedStatementPerConnectionSize(20);
        result.setMaxOpenPreparedStatements(20);
        result.setValidationQuery(CONNECTION_TEST_QUERY);
        result.setConnectionInitSqls(Collections.singleton(CONNECTION_INIT_SQL));
        return result;
    }

    public static DataSource createDruidDataSource(final String host,
                                                   final String port,
                                                   final String databaseName,
                                                   final String userName,
                                                   final String password) {
        return createDruidDataSource(null, host, port, databaseName, userName, password, 5, 10, 20);
    }

    public static DataSource createHikariDataSource(final String poolName,
                                                    final String host,
                                                    final String port,
                                                    final String databaseName,
                                                    final String userName,
                                                    final String password,
                                                    final Integer minIdle,
                                                    final Integer maximum) {
        final HikariDataSource result = new HikariDataSource();
        if (!StrUtil.isBlank(poolName)) {
            result.setPoolName(poolName); //连接池名称
        }
        result.setDriverClassName(com.mysql.cj.jdbc.Driver.class.getCanonicalName());
        result.setJdbcUrl(String.format(DATA_BASE_URL, getDbUrl(host, port), databaseName));
        result.setUsername(userName);
        result.setPassword(password);
        result.setMinimumIdle(minIdle);     //最小空闲连接数量
        result.setMaximumPoolSize(maximum); //连接池最大连接数，默认是10
        result.setIdleTimeout(600000); //空闲连接存活最大时间，默认600000（10分钟）
        result.setAutoCommit(true);  //此属性控制从池返回的连接的默认自动提交行为,默认值：true
        result.setMaxLifetime(1800000); //此属性控制池中连接的最长生命周期，值0表示无限生命周期，默认1800000即30分钟
        result.setConnectionTimeout(30000); //数据库连接超时时间,默认30秒，即30000
        result.setConnectionTestQuery(CONNECTION_TEST_QUERY);
        result.setConnectionInitSql(CONNECTION_INIT_SQL);
        return result;
    }

    public static DataSource createHikariDataSource(final String host,
                                                    final String port,
                                                    final String databaseName,
                                                    final String userName,
                                                    final String password,
                                                    final Integer minIdle,
                                                    final Integer maximum) {
        return createHikariDataSource(null, host, port, databaseName, userName, password, minIdle, maximum);
    }

    public static DataSource createHikariDataSource(final String host,
                                                    final String port,
                                                    final String databaseName,
                                                    final String userName,
                                                    final String password) {
        return createHikariDataSource(host, port, databaseName, userName, password, 10, 20);
    }

    public static void close(final DataSource dataSource) {
        if (null != dataSource) {
            try {
                if (dataSource instanceof DruidDataSource) {
                    ((DruidDataSource) dataSource).close();
                } else if (dataSource instanceof HikariDataSource) {
                    ((HikariDataSource) dataSource).close();
                }

            } catch (final Exception exception) {
                log.error(exception.getMessage(), exception);
            }
        }
    }

    public static void close(final Connection connection) {
        if (null != connection) {
            try {
                connection.close();
            } catch (final Exception exception) {
                log.error(exception.getMessage(), exception);
            }
        }
    }

    public static void close(final Statement statement) {
        if (null != statement) {
            try {
                statement.close();
            } catch (final Exception exception) {
                log.error(exception.getMessage(), exception);
            }
        }
    }

    public static void close(final ResultSet resultSet) {
        if (null != resultSet) {
            try {
                resultSet.close();
            } catch (final Exception exception) {
                log.error(exception.getMessage(), exception);
            }
        }
    }

    private static String getDbUrl(final String host,
                                   final String port) {
        if (ObjectUtils.isEmpty(host)) {
            throw new RuntimeException("host is required");
        }

        if (ObjectUtils.isEmpty(port)) {
            return host.trim();
        } else {
            return String.format("%s:%s", host.trim(), port.trim());
        }
    }
}