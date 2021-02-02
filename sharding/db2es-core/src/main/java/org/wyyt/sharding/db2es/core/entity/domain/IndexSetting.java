package org.wyyt.sharding.db2es.core.entity.domain;

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
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
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