package org.wyyt.elasticsearch.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ObjectUtils;
import org.wyyt.elasticsearch.auto.property.ElasticSearchProperty;
import org.wyyt.elasticsearch.exception.ElasticSearchException;
import org.wyyt.elasticsearch.page.IPage;
import org.wyyt.elasticsearch.page.SortItem;

import java.io.IOException;
import java.util.*;

/**
 * the service of Elastic-Search manipulation
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class ElasticSearchService implements InitializingBean, DisposableBean {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final ElasticSearchProperty elasticSearchProperty;
    private RestHighLevelClient restHighLevelClient;

    public ElasticSearchService(final ElasticSearchProperty elasticSearchProperty) {
        this.elasticSearchProperty = elasticSearchProperty;
    }

    public int getNodesCount() {
        return this.elasticSearchProperty.getHostnames().size();
    }

    public RestHighLevelClient getRestHighLevelClient() {
        return this.restHighLevelClient;
    }

    public final boolean insert(final String indexName,
                                final String id,
                                final Map<String, Object> source,
                                final long version) throws IOException {
        final IndexRequest request = new IndexRequest(indexName)
                .id(id)
                .source(source)
                .versionType(VersionType.EXTERNAL)
                .version(version);
        final IndexResponse response = this.restHighLevelClient.index(request, RequestOptions.DEFAULT);
        return (0 == response.getShardInfo().getFailed());
    }

    public boolean insert(final String indexName,
                          final String id,
                          final Map<String, Object> source) throws IOException {
        final IndexRequest request = new IndexRequest(indexName)
                .id(id)
                .source(source);
        final IndexResponse response = this.restHighLevelClient.index(request, RequestOptions.DEFAULT);
        return (0 == response.getShardInfo().getFailed());
    }

    public final boolean update(final String indexName,
                                final String id,
                                final Map<String, Object> source) throws IOException {
        final UpdateRequest updateRequest = new UpdateRequest(indexName, id).doc(source);
        final UpdateResponse response = this.restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        return (0 == response.getShardInfo().getFailed());
    }

    public final boolean delete(final String indexName,
                                final String id) throws IOException {
        final DeleteRequest request = new DeleteRequest(indexName, id);
        final DeleteResponse response = this.restHighLevelClient.delete(request, RequestOptions.DEFAULT);
        return (0 == response.getShardInfo().getFailed());
    }

    public final boolean delete(final String indexName,
                                final QueryBuilder queryBuilder) throws IOException {
        final DeleteByQueryRequest request = new DeleteByQueryRequest(indexName);
        request.setQuery(queryBuilder);
        final BulkByScrollResponse resp = this.restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
        return (0 == resp.getBulkFailures().size());
    }

    public final BulkResponse bulk(final BulkRequest request) throws IOException {
        return this.restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
    }

    public final boolean dropIndex(final String indexName) throws IOException {
        final DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        final AcknowledgedResponse response = this.restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        return response.isAcknowledged();
    }

    public void curl(final String method,
                     final String endpoint,
                     final Handle handle) throws IOException {
        final RestClient restClient = this.getRestHighLevelClient().getLowLevelClient();
        final Response response = restClient.performRequest(new Request(method, endpoint));
        if (null != response) {
            final String rawBody = EntityUtils.toString(response.getEntity());
            handle.process(MAPPER.readValue(rawBody, new TypeReference<List<HashMap<String, String>>>() {
            }));
        }
    }

    public final long count(final String indexName) throws IOException {
        final CountResponse response = this.restHighLevelClient.count(new CountRequest(indexName), RequestOptions.DEFAULT);
        return response.getCount();
    }

    public final long count(final String indexName,
                            final QueryBuilder queryBuilder) throws IOException {
        final CountResponse response = this.restHighLevelClient.count(new CountRequest(new String[]{indexName}, queryBuilder), RequestOptions.DEFAULT);
        return response.getCount();
    }

    public final Set<String> listIndexNames() throws IOException {
        final GetAliasesRequest request = new GetAliasesRequest();
        final GetAliasesResponse getAliasesResponse = this.restHighLevelClient.indices().getAlias(request, RequestOptions.DEFAULT);
        final Map<String, Set<AliasMetadata>> aliases = getAliasesResponse.getAliases();
        return aliases.keySet();
    }

    public final List<Map<String, Object>> getByColumnValue(final String indexName,
                                                            final String columnName,
                                                            final String columnValue) throws IOException {
        final SearchRequest searchRequest = new SearchRequest(indexName);

        final BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.termQuery(columnName, columnValue));
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);

        final List<String> response = this.select(searchRequest, String.class);
        final List<Map<String, Object>> result = new ArrayList<>(response.size());
        for (final String s : response) {
            result.add(JSONObject.parseObject(s));
        }
        return result;
    }

    public final <T> T getById(final String alias,
                               final String id,
                               final Class<T> tClass) throws IOException {
        final EsResult esResult = getDetailById(alias, id);
        if (null == esResult) {
            return null;
        }
        return parseRow(esResult.getGetResponse().getSourceAsString(), tClass);
    }

    public final EsResult getDetailById(final String alias,
                                        final String id) throws IOException {
        final Set<String> indexNameSet = getIndexNameByAlias(alias);
        if (null == indexNameSet || indexNameSet.isEmpty()) {
            return _getById(alias, id);
        } else {
            final List<EsResult> esResultList = new ArrayList<>();
            for (final String indexName : indexNameSet) {
                final EsResult esResult = _getById(indexName, id);
                if (null != esResult) {
                    esResultList.add(esResult);
                }
            }
            if (1 == esResultList.size()) {
                return esResultList.get(0);
            } else if (esResultList.size() > 1) {
                throw new ElasticSearchException(String.format("期待只获取一条记录，但在ElasticSearch索引[%s]中查出了多条id=%s的记录", alias, id));
            }
        }
        return null;
    }

    public final Map<String, Object> getById(final String indexName,
                                             final String id) throws IOException {
        final EsResult esResult = getDetailById(indexName, id);
        if (null == esResult) {
            return null;
        }
        return esResult.getData();
    }

    public final <T> List<T> select(final SearchRequest searchRequest,
                                    final Class<T> tClass) throws IOException {
        if (null == searchRequest || null == tClass) {
            return null;
        }
        final SearchResponse search = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        final List<T> result = new ArrayList<>(search.getHits().getHits().length);
        for (final SearchHit hit : search.getHits().getHits()) {
            result.add(parseRow(hit.getSourceAsString(), tClass));
        }
        return result;
    }

    public final <T> IPage<T> page(final SearchRequest searchRequest,
                                   final Class<T> tClass,
                                   final IPage<T> page) throws IOException {
        SearchSourceBuilder source = searchRequest.source();
        if (null == source) {
            source = new SearchSourceBuilder();
        }

        final long current = page.getCurrent();
        final long size = page.getSize();
        final long from = (current - 1) * size;

        if (from < 0) {
            throw new ElasticSearchException("from must be greater than or equal to 0");
        }
        if (size < 0) {
            throw new ElasticSearchException("size must be greater than or equal to 0");
        }
        source.from((int) from);
        source.size((int) size);
        final List<SortItem> sortItemList = page.sortItemList();
        if (null != sortItemList) {
            for (final SortItem sortItem : sortItemList) {
                source.sort(sortItem.getColumn(), sortItem.isAsc() ? SortOrder.ASC : SortOrder.DESC);
            }
        }
        final SearchResponse search = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        final List<T> result = new ArrayList<>(search.getHits().getHits().length);
        for (final SearchHit hit : search.getHits().getHits()) {
            result.add(parseRow(hit.getSourceAsString(), tClass));
        }
        page.setRecords(result);
        page.setTotal(search.getHits().getTotalHits().value);
        page.setRelation(search.getHits().getTotalHits().relation);
        return page;
    }

    public final <T> T selectOne(final SearchRequest searchRequest,
                                 final Class<T> tClass) throws IOException {
        final List<T> result = select(searchRequest, tClass);
        if (null == result || result.size() < 1) {
            return null;
        } else if (result.size() > 1) {
            throw new ElasticSearchException("期待只获取一条记录，但在ElasticSearch中查出了多条符合条件的记录");
        }
        return result.get(0);
    }

    public final boolean exists(final String indexName) throws IOException {
        return this.restHighLevelClient.indices().exists(new GetIndexRequest(indexName), RequestOptions.DEFAULT);
    }

    @Override
    public void afterPropertiesSet() {
        final List<HttpHost> httpHostList = new ArrayList<>();
        for (final String hostname : this.elasticSearchProperty.getHostnames()) {
            String ip;
            int port = 9200;
            final String[] all = hostname.split(":");
            if (1 == all.length) {
                ip = all[0].trim();
            } else if (2 == all.length) {
                ip = all[0].trim();
                port = Integer.parseInt(all[1].trim());
            } else {
                throw new ElasticSearchException(String.format("[%s]不符合要求", hostname));
            }
            httpHostList.add(new HttpHost(ip, port));
        }

        final RestClientBuilder restClientBuilder = RestClient.builder(httpHostList.toArray(new HttpHost[]{}))
                .setRequestConfigCallback(builder -> {
                    builder.setConnectTimeout(1000 * 30);
                    builder.setSocketTimeout(1000 * 60 * 2);
                    builder.setConnectionRequestTimeout(1000 * 30);
                    return builder;
                })
                .setHttpClientConfigCallback(builder -> {
                    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(AuthScope.ANY,
                            new UsernamePasswordCredentials(
                                    this.elasticSearchProperty.getUsername(),  //ElasticSearch账号
                                    this.elasticSearchProperty.getPassword())); //ElasticSearch密码

                    final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                            .setConnectTimeout(1000 * 30)
                            .setSoTimeout(1000 * 60 * 2)
                            .setRcvBufSize(4096)
                            .setSndBufSize(8192)
                            .setSoKeepAlive(true)
                            .build();
                    PoolingNHttpClientConnectionManager connManager = null;
                    try {
                        connManager = new PoolingNHttpClientConnectionManager(new
                                DefaultConnectingIOReactor(ioReactorConfig));
                        connManager.setMaxTotal(20);
                        connManager.setDefaultMaxPerRoute(5);
                    } catch (final IOReactorException e) {
                        log.error(String.format("ElasticSearchService: init Connection Manager meet error with %s", e.getMessage()), e);
                    }
                    builder.setMaxConnTotal(this.elasticSearchProperty.getMaxConnTotal()); //多线程访问时最大并发量
                    builder.setMaxConnPerRoute(this.elasticSearchProperty.getMaxConnPerRoute()); //单次路由线程上限
                    builder.disableAuthCaching();

                    builder.setDefaultIOReactorConfig(ioReactorConfig);
                    builder.setConnectionManager(connManager);

                    builder.setDefaultCredentialsProvider(credentialsProvider);
                    return builder;
                });
        restClientBuilder.setFailureListener(new ElasticFailureListener());
        this.restHighLevelClient = new RestHighLevelClient(restClientBuilder);
    }

    @Override
    public final void destroy() throws Exception {
        if (null != this.restHighLevelClient) {
            this.restHighLevelClient.close();
        }
    }

    @Data
    @AllArgsConstructor
    public final static class EsResult {
        private GetResponse getResponse;
        private Map<String, Object> data;
    }

    public interface Handle {
        void process(final List<HashMap<String, String>> dataList);
    }

    private EsResult _getById(final String indexName,
                              final String id) throws IOException {
        final GetRequest getRequest = new GetRequest(indexName, id);
        final GetResponse getResponse = this.restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        if (getResponse.isExists()) {
            return new EsResult(getResponse, JSONObject.parseObject(getResponse.getSourceAsString()));
        }
        return null;
    }

    private Set<String> getIndexNameByAlias(final String alias) throws IOException {
        if (ObjectUtils.isEmpty(alias)) {
            return null;
        }
        final Set<String> result = new HashSet<>();
        final GetAliasesRequest request = new GetAliasesRequest(alias);
        final GetAliasesResponse response = this.restHighLevelClient.indices().getAlias(request, RequestOptions.DEFAULT);
        for (final Map.Entry<String, Set<AliasMetadata>> pair : response.getAliases().entrySet()) {
            result.add(pair.getKey());
        }
        return result;
    }

    private static <T> T parseRow(final String row,
                                  final Class<T> tClass) {
        if (tClass.isAssignableFrom(String.class)) {
            return (T) row;
        } else {
            return JSON.parseObject(row, tClass);
        }
    }
}