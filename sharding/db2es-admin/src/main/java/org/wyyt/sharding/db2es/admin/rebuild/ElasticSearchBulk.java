package org.wyyt.sharding.db2es.admin.rebuild;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.wyyt.sharding.db2es.admin.service.common.EsService;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Used for polling the records to Elastic-Search
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
public final class ElasticSearchBulk implements Closeable {
    private final EsService esService;
    private final BulkProcessor bulkProcessor;

    public ElasticSearchBulk(final EsService esService,
                             final ExceptionCallback exceptionCallback) {
        this.esService = esService;
        final BulkProcessorListenerImpl bulkProcessorListener = new BulkProcessorListenerImpl(exceptionCallback);
        this.bulkProcessor = BulkProcessor.builder(
                (request, bulkListener) -> this.esService.getElasticSearchService().getRestHighLevelClient().bulkAsync(request, RequestOptions.DEFAULT, bulkListener), bulkProcessorListener)
                .setBulkActions(5000)
                .setBulkSize(new ByteSizeValue(10, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueSeconds(5))
                .setConcurrentRequests(1)
                .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
                .build();
    }

    public final void add(final DocWriteRequest<?> docWriteRequest) {
        this.bulkProcessor.add(docWriteRequest);
    }

    public final void add(final List<DocWriteRequest> docWriteRequestList) {
        for (final DocWriteRequest docWriteRequest : docWriteRequestList) {
            this.bulkProcessor.add(docWriteRequest);
        }
    }

    public final void addIndexRequests(final List<IndexRequest> indexRequestList) {
        for (final DocWriteRequest<?> docWriteRequest : indexRequestList) {
            this.add(docWriteRequest);
        }
    }

    public final void addUpdateRequests(final List<UpdateRequest> updateRequestList) {
        for (final DocWriteRequest<?> docWriteRequest : updateRequestList) {
            this.add(docWriteRequest);
        }
    }

    public final void addDeleteRequests(final List<DeleteRequest> deleteRequestList) {
        for (final DocWriteRequest<?> docWriteRequest : deleteRequestList) {
            this.add(docWriteRequest);
        }
    }

    public final void flush() {
        this.bulkProcessor.flush();
    }

    @Override
    public final void close() {
        try {
            this.bulkProcessor.awaitClose(1, TimeUnit.MINUTES);
        } catch (final InterruptedException exception) {
            log.error(exception.getMessage(), exception);
        }
    }
}