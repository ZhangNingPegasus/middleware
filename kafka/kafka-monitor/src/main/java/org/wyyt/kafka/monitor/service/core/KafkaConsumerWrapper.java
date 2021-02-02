package org.wyyt.kafka.monitor.service.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.wyyt.kafka.monitor.common.Constants;
import org.wyyt.kafka.monitor.entity.po.MaxOffset;
import org.wyyt.kafka.monitor.service.dto.TopicRecordService;
import org.wyyt.tool.resource.ResourceTool;

import java.io.Closeable;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

/**
 * the wrapper class of Kafka Consumer,which providing each of methods to manipulate Kafka consumer
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class KafkaConsumerWrapper implements Closeable {
    private final KafkaConsumer<String, String> kafkaConsumer;
    private final TopicRecordService topicRecordService;

    public KafkaConsumerWrapper(final TopicRecordService topicRecordService,
                                final String kafkaBootstrapServers,
                                final String groupName) {
        this.topicRecordService = topicRecordService;
        final Properties consumerConfig = new Properties();
        consumerConfig.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        //消费组名称，名称相同的视为同一个消费组。同一个消费组内属于单播模式，不同消费组之间属于广播模式
        consumerConfig.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupName);
        //ID在发出请求时传递给服务器;用于服务器端日志记录。
        consumerConfig.put(ConsumerConfig.CLIENT_ID_CONFIG, String.format("%s_message_monitor", groupName));
        //如果为true，则消费者的偏移量将在后台定期提交，默认值为true
        consumerConfig.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        //Consumer每次调用poll()时取到的records的最大数
        consumerConfig.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, Constants.CAPACITY.toString());
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
        this.kafkaConsumer = new KafkaConsumer<>(consumerConfig);
    }

    public void assign(final List<TopicPartition> topicPartitionList) {
        this.kafkaConsumer.assign(topicPartitionList);
        for (final TopicPartition topicPartition : topicPartitionList) {
            final MaxOffset maxOffset = this.topicRecordService.listMaxOffset(topicPartition);
            if (null == maxOffset) {
                this.kafkaConsumer.seek(topicPartition, 0L);
            } else {
                this.kafkaConsumer.seek(topicPartition, maxOffset.getOffset() + 1);
            }
        }
    }

    public ConsumerRecords<String, String> poll() {
        return this.kafkaConsumer.poll(Duration.ofMillis(500));
    }

    @Override
    public void close() {
        ResourceTool.closeQuietly(this.kafkaConsumer);
    }
}