package org.wyyt.db2es.core.entity.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * the domain entity of index setting
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class IndexSetting {
    private String numberOfReplicas;
    private String refreshInterval;

    public final boolean isOptimize() {
        if (null == this.refreshInterval || null == this.numberOfReplicas) {
            return false;
        }
        return this.refreshInterval.startsWith("-1") && "0".equals(this.numberOfReplicas);
    }
}