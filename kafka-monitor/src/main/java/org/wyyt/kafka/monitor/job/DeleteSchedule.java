package org.wyyt.kafka.monitor.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wyyt.kafka.monitor.service.dto.SchemaService;

import java.util.Set;
import java.util.stream.Collectors;

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
    private static final String SUFFIX_SYSTEM_TABLE = "sys_";
    private final SchemaService schemaService;

    public DeleteSchedule(final SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    //每天00:01:00执行一次
    @Scheduled(cron = "0 1 0 1/1 * ?")
    public void deleteExpired() {
        final Set<String> tableNames = this.schemaService.listTables();
        final Set<String> filterTableNames = tableNames.stream().filter(tableName -> !tableName.startsWith(SUFFIX_SYSTEM_TABLE)).collect(Collectors.toSet());
        this.schemaService.deleteExpired(filterTableNames);
    }
}