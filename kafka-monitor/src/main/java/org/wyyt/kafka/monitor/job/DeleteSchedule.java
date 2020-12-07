package org.wyyt.kafka.monitor.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wyyt.kafka.monitor.service.dto.SchemaService;

import java.util.*;

/**
 * The schedule job for deleting expired records.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Component
public class DeleteSchedule {
    private static final List<String> ignoreTable = Arrays.asList("sys_admin", "sys_alert_cluster", "sys_alert_consumer", "sys_alert_topic", "sys_dingding_config", "sys_mail_config", "sys_page", "sys_permission", "sys_role", "sys_table_name");
    private final SchemaService schemaService;

    public DeleteSchedule(final SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    //每天00:01:00执行一次
    @Scheduled(cron = "0 1 0 1/1 * ?")
    public void deleteExpired() {
        final Set<String> allTableNames = this.schemaService.listTables();
        final Set<String> tableNames = new HashSet<>(allTableNames.size());

        for (final String tableName : allTableNames) {
            if (ignoreTable.contains(tableName.toLowerCase(Locale.ROOT))) {
                continue;
            }
            tableNames.add(tableName);
        }

        this.schemaService.deleteExpired(tableNames);
    }
}