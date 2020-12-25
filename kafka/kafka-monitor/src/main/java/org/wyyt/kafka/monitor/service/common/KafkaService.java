package org.wyyt.kafka.monitor.service.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.requests.DescribeLogDirsResponse;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.zookeeper.data.Stat;
import org.json.JSONObject;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.admin.ui.exception.BusinessException;
import org.wyyt.kafka.monitor.common.Constants;
import org.wyyt.kafka.monitor.common.JMX;
import org.wyyt.kafka.monitor.entity.dto.SysKpi;
import org.wyyt.kafka.monitor.entity.po.Offset;
import org.wyyt.kafka.monitor.entity.vo.*;
import org.wyyt.tool.common.CommonTool;
import org.wyyt.tool.date.DateTool;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.resource.ResourceTool;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The service for kafka.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Slf4j
@Service
public class KafkaService implements InitializingBean, DisposableBean {
    private final KafkaZkService kafkaZkService;
    private final KafkaJmxService kafkaJmxService;
    private final MBeanService mBeanService;
    private KafkaAdminClient kafkaAdminClient;

    public KafkaService(final KafkaZkService kafkaZkService,
                        final KafkaJmxService kafkaJmxService,
                        final MBeanService mBeanService) {
        this.kafkaZkService = kafkaZkService;
        this.kafkaJmxService = kafkaJmxService;
        this.mBeanService = mBeanService;
    }

    public List<String> listTopicNames() throws Exception {
        ListTopicsResult listTopicsResult = this.kafkaAdminClient.listTopics();
        final List<String> result = new ArrayList<>(listTopicsResult.names().get());
        result.sort(Comparator.naturalOrder());
        return result;
    }

    public Map<String, Set<Integer>> listPartitionIds(final List<String> topicNameList) throws Exception {
        final Map<String, Set<Integer>> result = new HashMap<>();
        final DescribeTopicsResult describeTopicsResult = this.kafkaAdminClient.describeTopics(topicNameList);
        final Map<String, TopicDescription> descriptionMap = describeTopicsResult.all().get();
        for (final Map.Entry<String, TopicDescription> pair : descriptionMap.entrySet()) {
            result.put(pair.getKey(), pair.getValue().partitions().stream().map(TopicPartitionInfo::partition).collect(Collectors.toSet()));
        }
        return result;
    }

    public Set<Integer> listPartitionIds(final String topicName) throws Exception {
        final Map<String, Set<Integer>> result = this.listPartitionIds(Collections.singletonList(topicName));
        if (null == result || result.isEmpty()) {
            return null;
        }
        return result.values().iterator().next();
    }

    public List<OffsetVo> listOffsetVo(List<KafkaConsumerVo> kafkaConsumerVoList,
                                       final String groupId,
                                       final String topicName) throws Exception {
        final List<OffsetVo> result = new ArrayList<>();

        final Map<TopicPartition, Long> partitionOffset = this.listConsumerOffset(groupId, topicName);
        final Map<TopicPartition, Long> partitionLogSize = this.getTopicLogSizeMap(topicName);

        if (null == kafkaConsumerVoList || kafkaConsumerVoList.isEmpty()) {
            kafkaConsumerVoList = this.listKafkaConsumers(groupId, true);
        } else {
            kafkaConsumerVoList = kafkaConsumerVoList.stream().filter(p -> p.getGroupId().equals(groupId)).collect(Collectors.toList());
        }

        for (final Map.Entry<TopicPartition, Long> entrySet : partitionLogSize.entrySet()) {
            final int partitionId = entrySet.getKey().partition();
            final long logSize = entrySet.getValue();
            final TopicPartition key = new TopicPartition(topicName, partitionId);

            final OffsetVo offsetVo = new OffsetVo();
            offsetVo.setTopicName(topicName);
            offsetVo.setPartitionId(partitionId);
            offsetVo.setLogSize(logSize);
            if (partitionOffset.containsKey(key)) {
                offsetVo.setOffset(partitionOffset.get(key));
                offsetVo.setLag(offsetVo.getOffset() == -1 ? 0 : (offsetVo.getLogSize() - offsetVo.getOffset()));
            } else {
                offsetVo.setOffset(-1L);
                offsetVo.setLag(-1L);
            }
            offsetVo.setConsumerId(getConsumerId(kafkaConsumerVoList, topicName, partitionId));
            result.add(offsetVo);
        }

        if (result.size() < partitionOffset.size()) {
            for (final OffsetVo offsetVo : result) {
                partitionOffset.remove(new TopicPartition(offsetVo.getTopicName(), offsetVo.getPartitionId()));
            }

            for (final Map.Entry<TopicPartition, Long> entry : partitionOffset.entrySet()) {
                final OffsetVo offsetVo = new OffsetVo();
                offsetVo.setTopicName(entry.getKey().topic());
                offsetVo.setPartitionId(entry.getKey().partition());
                offsetVo.setOffset(entry.getValue());
                offsetVo.setLogSize(-1L);
                offsetVo.setLag(-1L);
                offsetVo.setConsumerId(String.format("分区[%s]不可用", entry.getKey()));
                result.add(offsetVo);
            }
        }

        result.sort(Comparator.comparing(OffsetVo::getPartitionId));
        return result;
    }

    public void alterOffset(final String groupId,
                            final List<Offset> offsetList) throws Exception {
        if (ObjectUtils.isEmpty(groupId) || null == offsetList || offsetList.isEmpty()) {
            return;
        }
        final Map<TopicPartition, OffsetAndMetadata> offsetsMap = new HashMap<>();
        for (Offset offset : offsetList) {
            OffsetAndMetadata offsetAndMetadata;
            if (ObjectUtils.isEmpty(offset.getMetadata())) {
                offsetAndMetadata = new OffsetAndMetadata(offset.getOffset());
            } else {
                offsetAndMetadata = new OffsetAndMetadata(offset.getOffset(), offset.getMetadata());
            }
            offsetsMap.put(new TopicPartition(offset.getTopicName(), offset.getPartitionId()), offsetAndMetadata);
        }
        this.kafkaAdminClient.alterConsumerGroupOffsets(groupId, offsetsMap).all().get();
    }

    public Map<TopicPartition, Long> getTopicLogSize(final KafkaAdminClient kafkaAdminClient,
                                                     final List<TopicPartition> topicPartitionList) throws Exception {
        final Map<TopicPartition, Long> result = new HashMap<>();
        final Map<TopicPartition, OffsetSpec> topicPartitionOffsets = new HashMap<>();
        for (final TopicPartition topicPartition : topicPartitionList) {
            topicPartitionOffsets.put(topicPartition, new OffsetSpec());
        }
        final ListOffsetsResult listOffsetsResult = kafkaAdminClient.listOffsets(topicPartitionOffsets);
        final Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> topicPartitionListOffsetsResultInfoMap = listOffsetsResult.all().get();
        for (final Map.Entry<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> pair : topicPartitionListOffsetsResultInfoMap.entrySet()) {
            result.put(pair.getKey(), pair.getValue().offset());
        }
        return result;
    }

    public Long getTopicLogSize(final KafkaAdminClient kafkaAdminClient,
                                final TopicPartition topicPartition) throws Exception {
        Map<TopicPartition, Long> result = this.getTopicLogSize(kafkaAdminClient, Collections.singletonList(topicPartition));
        if (null == result || result.isEmpty()) {
            return null;
        }
        return result.get(topicPartition);
    }

    public Map<TopicPartition, Long> getTopicLogSize(final List<TopicPartition> topicPartitionList) throws Exception {
        return new HashMap<>(getTopicLogSize(this.kafkaAdminClient, topicPartitionList));
    }


    public Map<String, Long> getTopicLogSizes(final List<String> topicNameList) throws Exception {
        final Map<String, Long> result = new HashMap<>(topicNameList.size());
        final Map<String, Set<Integer>> partitionIdMap = this.listPartitionIds(topicNameList);
        final List<TopicPartition> topicPartitionList = new ArrayList<>(topicNameList.size() * 3);

        for (final String topicName : topicNameList) {
            final Set<Integer> partitionIds = partitionIdMap.get(topicName);
            if (null == partitionIds || partitionIds.isEmpty()) {
                continue;
            }
            for (final Integer partitionId : partitionIds) {
                topicPartitionList.add(new TopicPartition(topicName, partitionId));
            }
        }
        final Map<TopicPartition, Long> topicLogSizeMap = this.getTopicLogSize(topicPartitionList);
        for (Map.Entry<TopicPartition, Long> pair : topicLogSizeMap.entrySet()) {
            String topicName = pair.getKey().topic();
            if (result.containsKey(topicName)) {
                result.put(topicName, result.get(topicName) + pair.getValue());
            } else {
                result.put(topicName, pair.getValue());
            }
        }
        return result;
    }

    public Long getTopicLogSize(final String topicName) throws Exception {
        final Map<String, Long> topicLogSizes = this.getTopicLogSizes(Collections.singletonList(topicName));
        if (null == topicLogSizes) {
            return null;
        }
        return topicLogSizes.get(topicName);
    }

    public Map<TopicPartition, Long> getTopicLogSizeMap(final String topicName) throws Exception {
        final Set<Integer> partitionIds = this.listPartitionIds(topicName);
        final List<TopicPartition> topicPartitionList = new ArrayList<>(partitionIds.size());
        for (final Integer partitionId : partitionIds) {
            topicPartitionList.add(new TopicPartition(topicName, partitionId));
        }
        return this.getTopicLogSize(topicPartitionList);
    }

    public Long getTopicLogSize(KafkaConsumer kafkaConsumer, final TopicPartition topicPartition) throws Exception {
        final Map<TopicPartition, Long> topicLogSize = this.getTopicLogSize(Collections.singletonList(topicPartition));
        if (null == topicLogSize || topicLogSize.isEmpty()) {
            return 0L;
        }
        return topicLogSize.get(topicPartition);
    }

    public String getTopicDiskSpace(final String topicName) throws Exception {
        final List<PartitionVo> partitionVoList = this.listTopicDetails(topicName, false);
        final Set<TopicPartition> topicPartitionSet = new HashSet<>();
        List<Integer> brokerIdList = partitionVoList.stream().map(p -> Integer.parseInt(p.getLeader().getPartitionId())).collect(Collectors.toList());
        long result = 0L;
        final DescribeLogDirsResult describeLogDirsResult = this.kafkaAdminClient.describeLogDirs(brokerIdList);
        final Map<Integer, Map<String, DescribeLogDirsResponse.LogDirInfo>> all = describeLogDirsResult.all().get();
        for (final Map.Entry<Integer, Map<String, DescribeLogDirsResponse.LogDirInfo>> entry : all.entrySet()) {
            final Map<String, DescribeLogDirsResponse.LogDirInfo> logDirInfoMap = entry.getValue();
            for (final Map.Entry<String, DescribeLogDirsResponse.LogDirInfo> entry1 : logDirInfoMap.entrySet()) {
                final DescribeLogDirsResponse.LogDirInfo info = entry1.getValue();
                final Map<TopicPartition, DescribeLogDirsResponse.ReplicaInfo> replicaInfoMap = info.replicaInfos;
                for (Map.Entry<TopicPartition, DescribeLogDirsResponse.ReplicaInfo> replicas : replicaInfoMap.entrySet()) {
                    if (topicName.equals(replicas.getKey().topic())) {
                        if (topicPartitionSet.contains(replicas.getKey())) {
                            continue;
                        }
                        result += replicas.getValue().size;
                        topicPartitionSet.add(replicas.getKey());
                    }
                }
            }
        }
        return CommonTool.convertSize(result);
    }

    public List<TopicVo> listTopicVos(final List<String> topicNameList) throws Exception {
        final List<TopicVo> result = new ArrayList<>(topicNameList.size());
        final List<KafkaConsumerVo> kafkaConsumerVoList = this.listKafkaConsumers();
        for (final String topicName : topicNameList) {
            final TopicVo topicVo = new TopicVo();
            topicVo.setTopicName(topicName);

            final List<String> subscribeGroupIdList = kafkaConsumerVoList.stream().filter(p -> p.getTopicNames().contains(topicName)).map(KafkaConsumerVo::getGroupId).distinct().collect(Collectors.toList());
            topicVo.setSubscribeNums(subscribeGroupIdList.size());
            topicVo.setSubscribeGroupIds(subscribeGroupIdList.toArray(new String[]{}));

            final Set<Integer> partitionIdSet = this.listPartitionIds(topicName);
            topicVo.setPartitionNum(partitionIdSet.size());
            topicVo.setPartitionIndex(StringUtils.join(partitionIdSet, ", "));

            final Stat stat = this.getTopicStat(topicName);
            topicVo.setCreateTime(new Date(stat.getCtime()));
            topicVo.setModifyTime(new Date(stat.getMtime()));
            result.add(topicVo);
        }
        return result;
    }

    public Map<String, List<PartitionVo>> listTopicDetails(final List<String> topicNameList,
                                                           final boolean needLogSize) throws Exception {
        final Map<String, List<PartitionVo>> result = new HashMap<>();
        final DescribeTopicsResult describeTopicsResult = this.kafkaAdminClient.describeTopics(topicNameList);
        final Map<String, TopicDescription> topicDescriptionMap = describeTopicsResult.all().get();
        for (final Map.Entry<String, TopicDescription> pair : topicDescriptionMap.entrySet()) {
            for (final TopicPartitionInfo partition : pair.getValue().partitions()) {
                final PartitionVo partitionVo = new PartitionVo();
                partitionVo.setTopicName(pair.getKey());
                partitionVo.setPartitionId(Integer.toString(partition.partition()));
                if (null == partition.leader()) {
                    partitionVo.setLogSize(-1L);
                    partitionVo.setStrLeader(Constants.HOST_NOT_AVAIABLE);
                    partitionVo.setStrReplicas(Constants.HOST_NOT_AVAIABLE);
                    partitionVo.setStrIsr(Constants.HOST_NOT_AVAIABLE);
                } else {
                    partitionVo.setLeader(new PartitionVo.PartionInfo(Integer.toString(partition.leader().id()), partition.leader().host(), Integer.toString(partition.leader().port()), partition.leader().rack()));
                    final List<PartitionVo.PartionInfo> partitionVoList = new ArrayList<>(partition.replicas().size());
                    for (final Node replica : partition.replicas()) {
                        partitionVoList.add(new PartitionVo.PartionInfo(Integer.toString(replica.id()), replica.host(), Integer.toString(replica.port()), replica.rack()));
                    }
                    partitionVo.setReplicas(partitionVoList);
                    final List<PartitionVo.PartionInfo> isrList = new ArrayList<>(partition.isr().size());
                    for (final Node isr : partition.isr()) {
                        isrList.add(new PartitionVo.PartionInfo(Integer.toString(isr.id()), isr.host(), Integer.toString(isr.port()), isr.rack()));
                    }
                    partitionVo.setIsr(isrList);

                    partitionVo.setStrLeader(String.format("[%s] : (%s:%s)", partitionVo.getLeader().getPartitionId(), partitionVo.getLeader().getHost(), partitionVo.getLeader().getPort()));
                    final StringBuilder strReplicas = new StringBuilder();
                    for (final PartitionVo.PartionInfo replica : partitionVo.getReplicas()) {
                        strReplicas.append(String.format("[%s] : (%s:%s), ", replica.getPartitionId(), replica.getHost(), replica.getPort()));
                    }
                    partitionVo.setStrReplicas(strReplicas.substring(0, strReplicas.length() - 2));
                    final StringBuilder strIsr = new StringBuilder();
                    for (final PartitionVo.PartionInfo isr : partitionVo.getIsr()) {
                        strIsr.append(String.format("[%s] : (%s:%s), ", isr.getPartitionId(), isr.getHost(), isr.getPort()));
                    }
                    partitionVo.setStrIsr(strIsr.substring(0, strIsr.length() - 2));
                }

                if (result.containsKey(pair.getKey())) {
                    result.get(pair.getKey()).add(partitionVo);
                } else {
                    final List<PartitionVo> partitionVoList = new ArrayList<>();
                    partitionVoList.add(partitionVo);
                    result.put(pair.getKey(), partitionVoList);
                }
            }
        }

        if (needLogSize) {
            this.kafkaConsumerDo(kafkaConsumer -> {
                for (Map.Entry<String, List<PartitionVo>> pair : result.entrySet()) {
                    for (PartitionVo partitionVo : pair.getValue()) {
                        partitionVo.setLogSize(this.getTopicLogSize(kafkaConsumer, new TopicPartition(partitionVo.getTopicName(), Integer.parseInt(partitionVo.getPartitionId()))));
                    }
                }
            });
        }

        return result;
    }

    public List<PartitionVo> listTopicDetails(final String topicName, boolean needLogSize) throws Exception {
        Map<String, List<PartitionVo>> result = this.listTopicDetails(Collections.singletonList(topicName), needLogSize);
        if (null == result || result.isEmpty()) {
            return null;
        }
        return result.get(topicName);
    }

    public List<KafkaConsumerVo> listKafkaConsumers(@Nullable final String searchGroupId,
                                                    @Nullable final Boolean isAccurate) throws Exception {
        final List<KafkaConsumerVo> result = new ArrayList<>();
        final ListConsumerGroupsResult listConsumerGroupsResult = this.kafkaAdminClient.listConsumerGroups();
        final Collection<ConsumerGroupListing> consumerGroupListings = listConsumerGroupsResult.all().get();
        for (final ConsumerGroupListing consumerGroupListing : consumerGroupListings) {
            final String groupId = consumerGroupListing.groupId();
            if (groupId.startsWith(Constants.KAFKA_MONITOR_PEGASUS_SYSTEM_PREFIX)) {
                continue;
            } else if (!ObjectUtils.isEmpty(searchGroupId) && null != isAccurate) {
                if (isAccurate) {
                    if (!groupId.equals(searchGroupId)) {
                        continue;
                    }
                } else {
                    if (!groupId.contains(searchGroupId)) {
                        continue;
                    }
                }
            }
            final KafkaConsumerVo kafkaConsumerVo = new KafkaConsumerVo();
            kafkaConsumerVo.setGroupId(groupId);

            final Set<String> hasOwnerTopics = new HashSet<>();
            final DescribeConsumerGroupsResult describeConsumerGroupsResult = this.kafkaAdminClient.describeConsumerGroups(Collections.singletonList(groupId));
            final Node coordinator = describeConsumerGroupsResult.all().get().get(groupId).coordinator();
            final ConsumerGroupDescription consumerGroupDescription = describeConsumerGroupsResult.describedGroups().get(groupId).get();
            final List<MemberDescription> members = new ArrayList<>(consumerGroupDescription.members());
            kafkaConsumerVo.setNode(String.format("%s : %s", coordinator.host(), coordinator.port()));

            final List<KafkaConsumerVo.Meta> metaList = new ArrayList<>();
            for (final MemberDescription member : members) {
                KafkaConsumerVo.Meta meta = new KafkaConsumerVo.Meta();
                meta.setConsumerId(member.consumerId());
                meta.setNode(member.host().replaceAll("/", ""));
                meta.setConsumerGroupState(consumerGroupDescription.state());

                final List<KafkaConsumerVo.TopicSubscriber> topicSubscriberList = new ArrayList<>();
                for (final TopicPartition topicPartition : member.assignment().topicPartitions()) {
                    final KafkaConsumerVo.TopicSubscriber topicSubscriber = new KafkaConsumerVo.TopicSubscriber();
                    topicSubscriber.setTopicName(topicPartition.topic());
                    topicSubscriber.setPartitionId(topicPartition.partition());
                    topicSubscriberList.add(topicSubscriber);
                    hasOwnerTopics.add(topicPartition.topic());
                }
                meta.setTopicSubscriberList(topicSubscriberList);
                metaList.add(meta);
            }

            final KafkaConsumerVo.Meta noActiveMeta = new KafkaConsumerVo.Meta();
            final List<KafkaConsumerVo.TopicSubscriber> noActivetopicSubscriberList = new ArrayList<>();
            noActiveMeta.setConsumerId("");
            noActiveMeta.setNode(" - ");
            noActiveMeta.setConsumerGroupState(consumerGroupDescription.state());
            final ListConsumerGroupOffsetsResult listConsumerGroupOffsetsResult = this.kafkaAdminClient.listConsumerGroupOffsets(groupId);
            for (final Map.Entry<TopicPartition, OffsetAndMetadata> entry : listConsumerGroupOffsetsResult.partitionsToOffsetAndMetadata().get().entrySet()) {
                final KafkaConsumerVo.TopicSubscriber topicSubscriber = new KafkaConsumerVo.TopicSubscriber();
                topicSubscriber.setTopicName(entry.getKey().topic());
                topicSubscriber.setPartitionId(entry.getKey().partition());
                if (!hasOwnerTopics.contains(entry.getKey().topic())) {
                    noActivetopicSubscriberList.add(topicSubscriber);
                }
            }
            noActiveMeta.setTopicSubscriberList(noActivetopicSubscriberList);
            if (!noActiveMeta.getTopicSubscriberList().isEmpty()) {
                metaList.add(noActiveMeta);
            }
            kafkaConsumerVo.setMetaList(metaList);
            result.add(kafkaConsumerVo);
        }

        for (final KafkaConsumerVo kafkaConsumerVo : result) {
            final Set<String> topicNameSet = new HashSet<>();
            final Set<String> activeTopicSet = new HashSet<>();
            for (final KafkaConsumerVo.Meta meta : kafkaConsumerVo.getMetaList()) {
                for (final KafkaConsumerVo.TopicSubscriber topicSubscriber : meta.getTopicSubscriberList()) {
                    topicNameSet.add(topicSubscriber.getTopicName());
                    if (!ObjectUtils.isEmpty(meta.getConsumerId()) || meta.getConsumerGroupState() == ConsumerGroupState.EMPTY) {
                        activeTopicSet.add(topicSubscriber.getTopicName());
                    }
                }
            }
            kafkaConsumerVo.setTopicNames(topicNameSet);
            kafkaConsumerVo.setActiveTopicNames(activeTopicSet);
            final Set<String> notActiveTopicNames = new HashSet<>(topicNameSet.size());
            notActiveTopicNames.addAll(topicNameSet);
            notActiveTopicNames.removeAll(activeTopicSet);
            kafkaConsumerVo.setNotActiveTopicNames(notActiveTopicNames);
            kafkaConsumerVo.setActiveTopicCount(activeTopicSet.size());
            kafkaConsumerVo.setTopicCount(topicNameSet.size());
        }
        result.sort(Comparator.comparing(KafkaConsumerVo::getGroupId));
        return result;
    }

    public List<KafkaConsumerVo> listKafkaConsumers() throws Exception {
        return this.listKafkaConsumers(null, null);
    }

    public List<KafkaConsumerVo> listKafkaConsumersByTopicName(String topicName) throws Exception {
        final List<KafkaConsumerVo> result = new ArrayList<>();
        if (ObjectUtils.isEmpty(topicName)) {
            return result;
        }
        final List<KafkaConsumerVo> kafkaConsumerVoList = this.listKafkaConsumers();
        for (final KafkaConsumerVo kafkaConsumerVo : kafkaConsumerVoList) {
            if (kafkaConsumerVo.getTopicNames().contains(topicName)) {
                result.add(kafkaConsumerVo);
            }
        }
        return result;
    }

    public List<String> listBrokerNames() throws Exception {
        return this.kafkaZkService.getChildren(Constants.ZK_BROKER_IDS_PATH);
    }

    public List<KafkaBrokerVo> listBrokerInfos() throws Exception {
        final List<String> brokerList = this.listBrokerNames();
        final List<KafkaBrokerVo> result = new ArrayList<>(brokerList.size());
        for (final String brokerName : brokerList) {
            final KafkaBrokerVo brokerInfo = this.getBrokerInfo(brokerName);
            brokerInfo.setName(brokerName);
            brokerInfo.setVersion(this.getKafkaVersion(brokerInfo));
            result.add(brokerInfo);
        }
        return result;
    }

    public String getBootstrapServers(boolean port) throws Exception {
        StringBuilder kafkaUrls = new StringBuilder();
        List<KafkaBrokerVo> brokerInfoList = this.listBrokerInfos();
        if (null == brokerInfoList || brokerInfoList.isEmpty()) {
            throw new BusinessException("KAFKA可能没有启动，请先启动kafka");
        }
        for (final KafkaBrokerVo brokerInfo : brokerInfoList) {
            if (port) {
                kafkaUrls.append(String.format("%s:%s,", brokerInfo.getHost(), brokerInfo.getPort()));
            } else {
                kafkaUrls.append(String.format("%s,", brokerInfo.getHost()));
            }
        }
        if (kafkaUrls.length() > 0) {
            kafkaUrls.delete(kafkaUrls.length() - 1, kafkaUrls.length());
        }
        return kafkaUrls.toString();
    }

    public String getBootstrapServers() throws Exception {
        return getBootstrapServers(true);
    }

    public Stat getTopicStat(String topicName) throws Exception {
        Stat stat = new Stat();
        this.kafkaZkService.getData(String.format("%s/%s", Constants.ZK_BROKERS_TOPICS_PATH, topicName), stat);
        return stat;
    }

    private KafkaBrokerVo getBrokerInfo(final String brokerName) throws Exception {
        final String brokerInfoJson = this.kafkaZkService.getData(String.format("%s/%s", Constants.ZK_BROKER_IDS_PATH, brokerName));
        final JSONObject jsonObject = new JSONObject(brokerInfoJson);
        final KafkaBrokerVo brokerVo = new KafkaBrokerVo();
        brokerVo.setHost(jsonObject.get("host").toString());
        brokerVo.setPort(jsonObject.get("port").toString());
        brokerVo.setEndpoints(jsonObject.get("endpoints").toString());
        brokerVo.setJmxPort(jsonObject.get("jmx_port").toString());
        brokerVo.setCreateTime(DateTool.format(new Date(jsonObject.getLong("timestamp"))));
        return brokerVo;
    }

    private String getKafkaVersion(final KafkaBrokerVo brokerInfo) {
        String result = " - ";
        try {
            result = this.kafkaJmxService.getData(brokerInfo, String.format("kafka.server:type=app-info,id=%s", brokerInfo.getName()), "Version");
        } catch (Exception e) {
            log.error(ExceptionTool.getRootCauseMessage(e), e);
        }
        return result;
    }

    public void sendMessage(final String topicName,
                            final String key,
                            final String value) throws Exception {
        this.kafkaProducerDo(kafkaProducer -> kafkaProducer.send(new ProducerRecord(topicName, key, value), (metadata, exception) -> {
            if (exception != null) {
                throw new BusinessException(exception);
            }
        }));
    }

    public void sendMessage(final String topicName,
                            final String value) throws Exception {
        this.sendMessage(topicName, null, value);
    }

    public List<MBeanVo> listTopicMBean(final String topicName) throws Exception {
        final Map<String, MBeanVo> result = new LinkedHashMap<>();
        final List<KafkaBrokerVo> brokerInfoList = this.listBrokerInfos();
        for (final KafkaBrokerVo brokerInfo : brokerInfoList) {
            final MBeanVo bytesIn = this.mBeanService.bytesInPerSec(brokerInfo, topicName);
            final MBeanVo bytesOut = this.mBeanService.bytesOutPerSec(brokerInfo, topicName);
            final MBeanVo bytesRejected = this.mBeanService.bytesRejectedPerSec(brokerInfo, topicName);
            final MBeanVo failedFetchRequest = this.mBeanService.failedFetchRequestsPerSec(brokerInfo, topicName);
            final MBeanVo failedProduceRequest = this.mBeanService.failedProduceRequestsPerSec(brokerInfo, topicName);
            final MBeanVo messageIn = this.mBeanService.messagesInPerSec(brokerInfo, topicName);
            final MBeanVo produceMessageConversions = this.mBeanService.produceMessageConversionsPerSec(brokerInfo, topicName);
            final MBeanVo totalFetchRequests = this.mBeanService.totalFetchRequestsPerSec(brokerInfo, topicName);
            final MBeanVo totalProduceRequests = this.mBeanService.totalProduceRequestsPerSec(brokerInfo, topicName);

            assembleMBeanInfo(result, JMX.MESSAGES_IN, messageIn);
            assembleMBeanInfo(result, JMX.BYTES_IN, bytesIn);
            assembleMBeanInfo(result, JMX.BYTES_OUT, bytesOut);
            assembleMBeanInfo(result, JMX.BYTES_REJECTED, bytesRejected);
            assembleMBeanInfo(result, JMX.FAILED_FETCH_REQUEST, failedFetchRequest);
            assembleMBeanInfo(result, JMX.FAILED_PRODUCE_REQUEST, failedProduceRequest);
            assembleMBeanInfo(result, JMX.TOTAL_FETCH_REQUESTS, totalFetchRequests);
            assembleMBeanInfo(result, JMX.TOTAL_PRODUCE_REQUESTS, totalProduceRequests);
            assembleMBeanInfo(result, JMX.PRODUCE_MESSAGE_CONVERSIONS, produceMessageConversions);
        }

        for (final Map.Entry<String, MBeanVo> entry : result.entrySet()) {
            if (null == entry || null == entry.getValue()) {
                continue;
            }
            if (entry.getKey().equals(JMX.MESSAGES_IN) || entry.getKey().equals(JMX.BYTES_IN) || entry.getKey().equals(JMX.BYTES_OUT) || entry.getKey().equals(JMX.BYTES_REJECTED)) {
                entry.getValue().setMeanRate(CommonTool.convertSize(entry.getValue().getMeanRate()));
                entry.getValue().setOneMinute(CommonTool.convertSize(entry.getValue().getOneMinute()));
                entry.getValue().setFiveMinute(CommonTool.convertSize(entry.getValue().getFiveMinute()));
                entry.getValue().setFifteenMinute(CommonTool.convertSize(entry.getValue().getFifteenMinute()));
            }
        }
        return new ArrayList<>(result.values());
    }


    public void createTopic(final String topicName,
                            final Integer partitionNumber,
                            final Integer replicationNumber) throws Exception {
        if ((null == topicName || ObjectUtils.isEmpty(topicName.trim())) && null == partitionNumber && null == replicationNumber) {
            return;
        }
        final NewTopic newTopic = new NewTopic(topicName.trim(), partitionNumber, Short.parseShort(replicationNumber.toString()));
        this.kafkaAdminClient.createTopics(Collections.singletonList(newTopic)).all().get();
    }

    public void editTopic(String topicName,
                          @Nullable final Integer partitionCount,
                          @Nullable final Integer replicationCount) throws Exception {
        if (ObjectUtils.isEmpty(topicName)) {
            return;
        }
        topicName = topicName.trim();
        Map<String, NewPartitions> newPartitionsMap = null;
        Map<TopicPartition, Optional<NewPartitionReassignment>> newPartitionReassignmentMap = null;

        final Set<Integer> partitionIds = this.listPartitionIds(topicName);
        if (null != partitionCount && partitionCount > 1 && !partitionCount.equals(partitionIds.size())) {
            if (null == partitionIds || partitionIds.isEmpty()) {
                throw new BusinessException(String.format("查询不到主题[%s]的分区信息", topicName));
            } else if (partitionCount < partitionIds.size()) {
                throw new BusinessException(String.format("主题[%s]新的分区数量必须大于[%s]", topicName, partitionIds.size()));
            }
            newPartitionsMap = new HashMap<>();
            newPartitionsMap.put(topicName, NewPartitions.increaseTo(partitionCount));
        }

        if (null != partitionCount && replicationCount >= 1) {
            final List<KafkaBrokerVo> kafkaBrokerVos = this.listBrokerInfos();
            if (replicationCount > kafkaBrokerVos.size()) {
                throw new BusinessException(String.format("主题[%s]副本分片数量不能大于kafka节点数[%s]", topicName, kafkaBrokerVos.size()));
            }

            List<PartitionVo> partitionVoList = this.listTopicDetails(topicName, false);
            if (null != partitionVoList && partitionVoList.get(0).getReplicas().size() != replicationCount) {
                newPartitionReassignmentMap = new HashMap<>();
                for (final PartitionVo partitionVo : partitionVoList) {
                    final List<Integer> newPartitions = new ArrayList<>();
                    if (replicationCount < kafkaBrokerVos.size()) {
                        for (int i = 0; i < replicationCount; i++) {
                            int nameInt = Integer.parseInt(kafkaBrokerVos.get(RandomUtils.nextInt(0, replicationCount + 1)).getName());
                            while (newPartitions.contains(nameInt)) {
                                nameInt = Integer.parseInt(kafkaBrokerVos.get(RandomUtils.nextInt(0, replicationCount + 1)).getName());
                            }
                            newPartitions.add(nameInt);
                        }
                    } else {
                        while (newPartitions.size() < replicationCount) {
                            for (KafkaBrokerVo kafkaBrokerVo : kafkaBrokerVos) {
                                newPartitions.add(Integer.parseInt(kafkaBrokerVo.getName()));
                            }
                        }
                    }
                    Collections.shuffle(newPartitions);
                    newPartitionReassignmentMap.put(new TopicPartition(topicName, Integer.parseInt(partitionVo.getPartitionId())), Optional.of(new NewPartitionReassignment(newPartitions)));
                }
            }
        }

        if (null != newPartitionsMap || null != newPartitionReassignmentMap) {
            if (null != newPartitionsMap) {
                this.kafkaAdminClient.createPartitions(newPartitionsMap).all().get();
            }
            if (null != newPartitionReassignmentMap) {
                this.kafkaAdminClient.alterPartitionReassignments(newPartitionReassignmentMap).all().get();
            }
        }
    }

    public void deleteTopic(final List<String> topicNameList) throws Exception {
        this.kafkaAdminClient.deleteTopics(topicNameList).all().get();
    }

    public void deleteConsumer(final List<String> groupIdList) throws Exception {
        this.kafkaAdminClient.deleteConsumerGroups(groupIdList).all().get();
    }

    public List<SysKpi> kpi(final Date now) throws Exception {
        final List<SysKpi> result = new ArrayList<>(SysKpi.KAFKA_KPI.values().length);
        final List<KafkaBrokerVo> brokers = this.listBrokerInfos();

        for (final SysKpi.KAFKA_KPI kpi : SysKpi.KAFKA_KPI.values()) {
            if (ObjectUtils.isEmpty(kpi.getName())) {
                continue;
            }
            final SysKpi sysKpi = new SysKpi();
            sysKpi.setKpi(kpi.getCode());
            sysKpi.setCollectTime(now);
            final StringBuilder host = new StringBuilder();
            for (final KafkaBrokerVo broker : brokers) {
                host.append(broker.getHost()).append(",");
                switch (kpi) {
                    case KAFKA_MESSAGES_IN:
                        final MBeanVo msg = this.mBeanService.messagesInPerSec(broker);
                        if (msg != null) {
                            sysKpi.setValue(CommonTool.numberic(null == sysKpi.getValue() ? 0D : sysKpi.getValue()) + CommonTool.numberic(msg.getOneMinute()));
                        }
                        break;
                    case KAFKA_BYTES_IN:
                        final MBeanVo bin = this.mBeanService.bytesInPerSec(broker);
                        if (bin != null) {
                            sysKpi.setValue(CommonTool.numberic(null == sysKpi.getValue() ? 0D : sysKpi.getValue()) + CommonTool.numberic(bin.getOneMinute()));
                        }
                        break;
                    case KAFKA_BYTES_OUT:
                        final MBeanVo bout = this.mBeanService.bytesOutPerSec(broker);
                        if (bout != null) {
                            sysKpi.setValue(CommonTool.numberic(null == sysKpi.getValue() ? 0D : sysKpi.getValue()) + CommonTool.numberic(bout.getOneMinute()));
                        }
                        break;
                    case KAFKA_BYTES_REJECTED:
                        final MBeanVo bytesRejectedPerSec = this.mBeanService.bytesRejectedPerSec(broker);
                        if (bytesRejectedPerSec != null) {
                            sysKpi.setValue(CommonTool.numberic(null == sysKpi.getValue() ? 0D : sysKpi.getValue()) + CommonTool.numberic(bytesRejectedPerSec.getOneMinute()));
                        }
                        break;
                    case KAFKA_FAILED_FETCH_REQUEST:
                        final MBeanVo failedFetchRequestsPerSec = this.mBeanService.failedFetchRequestsPerSec(broker);
                        if (failedFetchRequestsPerSec != null) {
                            sysKpi.setValue(CommonTool.numberic(null == sysKpi.getValue() ? 0D : sysKpi.getValue()) + CommonTool.numberic(failedFetchRequestsPerSec.getOneMinute()));
                        }
                        break;
                    case KAFKA_FAILED_PRODUCE_REQUEST:
                        final MBeanVo failedProduceRequestsPerSec = this.mBeanService.failedProduceRequestsPerSec(broker);
                        if (failedProduceRequestsPerSec != null) {
                            sysKpi.setValue(CommonTool.numberic(null == sysKpi.getValue() ? 0D : sysKpi.getValue()) + CommonTool.numberic(failedProduceRequestsPerSec.getOneMinute()));
                        }
                        break;
                    case KAFKA_TOTAL_FETCH_REQUESTS_PER_SEC:
                        final MBeanVo totalFetchRequests = this.mBeanService.totalFetchRequestsPerSec(broker);
                        if (totalFetchRequests != null) {
                            sysKpi.setValue(CommonTool.numberic(null == sysKpi.getValue() ? 0D : sysKpi.getValue()) + CommonTool.numberic(totalFetchRequests.getOneMinute()));
                        }
                        break;
                    case KAFKA_TOTAL_PRODUCE_REQUESTS_PER_SEC:
                        final MBeanVo totalProduceRequestsPerSec = this.mBeanService.totalProduceRequestsPerSec(broker);
                        if (totalProduceRequestsPerSec != null) {
                            sysKpi.setValue(CommonTool.numberic(null == sysKpi.getValue() ? 0D : sysKpi.getValue()) + CommonTool.numberic(totalProduceRequestsPerSec.getOneMinute()));
                        }
                        break;
                    case KAFKA_REPLICATION_BYTES_IN_PER_SEC:
                        final MBeanVo replicationBytesInPerSec = this.mBeanService.replicationBytesInPerSec(broker);
                        if (replicationBytesInPerSec != null) {
                            sysKpi.setValue(CommonTool.numberic(null == sysKpi.getValue() ? 0D : sysKpi.getValue()) + CommonTool.numberic(replicationBytesInPerSec.getOneMinute()));
                        }
                        break;
                    case KAFKA_REPLICATION_BYTES_OUT_PER_SEC:
                        final MBeanVo replicationBytesOutPerSec = this.mBeanService.replicationBytesOutPerSec(broker);
                        if (replicationBytesOutPerSec != null) {
                            sysKpi.setValue(CommonTool.numberic(null == sysKpi.getValue() ? 0D : sysKpi.getValue()) + CommonTool.numberic(replicationBytesOutPerSec.getOneMinute()));
                        }
                        break;
                    case KAFKA_PRODUCE_MESSAGE_CONVERSIONS:
                        final MBeanVo produceMessageConv = this.mBeanService.produceMessageConversionsPerSec(broker);
                        if (produceMessageConv != null) {
                            sysKpi.setValue(CommonTool.numberic(null == sysKpi.getValue() ? 0D : sysKpi.getValue()) + CommonTool.numberic(produceMessageConv.getOneMinute()));
                        }
                        break;
                    case KAFKA_OS_TOTAL_MEMORY:
                        final long totalMemory = this.mBeanService.getOsTotalMemory(broker);
                        sysKpi.setValue(CommonTool.numberic(null == sysKpi.getValue() ? 0D : sysKpi.getValue()) + totalMemory);
                        break;
                    case KAFKA_OS_FREE_MEMORY:
                        final long freeMemory = this.mBeanService.getOsFreeMemory(broker);
                        sysKpi.setValue(CommonTool.numberic(null == sysKpi.getValue() ? 0D : sysKpi.getValue()) + freeMemory);
                        break;
                    case KAFKA_SYSTEM_CPU_LOAD:
                    case KAFKA_PROCESS_CPU_LOAD:
                        final double systemCpuLoad = Double.parseDouble(kafkaJmxService.getData(broker, JMX.OPERATING_SYSTEM, kpi.getName()));
                        sysKpi.setValue(CommonTool.numberic(null == sysKpi.getValue() ? 0D : sysKpi.getValue()) + systemCpuLoad);
                        break;
                    case KAFKA_THREAD_COUNT:
                        final int threadCount = Integer.parseInt(kafkaJmxService.getData(broker, JMX.THREADING, kpi.getName()));
                        sysKpi.setValue(CommonTool.numberic(null == sysKpi.getValue() ? 0D : sysKpi.getValue()) + threadCount);
                        break;
                    default:
                        break;
                }
            }
            if (null == sysKpi.getValue()) {
                continue;
            }
            sysKpi.setHost(host.length() == 0 ? "unkowns" : host.substring(0, host.length() - 1));
            result.add(sysKpi);
        }

        final Optional<SysKpi> firstOsFree = result.stream().filter(p -> p.getKpi().equals(SysKpi.KAFKA_KPI.KAFKA_OS_FREE_MEMORY.getCode())).findFirst();
        final Optional<SysKpi> firstOsTotal = result.stream().filter(p -> p.getKpi().equals(SysKpi.KAFKA_KPI.KAFKA_OS_TOTAL_MEMORY.getCode())).findFirst();
        final Optional<SysKpi> firstSystemCpuLoad = result.stream().filter(p -> p.getKpi().equals(SysKpi.KAFKA_KPI.KAFKA_SYSTEM_CPU_LOAD.getCode())).findFirst();
        final Optional<SysKpi> firstProcessCpuLoad = result.stream().filter(p -> p.getKpi().equals(SysKpi.KAFKA_KPI.KAFKA_PROCESS_CPU_LOAD.getCode())).findFirst();
        final Optional<SysKpi> firstThreadCount = result.stream().filter(p -> p.getKpi().equals(SysKpi.KAFKA_KPI.KAFKA_THREAD_COUNT.getCode())).findFirst();

        if (firstOsFree.isPresent() && firstOsTotal.isPresent()) {
            final Double osFree = firstOsFree.get().getValue();
            final Double osTotal = firstOsTotal.get().getValue();
            final SysKpi sysKpi = new SysKpi();
            sysKpi.setKpi(SysKpi.KAFKA_KPI.KAFKA_OS_USED_MEMORY_PERCENTAGE.getCode());
            sysKpi.setCollectTime(now);
            sysKpi.setValue(CommonTool.numberic((1 - osFree / osTotal) * 100));
            sysKpi.setHost(firstOsFree.get().getHost());
            result.add(sysKpi);
        }

        if (firstSystemCpuLoad.isPresent()) {
            final SysKpi sysKpi = firstSystemCpuLoad.get();
            sysKpi.setValue((sysKpi.getValue() / brokers.size()) * 100);
        }
        if (firstProcessCpuLoad.isPresent()) {
            final SysKpi sysKpi = firstProcessCpuLoad.get();
            sysKpi.setValue((sysKpi.getValue() / brokers.size()) * 100);
        }
        if (firstThreadCount.isPresent()) {
            final SysKpi sysKpi = firstThreadCount.get();
            sysKpi.setValue((double) (sysKpi.getValue().intValue() / brokers.size()));
        }
        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final Properties properties = new Properties();
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, this.getBootstrapServers());
        this.kafkaAdminClient = (KafkaAdminClient) AdminClient.create(properties);
    }

    @Override
    public void destroy() {
        ResourceTool.closeQuietly(this.kafkaAdminClient);
    }

    private Map<TopicPartition, Long> listConsumerOffset(final String groupId,
                                                         final String topicName) throws Exception {
        final Map<TopicPartition, Long> result = new HashMap<>();
        final ListConsumerGroupOffsetsResult listConsumerGroupOffsetsResult = this.kafkaAdminClient.listConsumerGroupOffsets(groupId);
        for (final Map.Entry<TopicPartition, OffsetAndMetadata> entry : listConsumerGroupOffsetsResult.partitionsToOffsetAndMetadata().get().entrySet()) {
            if (topicName.equals(entry.getKey().topic())) {
                result.put(entry.getKey(), entry.getValue().offset());
            }
        }
        return result;
    }

    private String getConsumerId(final List<KafkaConsumerVo> kafkaConsumerVoList,
                                 final String topicName,
                                 final Integer partitionId) {
        if (null == kafkaConsumerVoList) {
            return null;
        }
        for (final KafkaConsumerVo kafkaConsumerVo : kafkaConsumerVoList) {
            for (final KafkaConsumerVo.Meta meta : kafkaConsumerVo.getMetaList()) {
                for (final KafkaConsumerVo.TopicSubscriber topicSubscriber : meta.getTopicSubscriberList()) {
                    if (topicSubscriber.getTopicName().equals(topicName) && topicSubscriber.getPartitionId().equals(partitionId)) {
                        return meta.getConsumerId();
                    }
                }
            }
        }
        return null;
    }

    private void assembleMBeanInfo(final Map<String, MBeanVo> mbeans,
                                   final String mBeanInfoKey,
                                   final MBeanVo mBeanVo) {
        if (mbeans.containsKey(mBeanInfoKey) && null != mBeanVo) {
            final DecimalFormat formatter = new DecimalFormat("###.##");
            final MBeanVo existedMBeanVo = mbeans.get(mBeanInfoKey);
            final String fifteenMinuteOld = existedMBeanVo.getFifteenMinute() == null ? "0.0" : existedMBeanVo.getFifteenMinute();
            final String fifteenMinuteLastest = mBeanVo.getFifteenMinute() == null ? "0.0" : mBeanVo.getFifteenMinute();
            final String fiveMinuteOld = existedMBeanVo.getFiveMinute() == null ? "0.0" : existedMBeanVo.getFiveMinute();
            final String fiveMinuteLastest = mBeanVo.getFiveMinute() == null ? "0.0" : mBeanVo.getFiveMinute();
            final String meanRateOld = existedMBeanVo.getMeanRate() == null ? "0.0" : existedMBeanVo.getMeanRate();
            final String meanRateLastest = mBeanVo.getMeanRate() == null ? "0.0" : mBeanVo.getMeanRate();
            final String oneMinuteOld = existedMBeanVo.getOneMinute() == null ? "0.0" : existedMBeanVo.getOneMinute();
            final String oneMinuteLastest = mBeanVo.getOneMinute() == null ? "0.0" : mBeanVo.getOneMinute();
            final double fifteenMinute = CommonTool.numberic(fifteenMinuteOld) + CommonTool.numberic(fifteenMinuteLastest);
            final double fiveMinute = CommonTool.numberic(fiveMinuteOld) + CommonTool.numberic(fiveMinuteLastest);
            final double meanRate = CommonTool.numberic(meanRateOld) + CommonTool.numberic(meanRateLastest);
            final double oneMinute = CommonTool.numberic(oneMinuteOld) + CommonTool.numberic(oneMinuteLastest);
            existedMBeanVo.setFifteenMinute(formatter.format(fifteenMinute));
            existedMBeanVo.setFiveMinute(formatter.format(fiveMinute));
            existedMBeanVo.setMeanRate(formatter.format(meanRate));
            existedMBeanVo.setOneMinute(formatter.format(oneMinute));
            existedMBeanVo.setName(mBeanInfoKey);
        } else {
            mbeans.put(mBeanInfoKey, mBeanVo);
        }
    }

    private void kafkaProducerDo(final KafkaProducerAction kafkaProducerAction) throws Exception {
        KafkaProducer<String, String> kafkaProducer = null;
        try {
            Properties props = new Properties();
            props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
            props.put(ProducerConfig.ACKS_CONFIG, "all");
            props.put(ProducerConfig.RETRIES_CONFIG, "3");
            props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, Constants.KAFKA_COMPRESS_TYPE);
            props.put(ProducerConfig.CLIENT_ID_CONFIG, String.format("%s_SEND_MSG", Constants.KAFKA_MONITOR_PEGASUS_SYSTEM_PREFIX));
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
            kafkaProducer = new KafkaProducer<>(props);
            kafkaProducerAction.action(kafkaProducer);
        } finally {
            ResourceTool.closeQuietly(kafkaProducer);
        }
    }

    private void kafkaConsumerDo(final KafkaConsumerAction kafkaConsumerAction) throws Exception {
        KafkaConsumer kafkaConsumer = null;
        try {
            Properties props = new Properties();
            props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
            props.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.KAFKA_MONITOR_SYSTEM_GROUP_NAME_FOR_MONITOR);
            props.put(ConsumerConfig.CLIENT_ID_CONFIG, String.format("%s_SEND_MSG", Constants.KAFKA_MONITOR_PEGASUS_SYSTEM_PREFIX));
            props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
            props.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
            props.setProperty(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
            props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
            kafkaConsumer = new KafkaConsumer<>(props);
            kafkaConsumerAction.action(kafkaConsumer);
        } finally {
            ResourceTool.closeQuietly(kafkaConsumer);
        }
    }

    private interface KafkaProducerAction {
        void action(final KafkaProducer kafkaProducer);
    }

    private interface KafkaConsumerAction {
        void action(final KafkaConsumer kafkaConsumer) throws Exception;
    }
}