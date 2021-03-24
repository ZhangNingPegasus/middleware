package org.wyyt.sharding.db2es.admin.service.common;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.elasticsearch.exception.ElasticSearchException;
import org.wyyt.elasticsearch.service.ElasticSearchService;
import org.wyyt.sharding.db2es.admin.service.TopicService;
import org.wyyt.sharding.db2es.core.entity.domain.IndexName;
import org.wyyt.sharding.db2es.core.entity.domain.IndexSetting;
import org.wyyt.sharding.db2es.core.entity.domain.Names;
import org.wyyt.sharding.db2es.core.entity.persistent.Topic;
import org.wyyt.sharding.db2es.core.entity.view.AliasVo;
import org.wyyt.sharding.db2es.core.entity.view.IndexVo;
import org.wyyt.sharding.db2es.core.exception.Db2EsException;
import org.wyyt.sharding.db2es.core.util.elasticsearch.ElasticSearchUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The service for Elastic-Search.
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
@Service
public class EsService {
    @Getter
    private final ElasticSearchService elasticSearchService;
    private final TopicService topicService;

    public EsService(final ElasticSearchService elasticSearchService,
                     final TopicService topicService) {
        this.elasticSearchService = elasticSearchService;
        this.topicService = topicService;
    }

    public List<IndexVo> listIndexVo(final Set<String> indexNames) {
        return ElasticSearchUtils.listIndexVo(this.elasticSearchService.getRestHighLevelClient(), indexNames);
    }

    public List<AliasVo> listAliasVo(final Set<String> indexNames) {
        return ElasticSearchUtils.listAliasVo(this.elasticSearchService.getRestHighLevelClient(), indexNames);
    }

    public Map<String, Long> listCount(final Set<String> indexAliasNames) {
        final Map<String, Long> result = new HashMap<>();
        if (null == indexAliasNames || indexAliasNames.isEmpty()) {
            return result;
        }
        final List<IndexVo> indexVos = listIndexVo(indexAliasNames);
        if (null == indexVos) {
            return result;
        }
        final List<AliasVo> aliasVos = listAliasVo(indexAliasNames);

        for (final String alias : indexAliasNames) {
            long count = 0L;
            final Set<String> indexNameSet = aliasVos.stream().filter(p -> p.getAlias().equals(alias)).map(AliasVo::getIndex).collect(Collectors.toSet());
            final Set<IndexVo> indexVoSet = indexVos.stream().filter(p -> "p".equalsIgnoreCase(p.getPrirep()) && indexNameSet.contains(p.getIndex())).collect(Collectors.toSet());
            for (final IndexVo indexVo : indexVoSet) {
                if (null == indexVo || ObjectUtils.isEmpty(indexVo.getDocs())) {
                    continue;
                }
                count += Long.parseLong(indexVo.getDocs());
            }
            result.put(alias, count);
        }
        return result;
    }

    public Map<String, Set<IndexName>> getIndexNameByAlias(final List<String> aliasList) throws IOException {
        return ElasticSearchUtils.getIndexNameByAlias(this.elasticSearchService.getRestHighLevelClient(), aliasList);
    }

    public Set<IndexName> getIndexNameByAlias(final String alias) throws IOException {
        return ElasticSearchUtils.getIndexNameByAlias(this.elasticSearchService.getRestHighLevelClient(), alias);
    }

    public Map<Integer, IndexName> getIndexNameMapByAlias(final String alias) throws IOException {
        final Map<Integer, IndexName> result = new HashMap<>();
        final Set<IndexName> indexNameSet = ElasticSearchUtils.getIndexNameByAlias(this.elasticSearchService.getRestHighLevelClient(), alias);
        if (null == indexNameSet || indexNameSet.isEmpty()) {
            return null;
        }
        for (final IndexName indexName : indexNameSet) {
            result.put(indexName.getYear(), indexName);
        }
        return result;
    }

    public IndexName getIndexNameByAlias(final String alias, final int year) throws IOException {
        return ElasticSearchUtils.getIndexNameByAlias(this.elasticSearchService.getRestHighLevelClient(), alias, year);
    }

    public void createIndex(final String indexName,
                            final String alias,
                            final String source) throws IOException {
        final CreateIndexResponse createIndexResponse = ElasticSearchUtils.createIndex(this.elasticSearchService.getRestHighLevelClient(),
                indexName,
                alias,
                source);

        if (!createIndexResponse.isAcknowledged()) {
            throw new ElasticSearchException(String.format("索引[%s]创建失败, 原因: %s", indexName, createIndexResponse));
        }
    }

    public void deleteIndex(final String indexName) throws IOException {
        final DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        final AcknowledgedResponse response = this.elasticSearchService.getRestHighLevelClient().indices().delete(request, RequestOptions.DEFAULT);
        if (!response.isAcknowledged()) {
            throw new ElasticSearchException(String.format("索引[%s]删除失败, 原因: %s", indexName, response.toString()));
        }
    }

    public final boolean optimizeBulk(final Set<String> indexNameSet) throws IOException {
        final Map<String, String> settingMap = new HashMap<>();
        settingMap.put(Names.NUMBER_OF_REPLICAS, "0");
        settingMap.put(Names.REFRESH_INTERVAL, "-1");
        return ElasticSearchUtils.updateIndexSetting(this.elasticSearchService.getRestHighLevelClient(),
                indexNameSet,
                settingMap);
    }

    public final boolean optimizeBulk(final String indexName) throws IOException {
        final Map<String, String> settingMap = new HashMap<>();
        settingMap.put(Names.NUMBER_OF_REPLICAS, "0");
        settingMap.put(Names.REFRESH_INTERVAL, "-1");
        return ElasticSearchUtils.updateIndexSetting(this.elasticSearchService.getRestHighLevelClient(),
                new HashSet<>(Collections.singletonList(indexName)),
                settingMap);
    }

    public final boolean unoptimizeBulk(final String indexAliasName) throws Exception {
        final Set<IndexName> indexNames = this.getIndexNameByAlias(indexAliasName);
        if (indexNames == null || indexNames.isEmpty()) {
            throw new Db2EsException(String.format("不存在索引%s", indexAliasName));
        }

        final Topic topic = this.topicService.getByName(indexAliasName);
        if (null == topic) {
            throw new Db2EsException(String.format("不存在名为[%s]的主题", indexAliasName));
        }
        return unoptimizeBulk(topic.getNumberOfReplicas(),
                topic.getRefreshInterval(),
                indexNames.stream().map(IndexName::toString).collect(Collectors.toSet()));
    }

    public final boolean unoptimizeBulk(final int numberOfReplicas,
                                        final String refreshInterval,
                                        final Set<String> indexNameSet) throws Exception {
        final Map<String, String> settingMap = new HashMap<>();
        settingMap.put(Names.NUMBER_OF_REPLICAS, String.valueOf(numberOfReplicas));
        settingMap.put(Names.REFRESH_INTERVAL, refreshInterval);
        return ElasticSearchUtils.updateIndexSetting(this.elasticSearchService.getRestHighLevelClient(),
                indexNameSet,
                settingMap);
    }

    public final IndexSetting getIndexSetting(final String indexName) throws IOException {
        final Set<String> indexNameSet = new HashSet<>();
        indexNameSet.add(indexName);
        final Map<String, String> result = ElasticSearchUtils.getIndexSetting(this.elasticSearchService.getRestHighLevelClient(),
                indexNameSet,
                new HashSet<>(Arrays.asList(Names.NUMBER_OF_REPLICAS, Names.REFRESH_INTERVAL)));
        if (null == result || result.isEmpty()) {
            return null;
        }
        return new IndexSetting(
                result.get(Names.NUMBER_OF_REPLICAS),
                result.get(Names.REFRESH_INTERVAL)
        );
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
        final IndexResponse response = this.elasticSearchService.getRestHighLevelClient().index(request, RequestOptions.DEFAULT);
        return (response.getShardInfo().getFailed() == 0);
    }

    public final boolean insert(final String indexName,
                                final String id,
                                final Map<String, Object> source) throws IOException {
        final IndexRequest request = new IndexRequest(indexName)
                .id(id)
                .source(source);
        final IndexResponse response = this.elasticSearchService.getRestHighLevelClient().index(request, RequestOptions.DEFAULT);
        return (response.getShardInfo().getFailed() == 0);
    }

    public final boolean update(final String indexName,
                                final String id,
                                final Map<String, Object> source) throws IOException {
        final UpdateRequest updateRequest = new UpdateRequest(indexName, id).doc(source);
        final UpdateResponse response = this.elasticSearchService.getRestHighLevelClient().update(updateRequest, RequestOptions.DEFAULT);
        return (0 == response.getShardInfo().getFailed());
    }

    public final boolean delete(final String indexName,
                                final String id) throws IOException {
        final DeleteRequest request = new DeleteRequest(indexName, id);
        final DeleteResponse response = this.elasticSearchService.getRestHighLevelClient().delete(request, RequestOptions.DEFAULT);
        return (response.getShardInfo().getFailed() == 0);
    }

    public final boolean delete(final String indexName,
                                final QueryBuilder queryBuilder) throws IOException {
        final DeleteByQueryRequest request = new DeleteByQueryRequest(indexName);
        request.setQuery(queryBuilder);
        final BulkByScrollResponse resp = this.elasticSearchService.getRestHighLevelClient().deleteByQuery(request, RequestOptions.DEFAULT);
        return 0 == resp.getBulkFailures().size();
    }

    public final BulkResponse bulk(final BulkRequest request) throws IOException {
        return this.elasticSearchService.getRestHighLevelClient().bulk(request, RequestOptions.DEFAULT);
    }
}