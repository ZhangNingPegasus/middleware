package org.wyyt.sharding.db2es.core.entity.view;

import lombok.Data;

/**
 * The View Entity for Index Alias of Elastic-Search.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public final class AliasVo {
    private String alias;
    private String index;
}