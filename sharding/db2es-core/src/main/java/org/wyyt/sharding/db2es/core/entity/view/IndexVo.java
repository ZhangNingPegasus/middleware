package org.wyyt.sharding.db2es.core.entity.view;

import lombok.Data;

/**
 * The View Object for Index of Elastic-Search.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public final class IndexVo {
    private String index;
    private String shard;
    private String prirep;
    private String state;
    private String docs;
    private String store;
    private String ip;
    private String node;
}