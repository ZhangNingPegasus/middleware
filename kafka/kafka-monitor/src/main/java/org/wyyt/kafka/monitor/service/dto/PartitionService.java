package org.wyyt.kafka.monitor.service.dto;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.wyyt.kafka.monitor.common.Constants;
import org.wyyt.kafka.monitor.config.PropertyConfig;
import org.wyyt.kafka.monitor.entity.dto.SysAdmin;
import org.wyyt.kafka.monitor.entity.po.Partition;
import org.wyyt.kafka.monitor.entity.po.PartitionInfo;
import org.wyyt.kafka.monitor.mapper.SchemaMapper;
import org.wyyt.tool.sql.SqlTool;

import java.util.*;

/**
 * The service for partition table
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class PartitionService extends ServiceImpl<SchemaMapper, SysAdmin> {
    private final PropertyConfig propertyConfig;


    public PartitionService(PropertyConfig propertyConfig) {
        this.propertyConfig = propertyConfig;
    }

    public List<PartitionInfo> getPartitionInfoList() {
        final List<PartitionInfo> result = new ArrayList<>();
        final Date today = DateUtil.parse(DateUtil.today());
        final int retentionDays = this.propertyConfig.getRetentionDays();
        for (int i = -retentionDays; i <= 2; i++) {
            final Date date = DateUtils.addDays(today, i);
            final String strDate = DateUtil.format(date, Constants.DATE_FORMAT);
            final PartitionInfo partitionInfo = new PartitionInfo();
            partitionInfo.setPartitionName(generatePartitionName(strDate));
            partitionInfo.setPartitionDescr(strDate);
            result.add(partitionInfo);
        }
        return result;
    }

    public String generatePartitionName(final String date) {
        return String.format("p_%s", date.replace("-", ""));
    }

    public String generatePartitionName(final Date date) {
        final String strDate = DateUtil.format(date, Constants.DATE_FORMAT);
        return this.generatePartitionName(strDate);
    }

    public Map<String, List<Partition>> getPartition(final Collection<String> tableNameList) {
        final Map<String, List<Partition>> result = new HashMap<>();
        final List<Partition> partitionList = this.baseMapper.getPartition(tableNameList);

        for (final Partition partition : partitionList) {
            String date = SqlTool.removeMySqlCharQualifier(partition.getDescription());
            if ("MAXVALUE".equalsIgnoreCase(date)) {
                partition.setDate(new Date(Long.MAX_VALUE));
            } else {
                partition.setDate(DateUtil.parse(date, Constants.DATE_FORMAT));
            }

            if (result.containsKey(partition.getTableName())) {
                result.get(partition.getTableName()).add(partition);
            } else {
                final List<Partition> buffer = new ArrayList<>();
                buffer.add(partition);
                result.put(partition.getTableName(), buffer);
            }
        }
        for (final Map.Entry<String, List<Partition>> pair : result.entrySet()) {
            pair.getValue().sort(Comparator.comparing(Partition::getDate));
        }
        return result;
    }

    public boolean containsDate(final List<Partition> partitionList,
                                final Date date) {
        for (final Partition partition : partitionList) {
            if (partition.getDate().compareTo(date) == 0) {
                return true;
            }
        }
        return false;
    }

    public void addPartitions(final String tableName,
                              final List<PartitionInfo> partitionInfoList) {
        if (partitionInfoList.isEmpty()) {
            return;
        }
        this.baseMapper.addPartitions(tableName, partitionInfoList);
    }

    public void removePartitions(final String tableName,
                                 final Set<String> partitionNameList) {
        if (partitionNameList.isEmpty()) {
            return;
        }
        this.baseMapper.removePartitions(tableName, partitionNameList);
    }

    public List<Partition> beforeDate(final List<Partition> partitionList,
                                      final Date date) {
        final List<Partition> result = new ArrayList<>();
        for (Partition partition : partitionList) {
            if (partition.getDate().before(date)) {
                result.add(partition);
            }
        }
        return result;
    }

    public PartitionInfo generateMaxValue() {
        final PartitionInfo result = new PartitionInfo();
        result.setPartitionName(Constants.MAX_VALUE_PARTITION_NAME);
        result.setPartitionDescr(Constants.MAX_VALUE);
        return result;
    }
}