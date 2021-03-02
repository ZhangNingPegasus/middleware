package org.wyyt.basic.service.admin;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.wyyt.admin.ui.config.EnableAdminUI;

/**
 * Kafka-Monitor
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@EnableScheduling
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableTransactionManagement(proxyTargetClass = true)
@EnableApolloConfig
@EnableAdminUI
public class BasicServiceAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(BasicServiceAdminApplication.class, args);
    }
}