package org.wyyt.springcloud.springbootadmin.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.wyyt.tool.cache.CacheService;
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
@ComponentScan({"org.wyyt.springcloud.gateway.entity"})
@MapperScan({"org.wyyt.springcloud.gateway.entity.mapper"})
public class DataSourceConfig {
    private final PropertyConfig propertyConfig;

    public DataSourceConfig(final PropertyConfig propertyConfig) {
        this.propertyConfig = propertyConfig;
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        final MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        final PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setOverflow(true);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
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
                this.propertyConfig.getDbUseName(),
                this.propertyConfig.getDbPassword(),
                this.propertyConfig.getDbMinIdle(),
                this.propertyConfig.getDbMaximum()
        );
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public CacheService cacheService() {
        return new CacheService();
    }
}