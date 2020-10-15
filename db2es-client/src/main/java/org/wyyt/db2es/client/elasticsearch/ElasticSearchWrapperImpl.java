package org.wyyt.db2es.client.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.wyyt.db2es.client.common.Context;
import org.wyyt.db2es.client.entity.FlatMessge;
import org.wyyt.db2es.core.entity.domain.TopicType;
import org.wyyt.db2es.core.util.elasticsearch.ElasticSearchUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * the wapper class of Elatic-Search, which providing each of methods to manipulate Elsatic-Search with Optimistic Locking
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
public class ElasticSearchWrapperImpl extends ElasticSearchWrapper {
    public ElasticSearchWrapperImpl(final Context context) {
        super(context);
    }

    @Override
    public final List<IndexRequest> toInsertRequest(final FlatMessge flatMessage) throws Exception {
        return ElasticSearchUtils.toInsertRequest(this.restHighLevelClient,
                flatMessage,
                this.context.getConfig(),
                TopicType.IN_USE,
                true
        );
    }

    @Override
    public final List<DeleteRequest> toDeleteRequest(final FlatMessge flatMessage) throws Exception {
        return ElasticSearchUtils.toDeleteRequest(this.restHighLevelClient,
                flatMessage,
                this.context.getConfig(),
                TopicType.IN_USE,
                true);
    }

    @Override
    public final List<DocWriteRequest> toUpdateRequest(final FlatMessge flatMessage) throws Exception {
        return new ArrayList<>(toInsertRequest(flatMessage));
    }
}