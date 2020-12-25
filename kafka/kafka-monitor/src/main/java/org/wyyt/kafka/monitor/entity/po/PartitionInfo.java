package org.wyyt.kafka.monitor.entity.po;

import lombok.Data;

/**
 * the entity class for partition table
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Data
public class PartitionInfo {
    private String partitionName;
    private String partitionDescr;
}