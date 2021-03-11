package org.wyyt.sharding.db2es.client.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.wyyt.sharding.db2es.client.common.Context;
import org.wyyt.sharding.db2es.client.entity.FlatMessage;
import org.wyyt.sharding.db2es.core.entity.domain.TopicType;
import org.wyyt.sharding.db2es.core.util.elasticsearch.ElasticSearchUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * the wrapper class of Elastic-Search, which providing each of methods to manipulate Elastic-Search with Optimistic Locking
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public class ElasticSearchWrapperImpl extends ElasticSearchWrapper {
    public ElasticSearchWrapperImpl(final Context context) {
        super(context);
    }

    @Override
    public final List<IndexRequest> toInsertRequest(final FlatMessage flatMessage) throws Exception {
        return ElasticSearchUtils.toInsertRequest(this.restHighLevelClient,
                flatMessage,
                this.context.getConfig(),
                TopicType.IN_USE,
                true
        );
    }

    @Override
    public final List<DeleteRequest> toDeleteRequest(final FlatMessage flatMessage) throws Exception {
        return ElasticSearchUtils.toDeleteRequest(this.restHighLevelClient,
                flatMessage,
                this.context.getConfig(),
                TopicType.IN_USE,
                true);
    }

    @Override
    public final List<DocWriteRequest<?>> toUpdateRequest(final FlatMessage flatMessage) throws Exception {
        return new ArrayList<>(toInsertRequest(flatMessage));
    }
}