package org.wyyt.db2es.client.elasticsearch;

import org.elasticsearch.client.Node;
import org.elasticsearch.client.RestClient;
import org.wyyt.db2es.core.exception.Db2EsException;

/**
 * the listner which processing the exception when manipulate the Elastic-Search
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public final class ElasticFailureListener extends RestClient.FailureListener {
    @Override
    public final void onFailure(final Node node) {
        throw new Db2EsException(String.format("Connect elastic-search node [%s] timeout", node.toString()));
    }
}