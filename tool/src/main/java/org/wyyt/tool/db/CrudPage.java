package org.wyyt.tool.db;

import lombok.Data;

import java.util.List;

/**
 * The database page used for dynamic sql
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public class CrudPage<T> {
    private List<T> records;
    private long total;
}
