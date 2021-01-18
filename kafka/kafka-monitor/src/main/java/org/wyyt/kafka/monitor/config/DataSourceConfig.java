package org.wyyt.kafka.monitor.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.wyyt.tool.cache.CacheService;
import org.wyyt.tool.db.DataSourceTool;

import javax.sql.DataSource;

/**
 * the configuration of mybatis-plus
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

    public DataSourceConfig(PropertyConfig propertyConfig) {
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
                30,
                60
        );
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public CacheService cacheService() {
        return new CacheService(10L, 128, 1024L);
    }
}