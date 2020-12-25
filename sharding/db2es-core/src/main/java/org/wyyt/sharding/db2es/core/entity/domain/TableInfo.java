package org.wyyt.sharding.db2es.core.entity.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * the domain entity of Table
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableInfo {
    private String primaryKeyFieldName;
    private String rowCreateTimeFieldName;
    private String rowUpdateTimeFieldName;
}