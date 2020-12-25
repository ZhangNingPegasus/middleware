package org.wyyt.sharding.db2es.client.common;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.wyyt.sharding.db2es.client.cache.CacheWrapper;
import org.wyyt.sharding.db2es.client.db.DbWrapper;
import org.wyyt.sharding.db2es.client.ding.DingDingWrapper;
import org.wyyt.sharding.db2es.client.elasticsearch.ElasticSearchWrapper;
import org.wyyt.sharding.db2es.client.elasticsearch.ElasticSearchWrapperImpl;
import org.wyyt.sharding.db2es.client.http.HttpServerWrapper;
import org.wyyt.sharding.db2es.client.kafka.KafkaAdminClientWrapper;
import org.wyyt.sharding.db2es.client.processor.ProcessorWrapper;
import org.wyyt.sharding.db2es.client.zookeeper.ZooKeeperWrapper;
import org.wyyt.sharding.db2es.core.entity.domain.Config;
import org.wyyt.sharding.db2es.core.entity.domain.TableMap;
import org.wyyt.sharding.db2es.core.entity.persistent.Property;
import org.wyyt.sharding.db2es.core.entity.persistent.Topic;
import org.wyyt.sharding.db2es.core.exception.Db2EsException;
import org.wyyt.sharding.db2es.core.util.CommonUtils;
import org.wyyt.sharding.db2es.core.util.kafka.KafkaUtils;
import org.wyyt.tool.resource.ResourceTool;

import java.io.Closeable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * the context of db2es-client
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
public final class Context implements Closeable {
    @Getter
    private final DbWrapper dbWrapper;
    @Getter
    private final ZooKeeperWrapper zooKeeperWrapper;
    @Getter
    private final ElasticSearchWrapper elasticSearchWrapper;
    @Getter
    private final DingDingWrapper dingDingWrapper;
    @Getter
    private final ProcessorWrapper processorWrapper;
    @Getter
    private final HttpServerWrapper httpServerWrapper;
    @Getter
    private final KafkaAdminClientWrapper kafkaAdminClientWrapper;
    @Getter
    private final Config config;
    @Getter
    private final String kafkaBootstrapServers;
    @Getter
    private final CacheWrapper cacheWrapper;

    public Context(final Config config) throws Exception {
        this.config = config;

        if (CommonUtils.isLocalPortUsing(this.config.getDb2EsPort())) {
            throw new Db2EsException(String.format("端口[%s]已被占用", this.config.getDb2EsPort()));
        }

        this.zooKeeperWrapper = new ZooKeeperWrapper(this);

        log.info(String.format("%s is trying to get leader [db2es.id = %s]", Utils.getLocalIpInfo(this), config.getDb2EsId()));
        this.zooKeeperWrapper.electLeader();
        log.info(String.format("%s has elected the leader [db2es.id = %s] with successfully", Utils.getLocalIpInfo(this), config.getDb2EsId()));

        this.dbWrapper = new DbWrapper(config);
        this.refreshExtraConfig();
        this.cacheWrapper = new CacheWrapper();
        this.kafkaBootstrapServers = KafkaUtils.getKafkaServers(this.zooKeeperWrapper.getCuratorFramework());
        this.elasticSearchWrapper = new ElasticSearchWrapperImpl(this);
        this.dingDingWrapper = new DingDingWrapper(this);
        this.processorWrapper = new ProcessorWrapper(this);
        this.kafkaAdminClientWrapper = new KafkaAdminClientWrapper(this);
        this.httpServerWrapper = new HttpServerWrapper(this);
        this.initTopics();
    }

    public final void refreshExtraConfig() throws Exception {
        final List<Property> properties = this.dbWrapper.listProperty();
        final TableMap tableMap = this.dbWrapper.listTableMap();
        CommonUtils.fillConfig(properties, tableMap, this.config);
        log.info("All configuration from database loaded with successfully");
    }

    @Override
    public final void close() {
        final long start = System.currentTimeMillis();

        log.info(String.format("[%s] is going to give up leader with [db2es.id = %s]", Utils.getLocalIpInfo(this), this.getConfig().getDb2EsId()));

        ResourceTool.closeQuietly(this.dbWrapper);
        ResourceTool.closeQuietly(this.httpServerWrapper);
        ResourceTool.closeQuietly(this.processorWrapper);
        ResourceTool.closeQuietly(this.kafkaAdminClientWrapper);
        ResourceTool.closeQuietly(this.zooKeeperWrapper);
        ResourceTool.closeQuietly(this.elasticSearchWrapper);
        ResourceTool.closeQuietly(this.dingDingWrapper);
        ResourceTool.closeQuietly(this.cacheWrapper);

        log.info(String.format("All resources released, %s seconds spent", (System.currentTimeMillis() - start) / 1000.0));
    }

    private void initTopics() throws Exception {
        final Set<Topic> topics = this.dbWrapper.listTopics(this.config.getDb2EsId());
        final Map<String, Topic> topicMap = new HashMap<>();
        if (null != topics) {
            for (final Topic topic : topics) {
                topicMap.put(topic.getName(), topic);
            }
        }
        this.config.setTopicMap(topicMap);
    }
}