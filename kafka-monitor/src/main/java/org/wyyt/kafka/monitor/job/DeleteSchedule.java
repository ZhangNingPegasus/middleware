package org.wyyt.kafka.monitor.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wyyt.kafka.monitor.config.PropertyConfig;
import org.wyyt.kafka.monitor.service.common.KafkaService;
import org.wyyt.kafka.monitor.service.dto.SchemaService;
import org.wyyt.kafka.monitor.service.dto.TopicRecordService;

import java.util.Arrays;
import java.util.HashSet;
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
    private final Set<String> NO_NEED_TO_DELETE_EXPIRE_RECORDS_TABLES = new HashSet<>(Arrays.asList(
            "sys_admin",
            "sys_alert_cluster",
            "sys_alert_consumer",
            "sys_alert_topic",
            "sys_dingding_config",
            "sys_mail_config",
            "sys_page",
            "sys_permission",
            "sys_role",
            "sys_table_name"
    ));
    private final SchemaService schemaService;
    private final KafkaService kafkaService;
    private final TopicRecordService topicRecordService;
    private final PropertyConfig propertyConfig;

    public DeleteSchedule(final SchemaService schemaService,
                          final KafkaService kafkaService,
                          final TopicRecordService topicRecordService,
                          final PropertyConfig propertyConfig) {
        this.schemaService = schemaService;
        this.kafkaService = kafkaService;
        this.topicRecordService = topicRecordService;
        this.propertyConfig = propertyConfig;
    }

    //每天00:01:00执行一次
    @Scheduled(cron = "0 1 0 1/1 * ?")
    public void deleteExpired() {
        final Set<String> tableNames = this.schemaService.listTables();
        final Set<String> filterTableNames = tableNames.stream().filter(p -> !NO_NEED_TO_DELETE_EXPIRE_RECORDS_TABLES.contains(p)).collect(Collectors.toSet());
        this.schemaService.deleteExpired(filterTableNames);
    }
}