package org.wyyt.sharding.db2es.admin;

import org.apache.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.wyyt.admin.ui.config.EnableAdminUI;

/**
 * the main function of db2es-admin
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@EnableScheduling
@SpringBootApplication(exclude = {SpringBootConfiguration.class})
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAdminUI
public class Db2EsAdmin {
    public static void main(final String[] args) {
        SpringApplication.run(Db2EsAdmin.class, args);
    }
}