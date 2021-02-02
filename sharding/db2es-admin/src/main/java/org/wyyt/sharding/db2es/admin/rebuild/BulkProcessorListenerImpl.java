package org.wyyt.sharding.db2es.admin.rebuild;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.wyyt.sharding.db2es.core.entity.domain.EsException;
import org.wyyt.sharding.db2es.core.util.elasticsearch.ElasticSearchUtils;

import java.util.List;

/**
 * The implemention of BulkProcessorListener
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class BulkProcessorListenerImpl implements BulkProcessor.Listener {
    private final ExceptionCallback exceptionCallback;

    public BulkProcessorListenerImpl(final ExceptionCallback exceptionCallback) {
        this.exceptionCallback = exceptionCallback;
    }

    @Override
    public void beforeBulk(final long executionId,
                           final BulkRequest bulkRequest) {
    }

    @Override
    public void afterBulk(final long executionId,
                          final BulkRequest bulkRequest,
                          final BulkResponse bulkResponse) {
        final List<EsException> elasticSearchExceptions = ElasticSearchUtils.getElasticSearchExceptions(bulkResponse);
        if (null == elasticSearchExceptions || elasticSearchExceptions.isEmpty()) {
            return;
        }
        this.exceptionCallback.setException(elasticSearchExceptions.get(0).getException());
    }

    @Override
    public void afterBulk(final long executionId,
                          final BulkRequest bulkRequest,
                          final Throwable throwable) {
        if (null == throwable) {
            return;
        }
        this.exceptionCallback.setException(new Exception(throwable));
    }
}