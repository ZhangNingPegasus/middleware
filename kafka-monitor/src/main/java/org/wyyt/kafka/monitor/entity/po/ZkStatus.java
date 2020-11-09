package org.wyyt.kafka.monitor.entity.po;

import lombok.Data;

/**
 * The status of zookeeper
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Data
public class ZkStatus {
    private String mode;
    private String version;
}
