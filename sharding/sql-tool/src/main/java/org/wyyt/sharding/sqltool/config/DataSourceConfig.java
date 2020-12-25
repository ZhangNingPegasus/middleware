package org.wyyt.sharding.sqltool.config;

import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * the configuration of mybatis-plus
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020       Initialize   *
 * *****************************************************************
 */
@Configuration
public class DataSourceConfig {
    private final ShardingDataSource shardingDataSource;

    public DataSourceConfig(ShardingDataSource shardingDataSource) {
        this.shardingDataSource = shardingDataSource;
    }

    @Bean
    public DataSource dataSource() {
        return this.shardingDataSource;
    }
}