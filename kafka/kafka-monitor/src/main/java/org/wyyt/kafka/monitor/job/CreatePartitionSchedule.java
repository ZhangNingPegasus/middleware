package org.wyyt.kafka.monitor.job;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wyyt.kafka.monitor.common.Constants;
import org.wyyt.kafka.monitor.entity.po.Partition;
import org.wyyt.kafka.monitor.entity.po.PartitionInfo;
import org.wyyt.kafka.monitor.service.dto.PartitionService;
import org.wyyt.kafka.monitor.service.dto.SchemaService;
import org.wyyt.tool.exception.ExceptionTool;

import java.util.*;

/**
 * The schedule job for creating partition table.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Slf4j
@Component
public class CreatePartitionSchedule {
    private final SchemaService schemaService;
    private final PartitionService partitionService;

    public CreatePartitionSchedule(final SchemaService schemaService,
                                   final PartitionService partitionService) {
        this.schemaService = schemaService;
        this.partitionService = partitionService;
    }

    //每天晚上10点,建立未来两天的partition
    @Scheduled(cron = "0 0 22 1/1 * ?")
    public void createPartition() throws Exception {
        final Set<String> tableNameList = this.schemaService.listPartitionTables();
        final Map<String, List<Partition>> partitionMap = this.partitionService.getPartition(tableNameList);

        final Date maxDate = new Date(Long.MAX_VALUE);
        final Date today = DateUtil.parse(DateUtil.today(), Constants.DATE_FORMAT);
        final Date day1 = DateUtils.addDays(today, 1);
        final Date day2 = DateUtils.addDays(day1, 1);
        boolean needDeleteMaxPartition;

        for (final Map.Entry<String, List<Partition>> pair : partitionMap.entrySet()) {
            try {
                final List<PartitionInfo> partitionInfoList = new ArrayList<>();
                needDeleteMaxPartition = false;
                if (!this.partitionService.containsDate(pair.getValue(), day1)) {
                    final PartitionInfo partitionInfo = new PartitionInfo();
                    final String strDate = DateUtil.format(day1, Constants.DATE_FORMAT);
                    partitionInfo.setPartitionName(this.partitionService.generatePartitionName(strDate));
                    partitionInfo.setPartitionDescr(String.format("'%s'", strDate));
                    partitionInfoList.add(partitionInfo);
                }
                if (!this.partitionService.containsDate(pair.getValue(), day2)) {
                    final PartitionInfo partitionInfo = new PartitionInfo();
                    final String strDate = DateUtil.format(day2, Constants.DATE_FORMAT);
                    partitionInfo.setPartitionName(this.partitionService.generatePartitionName(strDate));
                    partitionInfo.setPartitionDescr(String.format("'%s'", strDate));
                    partitionInfoList.add(partitionInfo);
                }
                if (this.partitionService.containsDate(pair.getValue(), maxDate)) {
                    needDeleteMaxPartition = true;
                } else {
                    partitionInfoList.add(this.partitionService.generateMaxValue());
                }

                if (!partitionInfoList.isEmpty()) {
                    if (needDeleteMaxPartition) {
                        this.partitionService.removePartitions(pair.getKey(), new HashSet<>(Collections.singletonList(Constants.MAX_VALUE_PARTITION_NAME)));
                        partitionInfoList.add(this.partitionService.generateMaxValue());
                    }
                    this.partitionService.addPartitions(pair.getKey(), partitionInfoList);
                }
            } catch (final Exception exception) {
                log.error(String.format("Create partition failed caused by %s", ExceptionTool.getRootCauseMessage(exception)), exception);
            }
        }
    }
}