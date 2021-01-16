package org.wyyt.sharding.db2es.admin.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.wyyt.tool.db.DataSourceTool;

import javax.sql.DataSource;

/**
 * the configuration of data source
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Configuration
public class DataSourceConfig {
    private final PropertyConfig propertyConfig;

    public DataSourceConfig(final PropertyConfig propertyConfig) {
        this.propertyConfig = propertyConfig;
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "sharding", name = "enabled", havingValue = "false")
    public DataSource shardingDataSource() {
        return DataSourceTool.createHikariDataSource(
                this.propertyConfig.getDbHost(),
                this.propertyConfig.getDbPort(),
                this.propertyConfig.getDbName(),
                this.propertyConfig.getDbUid(),
                this.propertyConfig.getDbPwd(),
                10,
                20
        );
    }
}