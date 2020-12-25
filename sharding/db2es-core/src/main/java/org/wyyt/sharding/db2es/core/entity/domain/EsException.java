package org.wyyt.sharding.db2es.core.entity.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.elasticsearch.action.bulk.BulkItemResponse;

/**
 * The domain entity for Exception of Elastic-Search.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@AllArgsConstructor
@Data
public final class EsException {
    private BulkItemResponse bulkItemResponse;
    private Exception exception;
}