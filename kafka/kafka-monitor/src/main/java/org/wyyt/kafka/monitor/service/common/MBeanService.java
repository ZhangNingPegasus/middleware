package org.wyyt.kafka.monitor.service.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.wyyt.kafka.monitor.common.JMX;
import org.wyyt.kafka.monitor.entity.vo.KafkaBrokerVo;
import org.wyyt.kafka.monitor.entity.vo.MBeanVo;

import java.util.ArrayList;
import java.util.List;

/**
 * The service for kafka's MBean.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
@Service
public class MBeanService {

    private final KafkaJmxService kafkaJmxService;

    public MBeanService(final KafkaJmxService kafkaJmxService) {
        this.kafkaJmxService = kafkaJmxService;
    }

    public MBeanVo bytesInPerSec(final KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.BYTES_IN_PER_SEC);
    }

    public MBeanVo bytesInPerSec(final KafkaBrokerVo brokerInfo,
                                 final String topicName) {
        return getMBeanInfo(brokerInfo, JMX.BYTES_IN_PER_SEC + ",topic=" + topicName);
    }

    public MBeanVo bytesOutPerSec(final KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.BYTES_OUT_PER_SEC);
    }

    public MBeanVo bytesOutPerSec(final KafkaBrokerVo brokerInfo,
                                  final String topic) {
        return getMBeanInfo(brokerInfo, JMX.BYTES_OUT_PER_SEC + ",topic=" + topic);
    }

    public MBeanVo bytesRejectedPerSec(final KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.BYTES_REJECTED_PER_SEC);
    }

    public MBeanVo bytesRejectedPerSec(final KafkaBrokerVo brokerInfo,
                                       final String topic) {
        return getMBeanInfo(brokerInfo, JMX.BYTES_REJECTED_PER_SEC + ",topic=" + topic);
    }

    public MBeanVo failedFetchRequestsPerSec(final KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.FAILED_FETCH_REQUESTS_PER_SEC);
    }

    public MBeanVo failedFetchRequestsPerSec(final KafkaBrokerVo brokerInfo,
                                             final String topic) {
        return getMBeanInfo(brokerInfo, JMX.FAILED_FETCH_REQUESTS_PER_SEC + ",topic=" + topic);
    }

    public MBeanVo failedProduceRequestsPerSec(final KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.FAILED_PRODUCE_REQUESTS_PER_SEC);
    }

    public MBeanVo failedProduceRequestsPerSec(final KafkaBrokerVo brokerInfo,
                                               final String topic) {
        return getMBeanInfo(brokerInfo, JMX.FAILED_PRODUCE_REQUESTS_PER_SEC + ",topic=" + topic);
    }

    public MBeanVo messagesInPerSec(final KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.MESSAGES_IN_PER_SEC);
    }

    public MBeanVo messagesInPerSec(final KafkaBrokerVo brokerInfo,
                                    final String topic) {
        return getMBeanInfo(brokerInfo, JMX.MESSAGES_IN_PER_SEC + ",topic=" + topic);
    }

    public MBeanVo produceMessageConversionsPerSec(final KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.PRODUCE_MESSAGE_CONVERSIONS_PER_SEC);
    }

    public MBeanVo produceMessageConversionsPerSec(final KafkaBrokerVo brokerInfo,
                                                   final String topic) {
        return getMBeanInfo(brokerInfo, JMX.PRODUCE_MESSAGE_CONVERSIONS_PER_SEC + ",topic=" + topic);
    }

    public MBeanVo totalFetchRequestsPerSec(final KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.TOTAL_FETCH_REQUESTS_PER_SEC);
    }

    public MBeanVo totalFetchRequestsPerSec(final KafkaBrokerVo brokerInfo,
                                            final String topic) {
        return getMBeanInfo(brokerInfo, JMX.TOTAL_FETCH_REQUESTS_PER_SEC + ",topic=" + topic);
    }

    public MBeanVo totalProduceRequestsPerSec(final KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.TOTAL_PRODUCE_REQUESTS_PER_SEC);
    }

    public MBeanVo totalProduceRequestsPerSec(final KafkaBrokerVo brokerInfo,
                                              final String topic) {
        return getMBeanInfo(brokerInfo, JMX.TOTAL_PRODUCE_REQUESTS_PER_SEC + ",topic=" + topic);
    }

    public MBeanVo replicationBytesInPerSec(final KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.REPLICATION_BYTES_IN_PER_SEC);
    }

    public MBeanVo replicationBytesInPerSec(final KafkaBrokerVo brokerInfo,
                                            final String topic) {
        return getMBeanInfo(brokerInfo, JMX.REPLICATION_BYTES_IN_PER_SEC + ",topic=" + topic);
    }

    public MBeanVo replicationBytesOutPerSec(final KafkaBrokerVo brokerInfo) {
        return getMBeanInfo(brokerInfo, JMX.REPLICATION_BYTES_OUT_PER_SEC);
    }

    public MBeanVo replicationBytesOutPerSec(final KafkaBrokerVo brokerInfo,
                                             final String topic) {
        return getMBeanInfo(brokerInfo, JMX.REPLICATION_BYTES_OUT_PER_SEC + ",topic=" + topic);
    }

    public Long getOsTotalMemory(final KafkaBrokerVo brokerInfo) throws Exception {
        return Long.parseLong(this.kafkaJmxService.getData(brokerInfo, JMX.OPERATING_SYSTEM, JMX.TOTAL_PHYSICAL_MEMORY_SIZE));
    }

    public Long getOsFreeMemory(final KafkaBrokerVo brokerInfo) throws Exception {
        return Long.parseLong(this.kafkaJmxService.getData(brokerInfo, JMX.OPERATING_SYSTEM, JMX.FREE_PHYSICAL_MEMORY_SIZE));
    }

    private MBeanVo getMBeanInfo(final KafkaBrokerVo brokerInfo,
                                 final String name) {
        final MBeanVo mbeanVo = new MBeanVo();
        try {
            final List<String> nameList = new ArrayList<>();
            final List<String> attributeList = new ArrayList<>();

            nameList.add(name);
            attributeList.add("OneMinuteRate");

            nameList.add(name);
            attributeList.add("FiveMinuteRate");

            nameList.add(name);
            attributeList.add("FifteenMinuteRate");

            nameList.add(name);
            attributeList.add("MeanRate");

            final String[] data = this.kafkaJmxService.getData(brokerInfo, nameList.toArray(new String[]{}), attributeList.toArray(new String[]{}));

            mbeanVo.setOneMinute(data[0]);
            mbeanVo.setFiveMinute(data[1]);
            mbeanVo.setFifteenMinute(data[2]);
            mbeanVo.setMeanRate(data[3]);

            mbeanVo.setDblOneMinute(Double.parseDouble(data[0]));
            mbeanVo.setDblFiveMinute(Double.parseDouble(data[1]));
            mbeanVo.setDblFifteenMinute(Double.parseDouble(data[2]));
            mbeanVo.setDblMeanRate(Double.parseDouble(data[3]));
        } catch (final Exception ignored) {
            mbeanVo.setFifteenMinute("0.0");
            mbeanVo.setFiveMinute("0.0");
            mbeanVo.setMeanRate("0.0");
            mbeanVo.setOneMinute("0.0");
        }
        return mbeanVo;
    }
}
