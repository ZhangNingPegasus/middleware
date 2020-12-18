package org.wyyt.kafka.monitor.job;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wyyt.kafka.monitor.common.Constants;
import org.wyyt.kafka.monitor.config.PropertyConfig;
import org.wyyt.kafka.monitor.entity.po.Partition;
import org.wyyt.kafka.monitor.service.dto.PartitionService;
import org.wyyt.kafka.monitor.service.dto.SchemaService;
import org.wyyt.tool.exception.ExceptionTool;

import java.util.Date;
import java.util.List;
import java.util.Map;
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
@Slf4j
@Component
public class DeleteSchedule {
    private final SchemaService schemaService;
    private final PartitionService partitionService;
    private final PropertyConfig propertyConfig;

    public DeleteSchedule(final SchemaService schemaService,
                          final PartitionService partitionService,
                          final PropertyConfig propertyConfig) {
        this.schemaService = schemaService;
        this.partitionService = partitionService;
        this.propertyConfig = propertyConfig;
    }

    //每天00:01:00执行一次
    @Scheduled(cron = "0 1 0 1/1 * ?")
    public void deleteExpired() throws Exception {
        final Set<String> tableNameList = this.schemaService.listPartitionTables();
        final Map<String, List<Partition>> partitionMap = this.partitionService.getPartition(tableNameList);

        final Date today = DateUtil.parse(DateUtil.today(), Constants.DATE_FORMAT);
        final Date date = DateUtils.addDays(today, -propertyConfig.getRetentionDays());

        for (final Map.Entry<String, List<Partition>> pair : partitionMap.entrySet()) {
            try {
                final List<Partition> buffer = this.partitionService.beforeDate(pair.getValue(), date);
                final Set<String> partitionNameSet = buffer.stream().map(Partition::getPartitionName).collect(Collectors.toSet());
                if (!partitionNameSet.isEmpty()) {
                    this.partitionService.removePartitions(pair.getKey(), partitionNameSet);
                }
            } catch (final Exception exception) {
                log.error(String.format("Delete expired failed caused by %s", ExceptionTool.getRootCauseMessage(exception)), exception);
            }
        }
    }
}