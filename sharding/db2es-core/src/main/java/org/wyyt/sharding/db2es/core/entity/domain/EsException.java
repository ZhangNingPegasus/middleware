package org.wyyt.sharding.db2es.core.entity.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.elasticsearch.action.bulk.BulkItemResponse;

/**
 * The domain entity for Exception of Elastic-Search.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@AllArgsConstructor
@Data
public final class EsException {
    private BulkItemResponse bulkItemResponse;
    private Exception exception;
}