package org.wyyt.sharding.db2es.core.util.metastore;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.kafka.common.TopicPartition;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wyyt.sharding.db2es.core.entity.domain.Checkpoint;
import org.wyyt.sharding.db2es.core.entity.domain.Common;

import java.util.HashMap;
import java.util.Map;

/**
 * the common functions of MetaStore
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public final class MetaStoreUtils {
    public final static String ZOOKEEPER_PATH = "%s/metastore/groups/%s";
    private static final String GROUP_NAME = "groupName";
    private static final String TOPIC_NAME = "topicName";
    private static final String PARTITION_NAME = "partition";
    private static final String OFFSET_NAME = "offset";
    private static final String TIMESTAMP_NAME = "timestamp";
    private static final String CHECKPOINT_NAME = "checkpoint";

    public static String getPath(final String groupName) {
        return String.format(ZOOKEEPER_PATH, Common.ZK_ROOT_PATH, groupName);
    }

    public static String toJson(final StoreElement storeElement) {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(GROUP_NAME, storeElement.groupName);
        final JSONArray jsonArray = new JSONArray();
        storeElement.getCheckpointMap().forEach((topicPartition, checkpoint) -> {
            final JSONObject streamCheckpointJsonObject = new JSONObject();
            streamCheckpointJsonObject.put(TOPIC_NAME, topicPartition.topic());
            streamCheckpointJsonObject.put(PARTITION_NAME, topicPartition.partition());
            streamCheckpointJsonObject.put(OFFSET_NAME, checkpoint.getOffset());
            streamCheckpointJsonObject.put(TIMESTAMP_NAME, checkpoint.getTimestamp());
            jsonArray.put(streamCheckpointJsonObject);
        });

        jsonObject.put(CHECKPOINT_NAME, jsonArray);
        return jsonObject.toString();
    }

    public static StoreElement fromString(final String jsonString) {
        final JSONObject jsonObject = new JSONObject(jsonString);
        final String groupName = jsonObject.getString(GROUP_NAME);
        final JSONArray streamCheckpointJsonObject = jsonObject.getJSONArray(CHECKPOINT_NAME);
        final Map<TopicPartition, Checkpoint> checkpointInfo = new HashMap<>();
        for (final Object o : streamCheckpointJsonObject) {
            final JSONObject tpAndCheckpoint = (JSONObject) o;
            final String topic = tpAndCheckpoint.getString(TOPIC_NAME);
            final int partition = tpAndCheckpoint.getInt(PARTITION_NAME);
            final long offset = tpAndCheckpoint.getLong(OFFSET_NAME);
            final long timestamp = tpAndCheckpoint.getLong(TIMESTAMP_NAME);
            checkpointInfo.put(new TopicPartition(topic, partition), new Checkpoint(new TopicPartition(topic, partition), offset, timestamp));
        }
        return new StoreElement(groupName, checkpointInfo);
    }

    @Data
    @AllArgsConstructor
    public static class StoreElement {
        private final String groupName;
        private final Map<TopicPartition, Checkpoint> checkpointMap;
    }
}