package org.wyyt.db2es.core.util.kafka;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.common.TopicPartition;
import org.json.JSONObject;
import org.wyyt.db2es.core.util.zookeeper.ZooKeeperUtils;

import java.util.*;

import static org.wyyt.db2es.core.entity.domain.Names.ZOOKEEPER_BROKER_IDS_PATH;

/**
 * the common functions of kafka
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public final class KafkaUtils {

    public static String getKafkaServers(final CuratorFramework curatorFramework) throws Exception {
        final List<String> brokerIds = ZooKeeperUtils.getChildren(curatorFramework, ZOOKEEPER_BROKER_IDS_PATH);
        final List<String> result = new ArrayList<>(brokerIds.size());

        for (final String brokerId : brokerIds) {
            final String brokerInfoJson = ZooKeeperUtils.getData(curatorFramework, String.format("%s/%s", ZOOKEEPER_BROKER_IDS_PATH, brokerId));
            final JSONObject jsonObject = new JSONObject(brokerInfoJson);
            final String host = jsonObject.optString("host", "").trim();
            final String port = jsonObject.optString("port", "").trim();
            if (StringUtils.isEmpty(host) || StringUtils.isEmpty(port)) {
                continue;
            }
            result.add(String.format("%s:%s", host, port));
        }
        return StringUtils.join(result, ',');
    }

    public static long getMaxOffset(final KafkaAdminClient kafkaAdminClient,
                                    final TopicPartition topicPartition) throws Exception {
        final Map<TopicPartition, OffsetSpec> topicPartitionOffsets = new HashMap<>();
        topicPartitionOffsets.put(topicPartition, new OffsetSpec());

        final ListOffsetsResult listOffsetsResult = kafkaAdminClient.listOffsets(topicPartitionOffsets);
        final Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> topicPartitionListOffsetsResultInfoMap = listOffsetsResult.all().get();

        for (final Map.Entry<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> pair : topicPartitionListOffsetsResultInfoMap.entrySet()) {
            if (topicPartition.equals(pair.getKey())) {
                return pair.getValue().offset();
            }
        }
        return 0L;
    }

    public static String toConsumerGroupName(final String topicName) {
        return String.format("_db2es_consumer_for_%s_", topicName);
    }

    public static String toRebuildConsumerGroupName(final String topicName) {
        return String.format("_db2es_rebuilding_consumer_for_%s_", topicName);
    }

    public static void deleteConsumer(final KafkaAdminClient kafkaAdminClient,
                                      final Set<String> groupIds) {
        kafkaAdminClient.deleteConsumerGroups(groupIds);
    }
}