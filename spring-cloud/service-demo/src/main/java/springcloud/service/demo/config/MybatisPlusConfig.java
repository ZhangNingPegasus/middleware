package springcloud.service.demo.config;

import brave.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

//    @Value("${db.name}")
//    private String dbName;

    @Bean
    public HttpResponseInjectingTraceFilter responseInjectingTraceFilter(Tracer tracer) {
        return new HttpResponseInjectingTraceFilter(tracer);
    }

//    @Primary
//    @Bean(name = "dataSource")
//    public DataSource dataSource() {
//        return new DataSourceProxy(DataSourceTool.createHikariDataSource(
//                "192.168.0.197",
//                "3306",
//                this.dbName,
//                "root",
//                "zhangningpegasus"
//        ));
//    }
}
