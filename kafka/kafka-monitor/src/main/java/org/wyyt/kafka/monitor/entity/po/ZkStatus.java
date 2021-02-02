package org.wyyt.kafka.monitor.entity.po;

import lombok.Data;

/**
 * The status of zookeeper
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public class ZkStatus {
    private String mode;
    private String version;
}
