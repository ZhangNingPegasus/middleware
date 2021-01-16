package org.wyyt.sharding.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * the entity of actual SQL statement executed
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Data
public final class FactSql implements Serializable {
    private static final long serialVersionUID = 1L;

    private String datasourceName;
    private String sql;
}