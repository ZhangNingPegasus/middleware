package org.wyyt.sharding.db2es.client.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.utils.CloseableUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.IOReactorException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.util.ObjectUtils;
import org.wyyt.sharding.db2es.client.common.Constant;
import org.wyyt.sharding.db2es.client.common.Context;
import org.wyyt.sharding.db2es.client.entity.Db2EsLog;
import org.wyyt.sharding.db2es.client.entity.FlatMessge;
import org.wyyt.sharding.db2es.core.entity.domain.EsException;
import org.wyyt.sharding.db2es.core.entity.domain.IndexName;
import org.wyyt.sharding.db2es.core.entity.domain.TopicType;
import org.wyyt.sharding.db2es.core.entity.persistent.Topic;
import org.wyyt.sharding.db2es.core.exception.Db2EsException;
import org.wyyt.sharding.db2es.core.util.elasticsearch.ElasticSearchUtils;
import org.wyyt.sharding.db2es.core.util.flatmsg.FlatMsgUtils;
import org.wyyt.sharding.db2es.core.util.flatmsg.Operation;
import org.wyyt.tool.dingtalk.WarningLevel;
import org.wyyt.tool.exception.ExceptionTool;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

/**
 * the base wapper class of Elatic-Search, which providing each of methods to manipulate Elsatic-Search
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
public abstract class ElasticSearchWrapper implements Closeable {
    private static final int BATCH_SIZE = (int) (Constant.CAPACITY * 1.2);
    protected final RestHighLevelClient restHighLevelClient;
    protected final Context context;

    public ElasticSearchWrapper(final Context context) {
        this.context = context;
        this.restHighLevelClient = new RestHighLevelClient(generateRestClientBuilder());
        log.info(String.format("ElasticSearchWrapper: initialize the ElasticSearch with server[%s] with successfully", this.context.getConfig().getEsHost()));
    }

    public final int populate(final List<FlatMessge> flatMessageList) throws Exception {
        if (null == flatMessageList || flatMessageList.isEmpty()) {
            return 0;
        }
        final List<Request> requestList = new ArrayList<>(flatMessageList.size());
        final List<Db2EsLog> db2EsLogList = new ArrayList<>();

        FlatMsgUtils.operate(flatMessageList, new Operation<FlatMessge>() {
            @Override
            public final void insert(final FlatMessge flatMessage) throws Exception {
                final List<IndexRequest> insertRequestList = toInsertRequest(flatMessage);
                if (null != insertRequestList && !insertRequestList.isEmpty()) {
                    for (final IndexRequest indexRequest : insertRequestList) {
                        requestList.add(new Request(indexRequest, flatMessage));
                    }
                }
            }

            @Override
            public final void delete(final FlatMessge flatMessage) throws Exception {
                final List<DeleteRequest> deleteRequestList = toDeleteRequest(flatMessage);
                if (null != deleteRequestList && !deleteRequestList.isEmpty()) {
                    for (final DeleteRequest deleteRequest : deleteRequestList) {
                        requestList.add(new Request(deleteRequest, flatMessage));
                    }
                }
            }

            @Override
            public final void update(final FlatMessge flatMessage) throws Exception {
                final List<DocWriteRequest> updateRequestList = toUpdateRequest(flatMessage);
                if (null != updateRequestList && !updateRequestList.isEmpty()) {
                    for (final DocWriteRequest docWriteRequest : updateRequestList) {
                        requestList.add(new Request(docWriteRequest, flatMessage));
                    }
                }
            }

            @Override
            public final void exception(final FlatMessge flatMessage, final Exception exception) throws Exception {
                if (!context.getConfig().getContinueOnError()) {
                    throw exception;
                }

                final Db2EsLog db2EsLog = new Db2EsLog(context, flatMessage);
                db2EsLog.setIndexName("");
                db2EsLog.setErrorMessage(ExceptionTool.getRootCauseMessage(exception));
                db2EsLogList.add(db2EsLog);
                log.error(String.format("ElasticSearchWrapper: process record meet error, %s", ExceptionTool.getRootCauseMessage(exception)), exception);
            }
        });

        if (!requestList.isEmpty()) {
            int from = 0, to = from + BATCH_SIZE;
            while (from < requestList.size()) {
                if (to > requestList.size()) {
                    to = requestList.size();
                }
                this.bulk(requestList.subList(from, to), db2EsLogList);
                from = to;
                to = from + BATCH_SIZE;
            }
        }

        if (this.context.getConfig().getContinueOnError() && !db2EsLogList.isEmpty()) {
            this.context.getDbWrapper().insertLogs(db2EsLogList);

            final Db2EsLog firstDb2EsLog = db2EsLogList.get(0);
            this.context.getDingDingWrapper().sendIfNoDuplicate(String.format("在kafka主题[%s]中, 有部分消息同步失败, 部分错误原因: %s, 详情请检查DB2ES_ADMIN中的[错误列表]功能. %s",
                    firstDb2EsLog.getTopicName(),
                    firstDb2EsLog.getErrorMessage(),
                    String.format("http://%s:%s", this.context.getConfig().getDb2esAdminHost(), this.context.getConfig().getDb2esAdminPort())),
                    WarningLevel.CRITICAL);
        }

        final int result = requestList.size();
        requestList.clear();
        db2EsLogList.clear();
        return result;
    }

    public final String getIndexName(final String alias,
                                     final Date rowCreateTime,
                                     final boolean checkInMemory) throws Exception {
        return ElasticSearchUtils.getIndexName(this.restHighLevelClient,
                this.context.getConfig(),
                alias,
                rowCreateTime,
                TopicType.IN_USE,
                checkInMemory);
    }

    public final void refreshTopicSuffix(final Topic topic) throws Exception {
        ElasticSearchUtils.refreshTopicSuffix(this.restHighLevelClient, topic);
    }

    public final Map<String, Set<IndexName>> getIndexNameByAlias(final List<String> aliasList) throws IOException {
        return ElasticSearchUtils.getIndexNameByAlias(this.restHighLevelClient, aliasList);
    }

    public final Set<IndexName> getIndexNameByAlias(final String alias) throws IOException {
        return ElasticSearchUtils.getIndexNameByAlias(this.restHighLevelClient, alias);
    }

    public final IndexName getIndexNameByAlias(final String alias, final int year) throws IOException {
        return ElasticSearchUtils.getIndexNameByAlias(this.restHighLevelClient, alias, year);
    }

    private void bulk(final List<Request> requestList,
                      final List<Db2EsLog> db2EsLogList) throws IOException {
        if (null == requestList || requestList.isEmpty()) {
            return;
        }
        final List<FlatMessge> flatMessageList = new ArrayList<>(requestList.size());
        final BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout(TimeValue.timeValueMinutes(1)); //bulk请求执行的超时。默认是1分钟
        //设置刷新策略
        //IMMEDIATE: 请求向ElasticSearch提交了数据，立即进行数据刷新，然后再结束请求。优点：实时性高、操作延时短; 缺点：资源消耗高
        //WAIT_UNTIL: 请求向ElasticSearch提交了数据，等待数据完成刷新，然后再结束请求。优点：实时性高、资源消耗低; 缺点：操作延时长
        //NONE(默认): 请求向ElasticSearch提交了数据，不关心数据是否已经完成刷新，直接结束请求。优点：操作延时短、资源消耗低; 缺点：实时性低
        bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.NONE);

        for (final Request request : requestList) {
            flatMessageList.add(request.getFlatMessage());
            bulkRequest.add(request.getDocWriteRequest());
        }

        BulkResponse responses = null;
        try {
            responses = this.restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (final Exception exception) {
            log.error(String.format("Bulk operation meet error, %s", ExceptionTool.getRootCauseMessage(exception)), exception);
            if (!this.context.getConfig().getContinueOnError()) {
                throw exception;
            }
            for (final FlatMessge flatMessage : flatMessageList) {
                final Db2EsLog db2EsLog = new Db2EsLog(this.context, flatMessage);
                db2EsLog.setIndexName("");
                db2EsLog.setErrorMessage(ExceptionTool.getRootCauseMessage(exception));
                db2EsLogList.add(db2EsLog);
            }
        }

        final List<EsException> elasticSearchExceptions = ElasticSearchUtils.getElasticSearchExceptions(responses);
        if (null != elasticSearchExceptions && !elasticSearchExceptions.isEmpty()) {
            for (final EsException esException : elasticSearchExceptions) {
                final String errorMsg = ExceptionTool.getRootCauseMessage(esException.getException());
                if (!this.context.getConfig().getContinueOnError()) {
                    throw new Db2EsException(errorMsg);
                }

                final FlatMessge flatMessage = flatMessageList.get(esException.getBulkItemResponse().getItemId());
                final Db2EsLog db2EsLog = new Db2EsLog(this.context, flatMessage);
                db2EsLog.setIndexName(esException.getBulkItemResponse().getIndex());
                db2EsLog.setErrorMessage(errorMsg);
                db2EsLogList.add(db2EsLog);
                log.error(String.format("populate record meet error, caused %s", errorMsg), esException.getException());
            }
        }
        flatMessageList.clear();
    }

    private RestClientBuilder generateRestClientBuilder() {
        final List<HttpHost> httpHostList = new ArrayList<>(12);

        final String hostnames = this.context.getConfig().getEsHost();
        final String[] hostnameArray = hostnames.split(",");
        for (final String hostname : hostnameArray) {
            if (ObjectUtils.isEmpty(hostname.trim())) {
                continue;
            }
            String ip;
            int port = 9200;
            final String[] all = hostname.split(":");
            if (all.length == 1) {
                ip = all[0].trim();
            } else if (all.length == 2) {
                ip = all[0].trim();
                port = Integer.parseInt(all[1].trim());
            } else {
                throw new Db2EsException(String.format("the format of [%s] is not correct", hostname));
            }
            httpHostList.add(new HttpHost(ip, port));
        }

        final RestClientBuilder restClientBuilder = RestClient.builder(httpHostList.toArray(new HttpHost[]{}))
                .setRequestConfigCallback(builder -> {
                    builder.setConnectTimeout(1000 * 30);
                    builder.setSocketTimeout(1000 * 60);
                    builder.setConnectionRequestTimeout(1000 * 30);
                    return builder;
                }).setHttpClientConfigCallback(builder -> {
                    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(AuthScope.ANY,
                            new UsernamePasswordCredentials(
                                    this.context.getConfig().getEsUid(),
                                    this.context.getConfig().getEsPwd())
                    );

                    final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                            .setConnectTimeout(1000 * 30)
                            .setSoTimeout(1000 * 60 * 2)
                            .setRcvBufSize(8192)
                            .setSndBufSize(8192)
                            .setSoKeepAlive(true)
                            .build();
                    PoolingNHttpClientConnectionManager connManager = null;
                    try {
                        connManager = new PoolingNHttpClientConnectionManager(new
                                DefaultConnectingIOReactor(ioReactorConfig));
                    } catch (IOReactorException exception) {
                        log.error(String.format("ElasticSearchWrapper: create class[PoolingNHttpClientConnectionManager] meet an error, %s",
                                ExceptionTool.getRootCauseMessage(exception)), exception);
                    }

                    builder.setMaxConnTotal(500); //多线程访问时最大并发量
                    builder.setMaxConnPerRoute(200); //单次路由线程上限
                    builder.disableAuthCaching();
                    builder.setDefaultIOReactorConfig(ioReactorConfig);
                    builder.setConnectionManager(connManager);
                    builder.setDefaultCredentialsProvider(credentialsProvider);
                    return builder;
                });

        restClientBuilder.setFailureListener(new ElasticFailureListener());
        return restClientBuilder;
    }

    @Override
    public final void close() {
        CloseableUtils.closeQuietly(this.restHighLevelClient);
    }

    @Data
    @AllArgsConstructor
    private static class Request {
        private DocWriteRequest<?> docWriteRequest;
        private FlatMessge flatMessage;
    }

    abstract List<IndexRequest> toInsertRequest(final FlatMessge flatMessage) throws Exception;

    abstract List<DeleteRequest> toDeleteRequest(final FlatMessge flatMessage) throws Exception;

    abstract List<DocWriteRequest> toUpdateRequest(final FlatMessge flatMessage) throws Exception;
}