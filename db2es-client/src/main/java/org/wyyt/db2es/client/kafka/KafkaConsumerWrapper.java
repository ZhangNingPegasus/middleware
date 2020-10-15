package org.wyyt.db2es.client.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.util.Assert;
import org.wyyt.db2es.client.common.CheckpointExt;
import org.wyyt.db2es.client.common.Context;
import org.wyyt.db2es.client.common.Utils;
import org.wyyt.db2es.core.entity.domain.Common;
import org.wyyt.db2es.core.entity.domain.TopicOffset;
import org.wyyt.tool.exception.ExceptionTool;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * the wrapper class of Kafka Consumer,which providing each of methods to manipulate Kafka consumer
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
public final class KafkaConsumerWrapper implements Closeable {
    private final KafkaConsumer<String, String> kafkaConsumer;
    private final Context context;

    public KafkaConsumerWrapper(final String groupName,
                                final Context context) {
        this.context = context;
        final Properties consumerConfig = new Properties();
        consumerConfig.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, context.getKafkaBootstrapServers());
        //消费组名称，名称相同的视为同一个消费组。同一个消费组内属于单播模式，不同消费组之间属于广播模式
        consumerConfig.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupName);
        //ID在发出请求时传递给服务器;用于服务器端日志记录。
        consumerConfig.put(ConsumerConfig.CLIENT_ID_CONFIG, String.format("%s_%s", groupName, Utils.getLocalIp(this.context).getLocalIp()));
        //如果为true，则消费者的偏移量将在后台定期提交，默认值为true
        consumerConfig.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        //Consumer每次调用poll()时取到的records的最大数
        consumerConfig.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "2048");
        //调用poll方法时不仅会拉取消息、异步提交消费位点，还会发送心跳包，但是，当Consumer由于某种原因不能发Heartbeat到coordinator时,且时间超过session.timeout.ms时,就会认为该consumer已退出,会进行rebalance操作,它所订阅的分区会分配到同一group内的其它的consumer上
        consumerConfig.setProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, String.valueOf(1000 * 60 * 5));
        //调用poll方法之间的最大延迟,如果超过max.poll.interval.ms还未调用poll时，就会认为该consumer已退出,会进行rebalance操作,它所订阅的分区会分配到同一group内的其它的consumer上
        consumerConfig.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, String.valueOf(1000 * 60 * 5));
        //earliest: 从上次消费的位置接着消费，如果没有，则从0开始消费;
        //latest: 从上次消费的位置接着消费，如果没有，则从这个时间节点后发送的消息开始消费;
        //none: 从上次消费的位置接着消费，如果没有，则抛出异常
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        //key的反序列化类
        consumerConfig.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class.getCanonicalName());
        //value的反序列化类
        consumerConfig.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class.getCanonicalName());
        //kafka消费拦截器
        consumerConfig.setProperty(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, ClusterSwitchListener.class.getCanonicalName());
        this.kafkaConsumer = new KafkaConsumer<>(consumerConfig);
    }

    public void assign(final TopicPartition topicPartition,
                       final CheckpointExt checkpoint) {
        this.kafkaConsumer.assign(Collections.singletonList(topicPartition));
        if (checkpoint.getTimestamp() > -1) {
            setConsumerPositionByTimestamp(topicPartition, checkpoint);
        } else {
            setConsumerPositionByOffset(topicPartition, checkpoint);
        }
    }

    public ConsumerRecords<String, String> poll() {
        return this.kafkaConsumer.poll(Duration.ofMillis(500));
    }

    public KafkaConsumer<String, String> getKafkaConsumer() {
        return this.kafkaConsumer;
    }

    @Override
    public void close() {
        if (null != this.kafkaConsumer) {
            this.kafkaConsumer.close();
        }
    }

    private void setConsumerPositionByOffset(final TopicPartition topicPartition,
                                             final CheckpointExt checkpoint) {
        final long offset = Math.max(checkpoint.getOffset(), 0);
        this.kafkaConsumer.seek(topicPartition, offset);
        log.info(String.format("KafkaConsumerWrap: assigned for topic[%s] by offset with checkpoint[timestamp=%s, offset=%s]",
                topicPartition,
                checkpoint.getTimestamp(),
                checkpoint.getOffset()));
    }

    private void setConsumerPositionByTimestamp(final TopicPartition topicPartition,
                                                final CheckpointExt checkpoint) {
        final long timestamp = Math.max(checkpoint.getTimestamp(), 0);
        final Map<TopicPartition, OffsetAndTimestamp> topicPartitionOffsetAndTimestampMap = this.kafkaConsumer.offsetsForTimes(Collections.singletonMap(topicPartition, timestamp));
        OffsetAndTimestamp offsetAndTimestamp = topicPartitionOffsetAndTimestampMap.get(topicPartition);
        if (null == offsetAndTimestamp) {
            try {
                final Map<TopicPartition, TopicOffset> topicOffsetMap = this.context.getKafkaAdminClientWrapper().listOffset(Collections.singletonList(topicPartition));
                if (topicOffsetMap.containsKey(topicPartition)) {
                    final TopicOffset topicOffset = topicOffsetMap.get(topicPartition);
                    if (null == topicOffset.getOffset() && null == topicOffset.getOffsetTimestamp()) {
                        offsetAndTimestamp = new OffsetAndTimestamp(0, 0, null);
                    } else {
                        throw new KafkaException(String.format("主题[%s]根据时间戳[%s]定位偏移量失败, 可能因为该时间戳超过了kafka设置的过期时间而被删除(意味着该主题在kafka设定的过期时间内, 没有收到任何的消息), 或者删除了主题但没有删除db2es中间件对应的ZooKeeper节点[%s]",
                                topicPartition,
                                timestamp,
                                Common.ZK_ROOT_PATH));
                    }
                }
            } catch (final Exception exception) {
                log.error(String.format("KafkaConsumerWrap: list offset meet error, %s", ExceptionTool.getRootCauseMessage(exception)), exception);
                throw new KafkaException(exception);
            }
        }
        Assert.notNull(offsetAndTimestamp, "offsetAndTimestamp is null");
        this.kafkaConsumer.seek(topicPartition, offsetAndTimestamp.offset());
        log.info(String.format("KafkaConsumerWrap: assigned for topic[%s] by timestamp with checkpoint[timestamp=%s, offset=%s]",
                topicPartition,
                offsetAndTimestamp.timestamp(),
                offsetAndTimestamp.offset()));
    }
}