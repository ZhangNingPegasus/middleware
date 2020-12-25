package org.wyyt.sharding.db2es.admin.service;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Service;
import org.wyyt.sharding.db2es.admin.service.common.EsService;
import org.wyyt.sharding.db2es.admin.service.common.ShardingDbService;
import org.wyyt.sharding.db2es.core.entity.domain.Config;
import org.wyyt.sharding.db2es.core.entity.domain.IndexName;
import org.wyyt.sharding.db2es.core.entity.domain.TableInfo;
import org.wyyt.sharding.db2es.core.entity.persistent.Topic;
import org.wyyt.sharding.db2es.core.exception.Db2EsException;
import org.wyyt.sharding.db2es.core.util.CommonUtils;
import org.wyyt.elasticsearch.service.ElasticSearchService;
import org.wyyt.sharding.anno.TranSave;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * The service for repair
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Service
public class RepairService {
    private final ShardingDbService shardingDbService;
    private final EsService esService;
    private final PropertyService propertyService;
    private final TopicService topicService;

    public RepairService(final ShardingDbService shardingDbService,
                         final EsService esService,
                         final PropertyService propertyService,
                         final TopicService topicService) {
        this.shardingDbService = shardingDbService;
        this.esService = esService;
        this.propertyService = propertyService;
        this.topicService = topicService;
    }

    @TranSave
    public void repair(final String databaseName,
                       final String tableName,
                       final String indexAliasName,
                       final String id) throws Exception {
        final Config config = this.propertyService.getConfig();
        TableInfo tableInfo = config.getTableMap().getByFactTableName(tableName);

        this.shardingDbService.getByIdInTransaction(kvs -> {
            if (null != kvs && !kvs.isEmpty()) {
                if (1 == kvs.size()) {
                    final Map<String, Object> dataMap = kvs.get(0);
                    long version;
                    final String pkValue = dataMap.get(tableInfo.getPrimaryKeyFieldName()).toString();
                    final Date rowUpdateTime = (Date) dataMap.get(tableInfo.getRowUpdateTimeFieldName());
                    final Date rowCreateTime = (Date) dataMap.get(tableInfo.getRowCreateTimeFieldName());

                    final ElasticSearchService.EsResult esResult = this.esService.getElasticSearchService().getDetailById(indexAliasName, pkValue);

                    if (null == esResult) {
                        final Calendar calendarUpdateTime = Calendar.getInstance();
                        calendarUpdateTime.setTime(rowUpdateTime);
                        version = CommonUtils.toEsVersion(rowUpdateTime);
                    } else {
                        version = esResult.getGetResponse().getVersion() + 1L;
                    }

                    final Calendar calendarCreateTime = Calendar.getInstance();
                    calendarCreateTime.setTime(rowCreateTime);

                    for (final Map.Entry<String, Object> pair : dataMap.entrySet()) {
                        if (pair.getValue().getClass().isAssignableFrom(Date.class)) {
                            pair.setValue(CommonUtils.formatMs((Date) pair.getValue()));
                        }
                    }
                    final int year = calendarCreateTime.get(Calendar.YEAR);
                    final IndexName indexName = this.esService.getIndexNameByAlias(indexAliasName, year);
                    if (null == indexName) {
                        throw new Db2EsException(String.format("不存在%s年的%s索引", year, indexAliasName));
                    }
                    this.esService.insert(indexName.toString(), id, dataMap, version);
                }
            } else {
                try {
                    final Calendar calendar = Calendar.getInstance();
                    final int year = calendar.get(Calendar.YEAR);
                    final Map<Integer, IndexName> indexNameMap = this.esService.getIndexNameMapByAlias(indexAliasName);

                    final Topic topic = this.topicService.getByName(indexAliasName);

                    if (null == topic) {
                        throw new Db2EsException(String.format("主题[%s]不存在", indexAliasName));
                    }

                    for (int i = year; i > year - topic.getAliasOfYears(); i--) {
                        final IndexName indexName = indexNameMap.get(i);
                        if (null != indexName) {
                            this.esService.delete(indexName.toString(), id);
                        }
                    }
                } catch (final ElasticsearchStatusException exception) {
                    if (RestStatus.NOT_FOUND != exception.status()) {
                        throw exception;
                    }
                }
            }
        }, databaseName, tableName, id);
    }
}