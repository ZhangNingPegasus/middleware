package org.wyyt.springcloud.gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.wyyt.tool.db.DataSourceTool;

import javax.sql.DataSource;

/**
 * the configuration of datasource
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
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
    @ConditionalOnMissingBean
    public DataSource dataSource() {
        return DataSourceTool.createHikariDataSource(
                this.propertyConfig.getDbHost(),
                this.propertyConfig.getDbPort(),
                this.propertyConfig.getDbName(),
                this.propertyConfig.getDbUid(),
                this.propertyConfig.getDbPwd(),
                this.propertyConfig.getDbMinIdle(),
                this.propertyConfig.getDbMaximum()
        );
    }
}