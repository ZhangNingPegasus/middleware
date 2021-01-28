package springcloud.service.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.wyyt.tool.db.DataSourceTool;

import javax.sql.DataSource;

@Configuration
public class MybatisPlusConfig {
    @Primary
    @Bean(name = "dataSource")
    public DataSource dataSource() {
        return DataSourceTool.createHikariDataSource(
                "192.168.0.197",
                "3306",
                "seata_test_1",
                "root",
                "zhangningpegasus"
        );
    }
}