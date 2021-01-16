package org.wyyt.sharding.db2es.core.util.elasticsearch;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.rest.RestStatus;
import org.wyyt.sharding.db2es.core.entity.domain.*;
import org.wyyt.sharding.db2es.core.entity.persistent.Topic;
import org.wyyt.sharding.db2es.core.entity.view.AliasVo;
import org.wyyt.sharding.db2es.core.entity.view.IndexVo;
import org.wyyt.sharding.db2es.core.exception.Db2EsException;
import org.wyyt.tool.date.DateTool;
import org.wyyt.tool.exception.ExceptionTool;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.wyyt.sharding.db2es.core.util.CommonUtils.OBJECT_MAPPER;

/**
 * the common functions of elastic-search
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
public final class ElasticSearchUtils {
    private static final String GET = "GET";
    private static final String PUT = "PUT";
    private static final Set<String> ALREADY_CREATED_INDEX_NAME = new HashSet<>(64);
    private static final List<String> IGNORE_ELASTIC_SEARCH_STATUS_EXCEPTION = Collections.singletonList("resource_already_exists_exception");
    private static final String _CAT_SHARDS = "/_cat/shards/%s?format=json";
    private static final String _CAT_ALIASES = "/_cat/aliases/%s?h=alias,index&format=json";

    public static List<EsException> getElasticSearchExceptions(final BulkResponse bulkResponse) {
        if (null == bulkResponse || !bulkResponse.hasFailures()) {
            return null;
        }

        final List<BulkItemResponse> failureResponse = Arrays.stream(bulkResponse.getItems()).filter(p -> !ignoreElasticSearchException(p.getFailure())).collect(Collectors.toList());
        if (failureResponse.isEmpty()) {
            return null;
        }

        final List<EsException> result = new ArrayList<>(failureResponse.size());
        for (final BulkItemResponse item : failureResponse) {
            result.add(new EsException(item, item.getFailure().getCause()));
        }
        return result;
    }

    public static boolean ignoreElasticSearchException(final BulkItemResponse.Failure failure) {
        if (null == failure) {
            return true;
        } else {
            return RestStatus.CONFLICT == failure.getStatus();
        }
    }


    public static List<IndexVo> listIndexVo(final RestHighLevelClient restHighLevelClient,
                                            final Set<String> indexNames) {
        try {
            return get(restHighLevelClient,
                    String.format(_CAT_SHARDS, org.apache.commons.lang.StringUtils.join(indexNames, ",")), IndexVo.class);
        } catch (final Exception exception) {
            return null;
        }
    }

    public static List<AliasVo> listAliasVo(final RestHighLevelClient restHighLevelClient,
                                            final Set<String> indexNames) {
        try {
            return get(restHighLevelClient,
                    String.format(_CAT_ALIASES, org.apache.commons.lang.StringUtils.join(indexNames, ",")), AliasVo.class);
        } catch (final Exception exception) {
            return null;
        }
    }

    public static <T> List<T> get(final RestHighLevelClient restHighLevelClient,
                                  final String endpoint,
                                  final Class<T> tClass) throws IOException {
        final List<T> result = new ArrayList<>();
        curl(restHighLevelClient, GET, endpoint,
                dataList -> {
                    if (null != dataList) {
                        for (HashMap<String, String> data : dataList) {
                            result.add(OBJECT_MAPPER.convertValue(data, tClass));
                        }
                    }
                });
        return result;
    }

    public static <T> List<T> put(final RestHighLevelClient restHighLevelClient,
                                  final String endpoint,
                                  final Class<T> tClass) throws IOException {
        final List<T> result = new ArrayList<>();
        curl(restHighLevelClient, PUT, endpoint,
                dataList -> {
                    if (null != dataList) {
                        for (HashMap<String, String> data : dataList) {
                            result.add(OBJECT_MAPPER.convertValue(data, tClass));
                        }
                    }
                });
        return result;
    }

    public static void curl(final RestHighLevelClient restHighLevelClient,
                            final String method,
                            final String endpoint,
                            final Handle handle) throws IOException {
        final RestClient restClient = restHighLevelClient.getLowLevelClient();
        final Response response = restClient.performRequest(new Request(method, endpoint));
        if (null != response) {
            final String rawBody = EntityUtils.toString(response.getEntity());
            handle.process(OBJECT_MAPPER.readValue(rawBody, new TypeReference<List<HashMap<String, String>>>() {
            }));
        }
    }

    public interface Handle {
        void process(final List<HashMap<String, String>> dataList);
    }

    public static List<IndexRequest> toInsertRequest(final RestHighLevelClient restHighLevelClient,
                                                     final FlatMsg flatMsg,
                                                     final Config config,
                                                     final TopicType topicType,
                                                     final boolean checkInMemory) throws Exception {
        final List<IndexRequest> result = new ArrayList<>(flatMsg.getData().size());
        final TableInfo tableInfo = config.getTableMap().getByFactTableName(flatMsg.getTable());
        for (final CaseInsensitiveMap<String, String> datum : flatMsg.getData()) {
            final String primaryValue = datum.get(tableInfo.getPrimaryKeyFieldName());
            final String strRowCreateTime = datum.get(tableInfo.getRowCreateTimeFieldName());

            if (StringUtils.isEmpty(primaryValue)) {
                throw new Db2EsException(String.format("record[%s] missing the required primary key field[%s]", flatMsg.getConsumerRecord(), tableInfo.getPrimaryKeyFieldName()));
            }
            if (StringUtils.isEmpty(strRowCreateTime)) {
                throw new Db2EsException(String.format("record[%s] missing the required row create time field[%s]", flatMsg.getConsumerRecord(), tableInfo.getRowCreateTimeFieldName()));
            }

            final Date rowCreateTime = DateTool.parse(strRowCreateTime);
            final String indexName = getIndexName(restHighLevelClient, config, flatMsg.getConsumerRecord().topic(), rowCreateTime, topicType, checkInMemory);

            result.add(new IndexRequest(indexName)
                    .id(primaryValue)
                    .source(datum)
                    .versionType(VersionType.EXTERNAL)
                    .version(flatMsg.getSequence()));
        }
        return result;
    }

    public static List<DeleteRequest> toDeleteRequest(final RestHighLevelClient restHighLevelClient,
                                                      final FlatMsg flatMsg,
                                                      final Config config,
                                                      final TopicType topicType,
                                                      final boolean checkInMemory) throws Exception {
        final List<DeleteRequest> result = new ArrayList<>(flatMsg.getData().size());
        final TableInfo tableInfo = config.getTableMap().getByFactTableName(flatMsg.getTable());

        for (final CaseInsensitiveMap<String, String> datum : flatMsg.getData()) {
            final String primaryValue = datum.get(tableInfo.getPrimaryKeyFieldName());
            final String strRowCreateTime = datum.get(tableInfo.getRowCreateTimeFieldName());

            if (StringUtils.isEmpty(primaryValue)) {
                throw new Db2EsException(String.format("record[%s] missing the required primary key field[%s]", flatMsg.getConsumerRecord(), tableInfo.getPrimaryKeyFieldName()));
            }
            if (StringUtils.isEmpty(strRowCreateTime)) {
                throw new Db2EsException(String.format("record[%s] missing the required row create time field[%s]", flatMsg.getConsumerRecord(), tableInfo.getRowCreateTimeFieldName()));
            }

            final Date rowCreateTime = DateTool.parse(strRowCreateTime);
            final String indexName = getIndexName(restHighLevelClient, config, flatMsg.getConsumerRecord().topic(), rowCreateTime, topicType, checkInMemory);

            result.add(new DeleteRequest(indexName, primaryValue)
                    .versionType(VersionType.EXTERNAL)
                    .version(flatMsg.getSequence()));
        }
        return result;
    }

    public static List<DocWriteRequest> toUpdateRequest(final RestHighLevelClient restHighLevelClient,
                                                        final FlatMsg flatMsg,
                                                        final Config config,
                                                        final TopicType topicType,
                                                        final boolean checkInMemory) throws Exception {
        if (flatMsg.getOld().size() != flatMsg.getData().size()) {
            throw new Db2EsException(String.format("the old data size not equals with new data in consumer record[%s]", flatMsg.getConsumerRecord().value()));
        }
        final List<DocWriteRequest> result = new ArrayList<>(flatMsg.getData().size());

        final TableInfo tableInfo = config.getTableMap().getByFactTableName(flatMsg.getTable());

        for (int i = 0; i < flatMsg.getOld().size(); i++) {
            final CaseInsensitiveMap<String, String> oldMap = flatMsg.getOld().get(i);
            final CaseInsensitiveMap<String, String> newMap = flatMsg.getData().get(i);
            final String primaryValue = newMap.get(tableInfo.getPrimaryKeyFieldName());
            final String strRowCreateTime = newMap.get(tableInfo.getRowCreateTimeFieldName());

            if (StringUtils.isEmpty(primaryValue)) {
                throw new Db2EsException(String.format("record[%s] missing the required primary key field[%s]", flatMsg.getConsumerRecord(), tableInfo.getPrimaryKeyFieldName()));
            }
            if (StringUtils.isEmpty(strRowCreateTime)) {
                throw new Db2EsException(String.format("record[%s] missing the required row create time field[%s]", flatMsg.getConsumerRecord(), tableInfo.getRowCreateTimeFieldName()));
            }

            final Date rowCreateTime = DateTool.parse(strRowCreateTime);
            final String indexName = getIndexName(restHighLevelClient, config, flatMsg.getConsumerRecord().topic(), rowCreateTime, topicType, checkInMemory);

            final Map<String, Object> source = new HashMap<>((int) (oldMap.size() / 0.75));
            for (final Map.Entry<String, String> pair : oldMap.entrySet()) {
                final String columnName = pair.getKey();
                final String newValue = newMap.get(columnName);
                source.put(columnName, newValue);
            }

            result.add(new UpdateRequest(indexName, primaryValue)
                    .doc(source)
                    .upsert(new HashMap<>(newMap)));
        }
        return result;
    }

    public static String getIndexName(final RestHighLevelClient restHighLevelClient,
                                      final Config config,
                                      final String topicName,
                                      final Date rowCreateTime,
                                      final TopicType topicType,
                                      final boolean checkInMemory) throws Exception {
        final Calendar calendar = Calendar.getInstance();
        final int currentYear = calendar.get(Calendar.YEAR);

        calendar.setTime(rowCreateTime);
        final int rowCreateYear = calendar.get(Calendar.YEAR);

        final Topic topic = config.getTopicMap().get(topicName);
        final String alias = (Math.abs(currentYear - rowCreateYear + 1) <= topic.getAliasOfYears()) ? topicName : "";

        Integer suffix = null;
        if (topicType == TopicType.IN_USE) {
            suffix = topic.getInUseSuffixMap().get(rowCreateYear);
        } else if (topicType == TopicType.REBUILD) {
            suffix = topic.getRebuildSuffixMap().get(rowCreateYear);
        }
        if (null == suffix) {
            final TopicType defaultType = TopicType.getDefaultType(topicType);
            suffix = defaultType.getType();
            if (topicType == TopicType.IN_USE) {
                topic.getInUseSuffixMap().put(rowCreateYear, defaultType.getType());
                topic.getRebuildSuffixMap().put(rowCreateYear, defaultType.getAnotherType().getType());
            } else if (topicType == TopicType.REBUILD) {
                topic.getInUseSuffixMap().put(rowCreateYear, defaultType.getAnotherType().getType());
                topic.getRebuildSuffixMap().put(rowCreateYear, defaultType.getType());
            }
        }

        final String indexName = new IndexName(topicName, rowCreateYear, suffix).toString();

        if (createIndexIfNotExists(restHighLevelClient, topic, alias, indexName, checkInMemory)) {
            final IndexName removeAlias = new IndexName(topicName, currentYear - topic.getAliasOfYears(), suffix);
            removeAlias(restHighLevelClient, removeAlias.toString(), removeAlias.getAlias());
        }
        return indexName;
    }

    public static void removeAlias(final RestHighLevelClient restHighLevelClient,
                                   final String indexName,
                                   final String alias) throws Exception {
        if (exists(restHighLevelClient, indexName)) {
            final IndicesAliasesRequest request = new IndicesAliasesRequest();
            final IndicesAliasesRequest.AliasActions aliasAction =
                    new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
                            .index(indexName)
                            .alias(alias);
            request.addAliasAction(aliasAction);
            final AcknowledgedResponse response = restHighLevelClient.indices().updateAliases(request, RequestOptions.DEFAULT);
        }
    }

    public static boolean createIndexIfNotExists(final RestHighLevelClient restHighLevelClient,
                                                 final Topic topic,
                                                 final String alias,
                                                 final String indexName,
                                                 final boolean checkInMemory) throws IOException {
        if (checkInMemory && ALREADY_CREATED_INDEX_NAME.contains(indexName)) {
            return false;
        }

        if (ElasticSearchUtils.exists(restHighLevelClient, indexName)) {
            ALREADY_CREATED_INDEX_NAME.add(indexName);
            return false;
        }
        if (StringUtils.isEmpty(topic.getMapping().trim())) {
            throw new Db2EsException(String.format("ElasticSearchUtils: the index[%s] miss the mapping information", alias));
        }
        try {
            final boolean result = createIndex(restHighLevelClient,
                    alias,
                    indexName,
                    topic.getNumberOfShards(),
                    topic.getNumberOfReplicas(),
                    topic.getRefreshInterval(),
                    topic.getMapping().trim());
            if (!result) {
                throw new Db2EsException(String.format("index[%s] create failed", indexName));
            }
            ALREADY_CREATED_INDEX_NAME.add(indexName);
            return true;
        } catch (final ElasticsearchStatusException e) {
            if (!IGNORE_ELASTIC_SEARCH_STATUS_EXCEPTION.contains(ExceptionTool.getRootCauseMessage(e).toLowerCase())) {
                throw e;
            }
        }
        return false;
    }

    public static boolean exists(final RestHighLevelClient restHighLevelClient,
                                 final String indexName) throws IOException {
        return restHighLevelClient.indices().exists(new GetIndexRequest(indexName), RequestOptions.DEFAULT);
    }

    public static boolean createIndex(final RestHighLevelClient restHighLevelClient,
                                      final String alias,
                                      final String indexName,
                                      final int numberOfShards,
                                      final int numberOfReplicas,
                                      final String refreshInterval,
                                      final String mapping) throws IOException {
        final CreateIndexRequest request = new CreateIndexRequest(indexName);
        if (!StringUtils.isEmpty(alias)) {
            request.alias(new Alias(alias));
        }
        request.settings(Settings.builder()
                .put(Names.NUMBER_OF_SHARDS, String.valueOf(numberOfShards))
                .put(Names.NUMBER_OF_REPLICAS, String.valueOf(numberOfReplicas))
                .put(Names.REFRESH_INTERVAL, refreshInterval));
        request.mapping(mapping, XContentType.JSON);
        final CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        return createIndexResponse.isAcknowledged();
    }

    public void deleteIndex(final RestHighLevelClient restHighLevelClient,
                            final String indexName) throws IOException {
        final DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        final AcknowledgedResponse response = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        if (response.isAcknowledged()) {
            ALREADY_CREATED_INDEX_NAME.remove(indexName);
        } else {
            throw new Db2EsException(String.format("索引[%s]删除失败, 原因: %s", indexName, response.toString()));
        }
    }

    public static Map<String, Set<IndexName>> getIndexNameByAlias(final RestHighLevelClient restHighLevelClient,
                                                                  final List<String> aliasList) throws IOException {
        if (aliasList == null || aliasList.isEmpty()) {
            return null;
        }
        final Map<String, Set<IndexName>> result = new HashMap<>();
        final GetAliasesRequest request = new GetAliasesRequest(aliasList.toArray(new String[]{}));
        final GetAliasesResponse response = restHighLevelClient.indices().getAlias(request, RequestOptions.DEFAULT);
        for (final Map.Entry<String, Set<AliasMetadata>> pair : response.getAliases().entrySet()) {
            final IndexName indexName = new IndexName(pair.getKey());
            final String key = indexName.getAlias();
            if (result.containsKey(key)) {
                result.get(key).add(indexName);
            } else {
                final Set<IndexName> indexNameSet = new HashSet<>();
                indexNameSet.add(indexName);
                result.put(key, indexNameSet);
            }
        }
        return result;
    }

    public static Map<Integer, IndexName> getIndexNameByAliasMap(final RestHighLevelClient restHighLevelClient,
                                                                 final String alias) throws IOException {
        final Map<Integer, IndexName> result = new HashMap<>();
        final Set<IndexName> indexNameByAlias = getIndexNameByAlias(restHighLevelClient, alias);
        if (null == indexNameByAlias) {
            return result;
        }
        for (final IndexName indexName : indexNameByAlias) {
            result.put(indexName.getYear(), indexName);
        }
        return result;
    }

    public static Set<IndexName> getIndexNameByAlias(final RestHighLevelClient restHighLevelClient,
                                                     final String alias) throws IOException {
        final Map<String, Set<IndexName>> result = getIndexNameByAlias(restHighLevelClient, Collections.singletonList(alias));
        if (null == result || result.isEmpty()) {
            return null;
        }
        for (final Map.Entry<String, Set<IndexName>> pair : result.entrySet()) {
            if (pair.getKey().equals(alias)) {
                return pair.getValue();
            }
        }
        return null;
    }

    public static IndexName getIndexNameByAlias(final RestHighLevelClient restHighLevelClient,
                                                final String alias,
                                                final int year) throws IOException {
        final Map<Integer, IndexName> indexNameMap = getIndexNameByAliasMap(restHighLevelClient, alias);
        return indexNameMap.get(year);
    }

    public static boolean updateIndexSetting(final RestHighLevelClient restHighLevelClient,
                                             final Set<String> indexNameSet,
                                             final Map<String, String> settingMap) throws IOException {
        if (null == settingMap || settingMap.isEmpty()) {
            return false;
        }
        final UpdateSettingsRequest request = new UpdateSettingsRequest(indexNameSet.toArray(new String[]{})).settings(settingMap);
        final AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().putSettings(request, RequestOptions.DEFAULT);
        return acknowledgedResponse.isAcknowledged();
    }

    public static Map<String, String> getIndexSetting(final RestHighLevelClient restHighLevelClient,
                                                      final Set<String> indexNameSet,
                                                      final Set<String> settingNameSet) throws IOException {
        final Map<String, String> result = new HashMap<>();
        final GetSettingsRequest request = new GetSettingsRequest().indices(indexNameSet.toArray(new String[]{}));
        request.names(settingNameSet.toArray(new String[]{}));
        request.includeDefaults(true);
        request.indicesOptions(IndicesOptions.lenientExpandOpen());
        final GetSettingsResponse response = restHighLevelClient.indices().getSettings(request, RequestOptions.DEFAULT);
        final ImmutableOpenMap<String, Settings> indexToSettings = response.getIndexToSettings();
        for (final String indexName : indexNameSet) {
            final Settings settings = indexToSettings.get(indexName);
            if (null == settings) {
                return null;
            }
            for (final String key : settings.keySet()) {
                result.put(key, settings.get(key));
            }
        }
        return result;
    }

    public static void refreshTopicSuffix(final RestHighLevelClient restHighLevelClient,
                                          final Topic topic,
                                          final boolean removeUnavaiableAlias) throws Exception {
        if (null == topic.getInUseSuffixMap()) {
            topic.setInUseSuffixMap(new HashMap<>());
        }
        if (null == topic.getRebuildSuffixMap()) {
            topic.setRebuildSuffixMap(new HashMap<>());
        }
        topic.getInUseSuffixMap().clear();
        topic.getRebuildSuffixMap().clear();
        final Map<Integer, IndexName> indexNameMap = getIndexNameByAliasMap(restHighLevelClient, topic.getName());
        calcAliasYearSpan(topic).forEach(year -> {
            final IndexName inUseIndexName = indexNameMap.get(year);
            if (null == inUseIndexName) {
                topic.getInUseSuffixMap().put(year, TopicType.IN_USE.getType());
                topic.getRebuildSuffixMap().put(year, TopicType.REBUILD.getType());
            } else {
                TopicType topicType = TopicType.get(inUseIndexName.getSuffix());
                topic.getInUseSuffixMap().put(year, topicType.getType());
                topic.getRebuildSuffixMap().put(year, topicType.getAnotherType().getType());
            }
        });

        if (removeUnavaiableAlias) {
            for (final Map.Entry<Integer, IndexName> pair : indexNameMap.entrySet()) {
                if (!topic.getInUseSuffixMap().containsKey(pair.getKey())) {
                    removeAlias(restHighLevelClient,
                            pair.getValue().toString(),
                            pair.getValue().getAlias());
                }
            }
        }
    }

    public static void refreshTopicSuffix(final RestHighLevelClient restHighLevelClient,
                                          final Topic topic) throws Exception {
        refreshTopicSuffix(restHighLevelClient, topic, true);
    }

    public static AliasYearSpan calcAliasYearSpan(final Topic topic) {
        final AliasYearSpan result = new AliasYearSpan();
        final Calendar now = Calendar.getInstance();
        final int currentYear = now.get(Calendar.YEAR);
        result.setTo(currentYear);
        result.setFrom(currentYear - topic.getAliasOfYears() + 1);
        result.setSpan(topic.getAliasOfYears());
        return result;
    }

    @ToString
    @Data
    public static class AliasYearSpan {
        private int from;
        private int to;
        private int span;

        public final void forEach(final Action action) {
            for (int year = this.getFrom(); year <= this.getTo(); year++) {
                action.getYear(year);
            }
        }

        interface Action {
            void getYear(final int year);
        }

    }
}