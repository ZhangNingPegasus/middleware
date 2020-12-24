package org.wyyt.springcloud.gateway.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
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
 * Ning.Zhang       Initialize        10/1/2020       Initialize   *
 * *****************************************************************
 */
@Configuration
public class DataSourceConfig {
    private final PropertyConfig propertyConfig;

    public DataSourceConfig(final PropertyConfig propertyConfig) {
        this.propertyConfig = propertyConfig;
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        final MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
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
                10,
                30
        );
    }
}