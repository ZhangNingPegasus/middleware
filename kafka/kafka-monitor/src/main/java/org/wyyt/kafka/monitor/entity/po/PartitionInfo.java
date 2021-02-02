package org.wyyt.kafka.monitor.entity.po;

import lombok.Data;

/**
 * the entity class for partition table
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public class PartitionInfo {
    private String partitionName;
    private String partitionDescr;
}