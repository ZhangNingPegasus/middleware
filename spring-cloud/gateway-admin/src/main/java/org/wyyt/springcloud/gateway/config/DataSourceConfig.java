package org.wyyt.springcloud.gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.wyyt.tool.db.DataSourceTool;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * the configuration of datasource
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
    private final PropertyConfig propertyConfig;

    public DataSourceConfig(PropertyConfig propertyConfig) {
        this.propertyConfig = propertyConfig;
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public DataSource dataSource() throws IOException {
        return DataSourceTool.createHikariDataSource(
                this.propertyConfig.getDbHost(),
                this.propertyConfig.getDbPort(),
                this.propertyConfig.getDbName(),
                this.propertyConfig.getDbUid(),
                this.propertyConfig.getDbPwd(),
                10,
                30
        );
    }
}