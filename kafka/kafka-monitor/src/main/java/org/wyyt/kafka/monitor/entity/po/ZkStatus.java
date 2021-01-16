package org.wyyt.kafka.monitor.entity.po;

import lombok.Data;

/**
 * The status of zookeeper
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Data
public class ZkStatus {
    private String mode;
    private String version;
}
