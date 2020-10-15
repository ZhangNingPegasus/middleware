package org.wyyt.db2es.core.entity.view;

import lombok.Data;

/**
 * The View Entity for Index Alias of Elastic-Search.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Data
public final class AliasVo {
    private String alias;
    private String index;
}