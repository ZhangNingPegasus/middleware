package org.wyyt.sharding.db2es.admin.rebuild;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.utils.CloseableUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.sharding.db2es.core.entity.domain.*;
import org.wyyt.sharding.db2es.core.util.flatmsg.Operation;
import org.wyyt.sharding.db2es.admin.entity.vo.DataSourceVo;
import org.wyyt.sharding.db2es.admin.service.PropertyService;
import org.wyyt.sharding.db2es.admin.service.TopicService;
import org.wyyt.sharding.db2es.admin.service.common.Db2EsHttpService;
import org.wyyt.sharding.db2es.admin.service.common.EsService;
import org.wyyt.sharding.db2es.admin.service.common.ZooKeeperService;
import org.wyyt.sharding.db2es.core.entity.persistent.Topic;
import org.wyyt.sharding.db2es.core.entity.view.IndexVo;
import org.wyyt.sharding.db2es.core.entity.view.NodeVo;
import org.wyyt.sharding.db2es.core.exception.Db2EsException;
import org.wyyt.sharding.db2es.core.util.CommonUtils;
import org.wyyt.sharding.db2es.core.util.elasticsearch.ElasticSearchUtils;
import org.wyyt.sharding.db2es.core.util.flatmsg.FlatMsgUtils;
import org.wyyt.sharding.db2es.core.util.kafka.KafkaUtils;
import org.wyyt.sharding.db2es.core.util.metastore.MetaStoreUtils;
import org.wyyt.tool.common.CommonTool;
import org.wyyt.tool.date.DateTool;
import org.wyyt.tool.db.DataSourceTool;
import org.wyyt.tool.exception.ExceptionTool;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * The service for ES' index rebuilding.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Slf4j
@Service
public class RebuildService {
    private static final int KAFKA_BATCH_SIZE = 2048;
    private static final int MIN_LAY = 100;
    private volatile List<PopulateRunner> populateRunnerList;
    private final AtomicBoolean terminated;
    private final EsService esService;
    private final ThreadService threadService;
    private final ZooKeeperService zooKeeperService;
    private final Config config;
    private final Db2EsHttpService db2EsHttpService;
    private final TopicService topicService;
    private final AtomicBoolean isRunning;
    private final AtomicBoolean isShardsReady;
    private final AtomicBoolean cannotCanal;
    private final RebuildStatus rebuildStatus;
    private final Map<DataSourceVo, DataSource> dataSourceMap;
    private volatile Thread threadFlushShards;
    private volatile Topic topic;
    @Getter
    private volatile DbStatus dbStatus;
    @Getter
    private volatile KafkaStatus kafkaStatus;
    private CompletableFuture<Boolean> completableFuture;

    public final CompletableFuture<Boolean> rebuild(final Topic topic,
                                                    final Map<DataSourceVo, Set<String>> tableSourceMap) throws Exception {
        if (!this.isRunning.compareAndSet(false, true)) {
            throw new Db2EsException("同一时刻只能对一个主题进行重建索引");
        }
        this.topic = topic;
        this.terminated.set(false);
        this.isShardsReady.set(false);
        this.cannotCanal.set(false);
        this.clearThreadFlushShards();
        this.clearDataSource();
        this.rebuildStatus.clear(topic.getName());
        final ExceptionCallbackImpl exceptionCallback = new ExceptionCallbackImpl(exception -> terminated.set(true));
        this.dbStatus = new DbStatus(topic.getName(), this.terminated);
        this.kafkaStatus = new KafkaStatus(topic.getName(), this.terminated);
        this.rebuildStatus.setDbStatus(this.dbStatus);
        this.rebuildStatus.setKafkaStatus(this.kafkaStatus);
        final Topic dbTopic = this.topicService.getByName(topic.getName());

        this.completableFuture = CompletableFuture.supplyAsync(() -> {
            ElasticSearchBulk elasticSearchBulk = null;
            Map<Integer, IndexName> rebuildIndexMap = null;
            boolean initialization = false;
            try {
                long allStart = System.currentTimeMillis();

                final Set<IndexName> buffer = this.esService.getIndexNameByAlias(topic.getName());
                initialization = (null == buffer || buffer.isEmpty());
                this.createDataSource(tableSourceMap);

                long start = allStart;
                this.rebuildStatus.addMessage(String.format("开始根据新的Elastic-Search的MAPPING设定，为逻辑表[<b>%s</b>]创建新的索引", topic.getName()));
                rebuildIndexMap = createRebuildIndex(topic);
                this.rebuildStatus.setRebuildIndexNames(rebuildIndexMap.values().stream().map(IndexName::toString).collect(Collectors.toSet()));
                this.rebuildStatus.addProgress(10);
                this.rebuildStatus.addMessage(String.format("已成功创建新的索引。耗时:%.3f秒<br/>", calcDuringSeconds(start)));

                elasticSearchBulk = new ElasticSearchBulk(this.esService, exceptionCallback);
                final Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.YEAR, -topic.getAliasOfYears() + 1);
                final int minYear = calendar.get(Calendar.YEAR);
                final Date fromDate = DateTool.parse(String.format("%s-01-01 00:00:00", minYear));
                final Map<DataSourceVo, Long> dbDateMap = this.getDbCurrentTime();
                final List<Date> dbDateList = dbDateMap.values().stream().map(Date::new).sorted(Date::compareTo).collect(Collectors.toList());
                this.rebuildStatus.addMessage(String.format("开始从数据库向Elastic-Search同步全量数据(时间: 从<b>%s</b>同步到<b>%s</b>)", CommonUtils.formatMs(fromDate), CommonUtils.formatMs(dbDateList.get(0))));
                start = System.currentTimeMillis();
                final List<Exception> populateExceptionList = this.populate(elasticSearchBulk, topic, tableSourceMap, rebuildIndexMap, fromDate, dbDateMap);
                if (!populateExceptionList.isEmpty()) {
                    System.err.printf("2. 数据库同步发生异常, 原因: %s%n", ExceptionTool.getRootCauseMessage(populateExceptionList.get(0)));
                    throw populateExceptionList.get(0);
                }
                double duringSeconds = calcDuringSeconds(start);
                this.rebuildStatus.setDbTps((int) (this.rebuildStatus.getDbCount() / duringSeconds));
                if (this.rebuildStatus.getDbTps() <= 0) {
                    this.rebuildStatus.setKafkaTps((int) this.rebuildStatus.getDbCount());
                }
                this.rebuildStatus.addProgress(40);
                this.rebuildStatus.addMessage(String.format("已成功从数据库同步完全量数据(共同步<b>%s</b>条, 每秒平均同步%s条)。耗时:%.3f秒<br/>", this.rebuildStatus.getDbCount(), this.rebuildStatus.getDbTps(), duringSeconds));

                start = System.currentTimeMillis();
                KafkaResult kafkaResult = this.startKafkaConsume(elasticSearchBulk, topic, dbDateList, rebuildIndexMap);
                duringSeconds = calcDuringSeconds(start);
                this.rebuildStatus.setKafkaTps((int) (this.rebuildStatus.getKafkaCount() / duringSeconds));
                if (this.rebuildStatus.getKafkaTps() <= 0) {
                    this.rebuildStatus.setKafkaTps((int) this.rebuildStatus.getKafkaCount());
                }
                this.rebuildStatus.addProgress(40);
                this.rebuildStatus.addMessage(String.format("已成功从Kafka同步完增量数据(时间: 从<b>%s</b>到<b>%s</b>, 共同步<b>%s</b>条, 每秒平均同步%s条)。共耗时:%.3f秒<br/>", CommonUtils.formatMs(dbDateList.get(0)), CommonUtils.formatMs(new Date(kafkaResult.getEndTime())), this.rebuildStatus.getKafkaCount(), this.rebuildStatus.getKafkaTps(), duringSeconds));

                this.checkTerminated(exceptionCallback.getException());
                this.rebuildStatus.addMessage(String.format("开始处理索引[<b>%s</b>]数据在ElasticSearch中的刷盘事项", topic.getName()));
                start = System.currentTimeMillis();
                elasticSearchBulk.flush();
                elasticSearchBulk.close();
                elasticSearchBulk = null;
                this.checkTerminated(exceptionCallback.getException());
                if (null != exceptionCallback.getException()) {
                    throw exceptionCallback.getException();
                } else {
                    this.rebuildStatus.addMessage(String.format("已成功完成了数据刷盘。耗时:%.3f秒", calcDuringSeconds(start)));
                }

                start = System.currentTimeMillis();
                this.rebuildStatus.addMessage(String.format("开始将旧索引从别名[<b>%s</b>]上移除，并绑定新的索引", topic.getName()));
                this.checkTerminated(exceptionCallback.getException());
                this.cannotCanal.set(true);
                this.topicService.insertOrUpdate(topic);
                final Set<IndexName> indexNames = rebindAlias(topic.getName(), rebuildIndexMap);
                this.rebuildStatus.addMessage(String.format("已成功切换索引别名，并已投入使用。耗时:%.3f秒<br/>", calcDuringSeconds(start)));

                try {
                    start = System.currentTimeMillis();
                    final NodeVo nodeVo = this.db2EsHttpService.getNodeByTopicName(topic.getName());
                    if (null != nodeVo) {
                        this.rebuildStatus.addMessage(String.format("正在向DB2ES(<b>id=%s</b>)服务(<b>%s</b>&nbsp;-&nbsp;<b>%s</b>)发送开启主题[<b>%s</b>]消费的通知", nodeVo.getId(), nodeVo.getIp(), nodeVo.getPort(), topic.getName()));
                        this.db2EsHttpService.restart(topic.getName(), kafkaResult.getOffset().toString(), null);
                        this.rebuildStatus.addMessage(String.format("已成功开启主题的消费。耗时:%.3f秒", calcDuringSeconds(start)));
                    }
                    for (final IndexName indexName : indexNames) {
                        this.esService.deleteIndex(indexName.toString());
                    }
                    this.rebuildStatus.addProgress(10);
                    this.rebuildStatus.addMessage(String.format("旧索引[<b>%s</b>]已成功清理完成", topic.getName()));
                    this.rebuildStatus.addMessage(String.format("索引[<b>%s</b>]重建已全部成功完成。共耗时:%.3f秒", topic.getName(), calcDuringSeconds(allStart)));
                } catch (final Exception e) {
                    this.rebuildStatus.addErrorMesssage(String.format("索引[%s]重建成功, 但收尾工作遇到一些问题, 请在Kiabana中对旧索引进行删除等操作, 失败原因: %s",
                            topic.getName(), ExceptionTool.getRootCauseMessage(e)), "green");
                }
            } catch (final Exception exception) {
                Exception e = exception;
                this.rebuildStatus.setError(true);
                if (e instanceof CanalException) {
                    final Throwable throwable = ((CanalException) e).getThrowable();
                    if (null == throwable) {
                        this.rebuildStatus.setProgress(0);
                        this.rebuildStatus.addErrorMesssage(String.format("<span style='color:green'>索引[%s]重建已被成功取消, 所有操作已回滚</span>", topic.getName()), "green");
                        e = null;
                    }
                }
                if (null != e) {
                    this.rebuildStatus.addErrorMesssage(String.format("<span style='color:red'>%s</span>", e.getMessage()));
                    this.rebuildStatus.addErrorMesssage(String.format("<span style='color:red'>索引[%s]重建失败, 所有操作成功回滚</span>", topic.getName()));
                    log.error(ExceptionTool.getRootCauseMessage(e), e);
                }

                final Set<String> deleteIndexNames = new HashSet<>();
                if (null != rebuildIndexMap && !rebuildIndexMap.isEmpty()) {
                    deleteIndexNames.addAll(rebuildIndexMap.values().stream().map(IndexName::toString).collect(Collectors.toSet()));
                }
                if (initialization) {
                    try {
                        final Set<IndexName> indexNamesSet = this.esService.getIndexNameByAlias(topic.getName());
                        if (null != indexNamesSet && !indexNamesSet.isEmpty()) {
                            deleteIndexNames.addAll(indexNamesSet.stream().map(IndexName::toString).collect(Collectors.toSet()));
                        }
                    } catch (IOException exc) {
                        log.error(ExceptionTool.getRootCauseMessage(exc), exc);
                    }
                }
                for (final String indexName : deleteIndexNames) {
                    try {
                        this.esService.deleteIndex(indexName);
                    } catch (final IOException exc) {
                        log.error(ExceptionTool.getRootCauseMessage(exc), exc);
                    }
                }

                if (null == dbTopic) {
                    this.topicService.removeById(topic.getId());
                } else {
                    this.topicService.updateById(dbTopic);
                }

                return false;
            } finally {
                if (null != elasticSearchBulk) {
                    elasticSearchBulk.flush();
                    elasticSearchBulk.close();
                }
                this.topic = null;
                this.isRunning.set(false);
                this.rebuildStatus.setComplete(true);
                this.completableFuture = null;
            }
            return true;
        });
        return this.completableFuture;
    }

    public RebuildService(final EsService esService,
                          final ThreadService threadService,
                          final PropertyService propertyService,
                          final Db2EsHttpService db2EsHttpService,
                          final ZooKeeperService zooKeeperService,
                          final TopicService topicService) {
        this.esService = esService;
        this.threadService = threadService;
        this.config = propertyService.getConfig();
        this.db2EsHttpService = db2EsHttpService;
        this.zooKeeperService = zooKeeperService;
        this.topicService = topicService;

        this.isRunning = new AtomicBoolean(false);
        this.terminated = new AtomicBoolean(false);
        this.isShardsReady = new AtomicBoolean(false);
        this.cannotCanal = new AtomicBoolean(false);
        this.rebuildStatus = new RebuildStatus();
        this.dataSourceMap = new HashMap<>();
        if (null == this.config.getTopicMap()) {
            this.config.setTopicMap(new HashMap<>());
        }
    }

    public final void clearRebuild() throws InterruptedException {
        this.clearThreadFlushShards();
        this.dbStatus = null;
        this.kafkaStatus = null;
        this.rebuildStatus.clear("");
    }

    public final boolean isRunning() {
        return this.isRunning.get();
    }

    public final RebuildStatus getRebuildStatus() {
        return this.rebuildStatus;
    }

    public final Topic getTopic() {
        return this.topic;
    }

    public final void stopRebuild() throws Exception {
        if (null == this.completableFuture) {
            return;
        }

        if (this.cannotCanal.get()) {
            throw new Db2EsException("停止失败, 原因: 重建索引已完成关键步骤, 已无法停止");
        }

        this.terminated.set(true);
        this.completableFuture.cancel(true);

        CompletableFuture.supplyAsync(() -> {
            while (null != this.completableFuture) {
                CommonTool.sleep(100);
            }
            return true;
        }).get();
    }

    private Map<Integer, IndexName> createRebuildIndex(final Topic topic) throws Exception {
        final Map<Integer, IndexName> result = new HashMap<>();
        final Set<IndexName> indexNameSet = this.esService.getIndexNameByAlias(topic.getName());
        final Calendar now = Calendar.getInstance();
        final int toYear = now.get(Calendar.YEAR);
        final int fromYear = toYear - topic.getAliasOfYears() + 1;

        for (int year = fromYear; year <= toYear; year++) {
            final IndexName indexName0 = new IndexName(topic.getName(), year, 0);
            final IndexName indexName1 = new IndexName(topic.getName(), year, 1);
            IndexName indexName = new IndexName(topic.getName(), year, 0);
            final IndexName indexNameRebuild = new IndexName(topic.getName(), year, 1);

            if (null != indexNameSet) {
                if (indexNameSet.contains(indexName0)) {
                    indexName = indexName0;
                } else if (indexNameSet.contains(indexName1)) {
                    indexName = indexName1;
                }
            }

            if (indexName.getSuffix().equals(0)) {
                indexNameRebuild.setSuffix(1);
            } else if (indexName.getSuffix().equals(1)) {
                indexNameRebuild.setSuffix(0);
            }

            if (!this.esService.getElasticSearchService().exists(indexName.toString())) {
                this.esService.createIndex(
                        indexName.toString(),
                        topic.getName(),
                        topic.getNumberOfShards(),
                        topic.getNumberOfReplicas(),
                        topic.getRefreshInterval(),
                        topic.getMapping()
                );
            }

            if (this.esService.getElasticSearchService().exists(indexNameRebuild.toString())) {
                this.esService.deleteIndex(indexNameRebuild.toString());
            }
            this.esService.createIndex(
                    indexNameRebuild.toString(),
                    null,
                    topic.getNumberOfShards(),
                    0,
                    "-1",
                    topic.getMapping()
            );
            result.put(indexNameRebuild.getYear(), indexNameRebuild);
            this.checkTerminated(null);
        }
        this.topicService.insertIfNotExists(topic);
        ElasticSearchUtils.refreshTopicSuffix(this.esService.getElasticSearchService().getRestHighLevelClient(), topic, false);
        this.config.getTopicMap().put(topic.getName(), topic);
        return result;
    }

    private List<Exception> populate(final ElasticSearchBulk elasticSearchBulk,
                                     final Topic topic,
                                     final Map<DataSourceVo, Set<String>> tableSourceMap,
                                     final Map<Integer, IndexName> rebuildIndexMap,
                                     final Date fromDate,
                                     final Map<DataSourceVo, Long> toDateMap) throws Exception {
        final List<Exception> result = new ArrayList<>();
        int tableCount = 0;
        for (final Set<String> value : tableSourceMap.values()) {
            tableCount += value.size();
        }
        if (null == populateRunnerList) {
            this.populateRunnerList = new ArrayList<>(tableCount);
        }
        this.populateRunnerList.clear();

        try {
            final CountDownLatch cdl = new CountDownLatch(tableCount);

            for (final Map.Entry<DataSourceVo, Set<String>> pair : tableSourceMap.entrySet()) {
                for (final String tableName : pair.getValue()) {
                    this.populateRunnerList.add(new PopulateRunner(elasticSearchBulk,
                            cdl,
                            this.dataSourceMap.get(pair.getKey()),
                            tableName,
                            this.config,
                            rebuildIndexMap,
                            fromDate,
                            new Date(toDateMap.get(pair.getKey())),
                            this.terminated));
                }
            }
            this.dbStatus.initStartTime(this.populateRunnerList);
            for (final PopulateRunner populateRunner : this.populateRunnerList) {
                this.threadService.submit(populateRunner);
            }
            cdl.await();
            elasticSearchBulk.flush();
            this.dbStatus.setDone(true);
            for (final PopulateRunner populateRunner : this.populateRunnerList) {
                if (null != populateRunner.getException()) {
                    result.add(populateRunner.getException());
                }
                this.rebuildStatus.setDbCount(this.rebuildStatus.getDbCount() + populateRunner.getCount());
            }
        } finally {
            for (final PopulateRunner populateRunner : this.populateRunnerList) {
                CloseableUtils.closeQuietly(populateRunner);
            }
            this.clearDataSource();
        }
        return result;
    }

    private KafkaResult startKafkaConsume(final ElasticSearchBulk elasticSearchBulk,
                                          final Topic topic,
                                          final List<Date> startDateList,
                                          final Map<Integer, IndexName> rebuildIndexMap) throws Exception {
        final KafkaResult kafkaResult = new KafkaResult();
        kafkaResult.setEndTime(System.currentTimeMillis());
        int tryTimes = 0;
        long _start = 0L;
        long count = 0L;
        KafkaConsumer<String, String> consumer = null;
        KafkaAdminClient kafkaAdminClient = null;
        final String rebuildConsumerName = KafkaUtils.toRebuildConsumerGroupName(topic.getName());

        try {
            final TopicPartition topicPartition = new TopicPartition(topic.getName(), 0);
            final Properties properties = new Properties();
            properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, KafkaUtils.getKafkaServers(this.zooKeeperService.getCuratorFramework()));
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, rebuildConsumerName);
            properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "db2es_admin_rebuild_index");
            properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
            properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, String.valueOf(KAFKA_BATCH_SIZE));
            properties.setProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, String.valueOf(1000 * 60 * 10));
            properties.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, String.valueOf(1000 * 60 * 10));
            properties.setProperty(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
            properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class.getCanonicalName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class.getCanonicalName());
            consumer = new KafkaConsumer<>(properties);
            consumer.assign(Collections.singletonList(topicPartition));

            kafkaAdminClient = (KafkaAdminClient) AdminClient.create(properties);
            long maxOffset = KafkaUtils.getMaxOffset(kafkaAdminClient, topicPartition);

            OffsetAndTimestamp offsetAndTimestamp = null;

            for (Date startDate : startDateList) {
                final Map<TopicPartition, OffsetAndTimestamp> topicPartitionOffsetAndTimestampMap = consumer.offsetsForTimes(Collections.singletonMap(topicPartition, startDate.getTime()));
                offsetAndTimestamp = topicPartitionOffsetAndTimestampMap.get(topicPartition);
                if (null != offsetAndTimestamp) {
                    break;
                }
            }

            if (null == offsetAndTimestamp) {
                offsetAndTimestamp = new OffsetAndTimestamp(maxOffset, 1L);
            }

            this.rebuildStatus.addMessage(String.format("开始从Kafka拉取增量数据并同步到Elastic-Search中(时间: 从<b>%s</b>开始拉取, 位点: <b>%s</b>)", CommonUtils.formatMs(startDateList.get(0)), offsetAndTimestamp.offset()));

            consumer.seek(topicPartition, offsetAndTimestamp.offset());
            final List<FlatMsg> flatMessageList = new ArrayList<>(KAFKA_BATCH_SIZE);
            Long lastOffset = null;
            this.kafkaStatus.initStartTime();
            while (!this.terminated.get()) {
                final ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                flatMessageList.clear();
                for (ConsumerRecord<String, String> record : records) {
                    flatMessageList.add(FlatMsgUtils.toFlatMsg(record, FlatMsg.class));
                    lastOffset = record.offset();
                }
                maxOffset = KafkaUtils.getMaxOffset(kafkaAdminClient, topicPartition);
                if (null == lastOffset) {
                    lastOffset = maxOffset;
                }

                count += process(elasticSearchBulk, flatMessageList);
                this.kafkaStatus.setCount(count);

                if (null == this.threadFlushShards && Math.abs(maxOffset - lastOffset) < MIN_LAY) {
                    this.rebuildStatus.addMessage(String.format("增量数据的消息积压已小于<b>%s</b>, 开始创建新索引的副本分片, 并将全部数据同步到这些副本分片中", MIN_LAY));
                    _start = System.currentTimeMillis();
                    elasticSearchBulk.flush();
                    this.esService.unoptimizeBulk(topic.getNumberOfReplicas(),
                            topic.getRefreshInterval(),
                            rebuildIndexMap.values().stream().map(IndexName::toString).collect(Collectors.toSet()));
                    this.createThreadFlushShards(rebuildIndexMap.values().stream().map(IndexName::toString).collect(Collectors.toSet()));
                }

                if (this.isShardsReady.get()) {
                    kafkaResult.setEndTime(System.currentTimeMillis());
                    this.kafkaStatus.setDone(true);
                    this.rebuildStatus.addMessage(String.format("已成功同步完新索引的副本分片。耗时:%.3f秒", calcDuringSeconds(_start)));
                    final NodeVo nodeVo = this.db2EsHttpService.getNodeByTopicName(topic.getName());
                    if (null != nodeVo) {
                        this.rebuildStatus.addMessage(String.format("正在向DB2ES(<b>id=%s</b>)服务(<b>%s</b>&nbsp;-&nbsp;<b>%s</b>)发送停止现有主题[<b>%s</b>]消费的通知", nodeVo.getId(), nodeVo.getIp(), nodeVo.getPort(), topic.getName()));
                        _start = System.currentTimeMillis();
                        this.db2EsHttpService.stop(topic.getName());
                        this.rebuildStatus.addMessage(String.format("已成功停止现有主题的消费。耗时:%.3f秒", calcDuringSeconds(_start)));
                    }
                    final String groupName = KafkaUtils.toConsumerGroupName(topic.getName());
                    final Map<TopicPartition, Checkpoint> topicPartitionCheckpointMap = new HashMap<>();
                    final Checkpoint checkpoint = new Checkpoint(topicPartition, lastOffset, -1);
                    kafkaResult.setOffset(lastOffset);
                    topicPartitionCheckpointMap.put(topicPartition, checkpoint);
                    this.zooKeeperService.setData(
                            MetaStoreUtils.getPath(groupName),
                            MetaStoreUtils.toJson(new MetaStoreUtils.StoreElement(groupName, topicPartitionCheckpointMap))
                    );
                    this.rebuildStatus.addMessage(String.format("已成功将主题[<b>%s</b>]的消费位点重置到了偏移量[<b>%s</b>]", topic.getName(), checkpoint.getOffset()));
                    elasticSearchBulk.flush();
                    this.rebuildStatus.setKafkaCount(count);
                    break;
                }
            }
        } finally {
            if (null != consumer) {
                consumer.close();
            }
            if (null != kafkaAdminClient) {
                KafkaUtils.deleteConsumer(kafkaAdminClient, new HashSet<>(Collections.singleton(rebuildConsumerName)));
                kafkaAdminClient.close();
            }
        }
        return kafkaResult;
    }

    private Set<IndexName> rebindAlias(final String indexAliasName,
                                       final Map<Integer, IndexName> rebuildIndexMap) throws IOException {

        final Set<IndexName> indexNameSet = this.esService.getIndexNameByAlias(indexAliasName);
        final IndicesAliasesRequest request = new IndicesAliasesRequest();

        for (final IndexName indexName : indexNameSet) {
            request.addAliasAction(new IndicesAliasesRequest
                    .AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
                    .index(indexName.toString())
                    .alias(indexAliasName));
        }

        for (final IndexName indexName : rebuildIndexMap.values()) {
            request.addAliasAction(new IndicesAliasesRequest
                    .AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                    .index(indexName.toString())
                    .alias(indexAliasName));
        }

        try {
            final AcknowledgedResponse response = this.esService.getElasticSearchService().getRestHighLevelClient().indices().updateAliases(request, RequestOptions.DEFAULT);
            if (!response.isAcknowledged()) {
                log.info(String.format("索引别名[%s]重新绑定失败, 原因: %s", indexAliasName, response));
            }
        } catch (final Exception exception) {
            log.info(String.format("索引别名[%s]重新绑定失败, 原因: %s", indexAliasName, exception.getMessage()));
        }
        return indexNameSet;
    }

    private int process(final ElasticSearchBulk elasticSearchBulk,
                        final List<FlatMsg> flatMessageList) throws Exception {
        final int[] result = {0};
        if (flatMessageList == null || flatMessageList.isEmpty()) {
            return result[0];
        }

        FlatMsgUtils.operate(flatMessageList, new Operation<FlatMsg>() {
            @Override
            public void insert(final FlatMsg flatMsg) throws Exception {
                final List<IndexRequest> indexRequestList = ElasticSearchUtils.toInsertRequest(
                        esService.getElasticSearchService().getRestHighLevelClient(),
                        flatMsg,
                        config,
                        TopicType.REBUILD,
                        true
                );
                elasticSearchBulk.addIndexRequests(indexRequestList);
                result[0] += indexRequestList.size();
            }

            @Override
            public void delete(final FlatMsg flatMsg) throws Exception {
                final List<DeleteRequest> deleteRequestList = ElasticSearchUtils.toDeleteRequest(
                        esService.getElasticSearchService().getRestHighLevelClient(),
                        flatMsg,
                        config,
                        TopicType.REBUILD,
                        true
                );
                elasticSearchBulk.addDeleteRequests(deleteRequestList);
                result[0] += deleteRequestList.size();
            }

            @Override
            public void update(final FlatMsg flatMsg) throws Exception {
                final List<DocWriteRequest> updateRequestList = ElasticSearchUtils.toUpdateRequest(
                        esService.getElasticSearchService().getRestHighLevelClient(),
                        flatMsg,
                        config,
                        TopicType.REBUILD,
                        true
                );
                elasticSearchBulk.add(updateRequestList);
                result[0] += updateRequestList.size();
            }

            @Override
            public void exception(final FlatMsg flatMsg, final Exception exception) {
                throw new Db2EsException(String.format("消息处理失败, 原因:[%s], 消息:[%s]", exception.getMessage(), flatMsg));
            }
        });

        return result[0];
    }

    private void createThreadFlushShards(final Set<String> rebuildIndexName) {
        if (null != this.threadFlushShards) {
            throw new Db2EsException("线程已启动");
        }

        this.threadFlushShards = new Thread(() -> {

            long trytimes = 0L;
            while (true) {
                trytimes++;
                final List<IndexVo> indexVos = this.esService.listIndexVo(rebuildIndexName);
                final Set<IndexVo> readyShards = indexVos.stream().filter(p -> "started".equalsIgnoreCase(p.getState())).collect(Collectors.toSet());
                if (trytimes >= 10L) {
                    final Set<IndexVo> avaiableShards = indexVos.stream().filter(p -> !ObjectUtils.isEmpty(p.getIp())).collect(Collectors.toSet());
                    if (avaiableShards.size() == readyShards.size()) {
                        isShardsReady.set(true);
                        break;
                    }
                } else {
                    if (indexVos.size() <= readyShards.size()) {
                        isShardsReady.set(true);
                        break;
                    }
                }
                CommonTool.sleep(1000);
            }
        });
        this.threadFlushShards.start();
    }

    private static double calcDuringSeconds(final long start) {
        return (System.currentTimeMillis() - start) / 1000.0;
    }

    private void clearThreadFlushShards() throws InterruptedException {
        if (null != this.threadFlushShards) {
            this.threadFlushShards.interrupt();
            this.threadFlushShards.join(10000, 0);
            this.threadFlushShards = null;
        }
    }

    private void createDataSource(final Map<DataSourceVo, Set<String>> tableSourceMap) throws CanalException {
        for (final Map.Entry<DataSourceVo, Set<String>> pair : tableSourceMap.entrySet()) {
            this.checkTerminated(null);

            final DataSourceVo dataSourceVo = pair.getKey();
            final Set<String> tableNameSet = pair.getValue();

            final DataSource dataSource = DataSourceTool.createHikariDataSource(
                    dataSourceVo.getHost().trim(),
                    dataSourceVo.getPort().toString().trim(),
                    dataSourceVo.getDatabaseName().trim(),
                    dataSourceVo.getUid().trim(),
                    dataSourceVo.getPwd().trim(),
                    10,
                    20
            );
            this.dataSourceMap.put(dataSourceVo, dataSource);
        }
    }

    private void clearDataSource() {
        if (this.dataSourceMap.isEmpty()) {
            return;
        }

        for (final Map.Entry<DataSourceVo, DataSource> pair : this.dataSourceMap.entrySet()) {
            DataSourceTool.close(pair.getValue());
        }
        this.dataSourceMap.clear();
    }

    private Map<DataSourceVo, Long> getDbCurrentTime() throws Exception {
        final Map<DataSourceVo, Long> result = new ConcurrentHashMap<>();
        final List<Exception> exceptionList = new ArrayList<>();

        final CountDownLatch cdl = new CountDownLatch(this.dataSourceMap.size());
        for (final Map.Entry<DataSourceVo, DataSource> pair : this.dataSourceMap.entrySet()) {
            new Thread(() -> {
                Connection conn = null;
                Statement statement = null;
                ResultSet rs = null;
                try {
                    conn = pair.getValue().getConnection();
                    statement = conn.createStatement();
                    rs = statement.executeQuery("SELECT REPLACE(unix_timestamp(current_timestamp(3)),'.','')");
                    while (rs.next()) {
                        result.put(pair.getKey(), rs.getLong(1));
                    }
                } catch (final Exception exception) {
                    exceptionList.add(exception);
                } finally {
                    DataSourceTool.close(rs);
                    DataSourceTool.close(statement);
                    DataSourceTool.close(conn);
                    cdl.countDown();
                }
            }).start();
        }

        cdl.await();

        if (!exceptionList.isEmpty()) {
            throw exceptionList.get(0);
        }
        return result;
    }

    private void checkTerminated(final Exception exception) throws CanalException {
        if (this.terminated.get()) {
            throw new CanalException(exception);
        }
    }

    private static class CanalException extends Exception {
        private final Throwable throwable;

        public CanalException(final Throwable throwable) {
            super(throwable);
            this.throwable = throwable;
        }

        public Throwable getThrowable() {
            return throwable;
        }
    }

    @ToString
    public static class DbStatus {
        private boolean done = false;
        private final AtomicBoolean terminated;
        private List<PopulateRunner> populateRunnerList;
        private final String name;
        private long count;
        private int tps;
        private long startTime;
        private long duringSeconds;

        public DbStatus(final String name,
                        final AtomicBoolean terminated) {
            this.name = name;
            this.terminated = terminated;
        }

        public final void initStartTime(final List<PopulateRunner> populateRunnerList) {
            this.populateRunnerList = populateRunnerList;
            this.startTime = System.currentTimeMillis();
        }

        public final String getName() {
            return name;
        }

        public final long getCount() {
            long result = 0L;
            if (null != this.populateRunnerList) {
                for (PopulateRunner populateRunner : populateRunnerList) {
                    result += populateRunner.getCount();
                }
            }
            return result;
        }

        public final int getTps() {
            if (this.done || this.terminated.get()) {
                return this.tps;
            }
            final long count = this.getCount();
            final double duringSeconds = calcDuringSeconds(this.startTime);
            int tps = (int) (count / duringSeconds);
            if (tps <= 0) {
                tps = (int) count;
            }
            this.tps = tps;
            return this.tps;
        }

        public final boolean isDone() {
            return done;
        }

        public final void setDone(final boolean done) {
            this.done = done;
        }

        public final long getStartTime() {
            return startTime;
        }

        public final long getDuringSeconds() {
            if (this.startTime > 0 && !this.done && !this.terminated.get()) {
                this.duringSeconds = (System.currentTimeMillis() - this.startTime) / 1000L;
            }
            return this.duringSeconds;
        }
    }

    @ToString
    public static class KafkaStatus {
        private boolean done = false;
        private final AtomicBoolean terminated;
        private final String name;
        private long count;
        private int tps;
        private long startTime;
        private long duringSeconds;

        public final String getName() {
            return name;
        }

        public final long getCount() {
            return count;
        }

        public final void setCount(final long count) {
            this.count = count;
        }

        public final int getTps() {
            if (this.done || this.terminated.get()) {
                return this.tps;
            }
            final long count = this.getCount();
            final double duringSeconds = calcDuringSeconds(this.startTime);
            int tps = (int) (count / duringSeconds);
            if (tps <= 0) {
                tps = (int) count;
            }
            this.tps = tps;
            return this.tps;
        }

        public KafkaStatus(final String name,
                           final AtomicBoolean terminated) {
            this.name = name;
            this.terminated = terminated;
        }

        public final void initStartTime() {
            this.startTime = System.currentTimeMillis();
        }

        public final boolean isDone() {
            return done;
        }

        public final void setDone(final boolean done) {
            this.done = done;
        }

        public final long getStartTime() {
            return startTime;
        }

        public final long getDuringSeconds() {
            if (this.startTime > 0 && !this.done && !this.terminated.get()) {
                this.duringSeconds = (System.currentTimeMillis() - this.startTime) / 1000L;
            }
            return this.duringSeconds;
        }
    }

    @ToString
    @Data
    public static class RebuildStatus {
        private Set<String> rebuildIndexNames;
        private boolean complete;
        private boolean error;
        private String name;
        private List<String> message;
        private int progress;
        private long dbCount;
        private int dbTps;
        private long kafkaCount;
        private int kafkaTps;
        private DbStatus dbStatus;
        private KafkaStatus kafkaStatus;

        public final void addMessage(final String message, final String color) {
            if (null == this.message) {
                this.message = new ArrayList<>();
            }
            this.message.add(String.format("<b style='color:%s'>%s:</b><span style='color:%s'>&nbsp;%s</span><br/>",
                    color,
                    CommonUtils.formatMs(new Date()),
                    color,
                    message));
        }

        public final void addErrorMesssage(final String message, final String color) {
            if (null == this.message) {
                this.message = new ArrayList<>();
            }
            this.message.add(String.format("<b style='color:%s'>%s:</b>&nbsp;%s<br/>",
                    color,
                    CommonUtils.formatMs(new Date()), message));
        }


        public final void addMessage(final String message) {
            addMessage(message, "black");
        }

        public final void addErrorMesssage(final String message) {
            addErrorMesssage(message, "red");
        }

        public final void addProgress(final int progress) {
            this.progress += progress;
            if (this.progress >= 100) {
                this.progress = 100;
            }
        }

        public final void setProgress(final int progress) {
            this.progress = progress;
        }

        public final void clear(String name) {
            this.complete = false;
            this.error = false;
            this.name = name;
            this.message = new ArrayList<>();
            this.progress = 0;
            this.dbCount = 0L;
            this.dbTps = 0;
            this.kafkaCount = 0L;
            this.kafkaTps = 0;
            this.dbStatus = null;
            this.kafkaStatus = null;
            this.rebuildIndexNames = null;
        }
    }

    @Data
    @ToString
    public static class KafkaResult {
        private Long endTime;
        private Long offset;
    }
}