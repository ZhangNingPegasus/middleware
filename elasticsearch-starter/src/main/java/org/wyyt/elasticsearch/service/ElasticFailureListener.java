package org.wyyt.elasticsearch.service;

import org.elasticsearch.client.Node;
import org.elasticsearch.client.RestClient;
import org.wyyt.elasticsearch.exception.ElasticSearchException;

/**
 * the exception listner of Elastic-Search
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
        throw new ElasticSearchException(String.format("连接Node节点超时,%s", node.toString()));
    }
}