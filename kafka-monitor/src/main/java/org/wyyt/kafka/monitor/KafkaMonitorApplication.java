package org.wyyt.kafka.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.wyyt.kafka.monitor.config.PropertyConfig;

/**
 * Kafka-Monitor
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@EnableScheduling
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableTransactionManagement(proxyTargetClass = true)
public class KafkaMonitorApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaMonitorApplication.class, args);
    }

    @Bean
    public TomcatServletWebServerFactory servletContainer(final PropertyConfig propertyConfig) {
        return new TomcatServletWebServerFactory(propertyConfig.getPort());
    }
}