package org.wyyt.springcloud.auth.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.wyyt.tool.db.DataSourceTool;
import org.wyyt.tool.rpc.RpcTool;

import javax.sql.DataSource;

/**
 * the configuration of bean
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020       Initialize   *
 * *****************************************************************
 */
@Configuration
@ComponentScan({"org.wyyt.springcloud.gateway.entity"})
@MapperScan({"org.wyyt.springcloud.gateway.entity.mapper"})
public class BeanConfig {
    @Bean
    public RpcTool rpcTool() {
        return new RpcTool();
    }

    @Bean
    public DataSource dataSource() {
        return DataSourceTool.createHikariDataSource(
                "192.168.0.197",
                "3306",
                "gateway_admin",
                "root",
                "zhangningpegasus"
        );
    }
}
