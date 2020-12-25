package org.wyyt.sharding.sqltool;

import org.apache.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.wyyt.admin.ui.config.EnableAdminUI;

/**
 * the main function of Tool
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@EnableScheduling
@SpringBootApplication(exclude = {SpringBootConfiguration.class})
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAdminUI
public class SqlToolApplication {
    public static void main(String[] args) {
        SpringApplication.run(SqlToolApplication.class, args);
    }
}