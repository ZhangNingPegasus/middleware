package org.wyyt.sharding.db2es.client.elasticsearch;

import org.elasticsearch.client.Node;
import org.elasticsearch.client.RestClient;
import org.wyyt.sharding.db2es.core.exception.Db2EsException;

/**
 * the listner which processing the exception when manipulate the Elastic-Search
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public final class ElasticFailureListener extends RestClient.FailureListener {
    @Override
    public final void onFailure(final Node node) {
        throw new Db2EsException(String.format("Connect elastic-search node [%s] timeout", node.toString()));
    }
}