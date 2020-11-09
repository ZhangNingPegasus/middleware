package org.wyyt.kafka.monitor.service.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.wyyt.kafka.monitor.anno.TranRead;
import org.wyyt.kafka.monitor.anno.TranSave;
import org.wyyt.kafka.monitor.common.Constants;
import org.wyyt.kafka.monitor.config.PropertyConfig;
import org.wyyt.kafka.monitor.entity.dto.SysTableName;
import org.wyyt.kafka.monitor.entity.dto.TopicRecord;
import org.wyyt.kafka.monitor.entity.po.MaxOffset;
import org.wyyt.kafka.monitor.entity.vo.RecordVo;
import org.wyyt.kafka.monitor.entity.vo.TopicVo;
import org.wyyt.kafka.monitor.mapper.TopicRecordMapper;
import org.wyyt.kafka.monitor.service.common.KafkaService;
import org.wyyt.kafka.monitor.service.core.ProcessorService;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.resource.ResourceTool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * The service for dynamic table. Saving topics' records.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Slf4j
@Service
public class TopicRecordService extends ServiceImpl<TopicRecordMapper, TopicRecord> implements DisposableBean {
    public static final String TABLE_PREFIX = "t_";
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private final PropertyConfig propertyConfig;
    private final SysTableNameService sysTableNameService;
    private final ExecutorService executorService;
    private final DataSource dataSource;
    private final KafkaService kafkaService;
    private final SysTopicSizeService sysTopicSizeService;
    private final SysTopicLagService sysTopicLagService;
    private final ProcessorService processorService;
    private final SysAlertTopicService sysAlertTopicService;
    private final SysAlertConsumerService sysAlertConsumerService;

    public TopicRecordService(final PropertyConfig propertyConfig,
                              final SysTableNameService sysTableNameService,
                              final DataSource dataSource,
                              final KafkaService kafkaService,
                              final SysTopicSizeService sysTopicSizeService,
                              final SysTopicLagService sysTopicLagService,
                              final ProcessorService processorService,
                              final SysAlertTopicService sysAlertTopicService,
                              final SysAlertConsumerService sysAlertConsumerService) {
        this.propertyConfig = propertyConfig;
        this.sysTableNameService = sysTableNameService;
        this.dataSource = dataSource;
        this.kafkaService = kafkaService;
        this.sysTopicSizeService = sysTopicSizeService;
        this.sysTopicLagService = sysTopicLagService;
        this.processorService = processorService;
        this.sysAlertTopicService = sysAlertTopicService;
        this.sysAlertConsumerService = sysAlertConsumerService;
        this.executorService = new ThreadPoolExecutor(CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                10L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(Constants.CAPACITY),
                new ThreadFactoryBuilder()
                        .setNameFormat("pool-thread-save-records-%d")
                        .setDaemon(false)
                        .setUncaughtExceptionHandler((thread, exception) -> log.error(ExceptionTool.getRootCauseMessage(exception), exception))
                        .build());
    }

    @TranRead
    public void listRecords(IPage<RecordVo> page, String topicName, Integer partitionId, Long offset, String key, Date from, Date to) {
        if (StringUtils.isEmpty(topicName)) {
            return;
        }
        final SysTableName sysTableName = this.sysTableNameService.getByTopicName(topicName.trim());
        if (null == sysTableName) {
            return;
        }

        final List<TopicRecord> topicRecordList = this.baseMapper.listRecords(page, sysTableName.getRecordTableName(), partitionId, offset, key, from, to);
        final List<RecordVo> result = new ArrayList<>(topicRecordList.size());
        for (final TopicRecord topicRecord : topicRecordList) {
            result.add(topicRecord.toVo());
        }
        page.setRecords(result);
    }

    @TranRead
    public String getRecordDetailValue(final String topicName,
                                       final Integer partitionId,
                                       final Long offset) {
        if (StringUtils.isEmpty(topicName)) {
            return null;
        }
        final SysTableName sysTableName = this.sysTableNameService.getByTopicName(topicName.trim());
        if (null == sysTableName) {
            return null;
        }
        try {
            return this.baseMapper.getRecordDetailValue(sysTableName.getRecordDetailTableName(), partitionId, offset);
        } catch (final Exception e) {
            log.error(ExceptionTool.getRootCauseMessage(e), e);
            return null;
        }
    }

    @TranRead
    public TopicVo listTopicDetailLogSize(final String topicName) {
        final TopicVo result = new TopicVo();
        result.setTopicName(topicName);
        try {
            result.setLogSize(this.kafkaService.getTopicLogSize(topicName));
        } catch (final Exception exception) {
            result.setError(ExceptionTool.getRootCauseMessage(exception));
        }
        result.setDay0LogSize(this.sysTopicSizeService.getHistoryLogSize(topicName, 0)); //今天
        result.setDay1LogSize(this.sysTopicSizeService.getHistoryLogSize(topicName, 1)); //昨天
        result.setDay2LogSize(this.sysTopicSizeService.getHistoryLogSize(topicName, 2)); //前天
        result.setDay3LogSize(this.sysTopicSizeService.getHistoryLogSize(topicName, 3)); //前3天
        result.setDay4LogSize(this.sysTopicSizeService.getHistoryLogSize(topicName, 4)); //前4天
        result.setDay5LogSize(this.sysTopicSizeService.getHistoryLogSize(topicName, 5)); //前5天
        result.setDay6LogSize(this.sysTopicSizeService.getHistoryLogSize(topicName, 6)); //前6天

        return result;
    }

    @TranRead
    public MaxOffset listMaxOffset(final TopicPartition topicPartition) {
        final SysTableName sysTableName = this.sysTableNameService.getByTopicName(topicPartition.topic());
        if (null == sysTableName) {
            return null;
        }
        final List<MaxOffset> maxOffsets = this.baseMapper.listMaxOffset(sysTableName.getRecordTableName(), topicPartition.partition());
        if (null == maxOffsets || maxOffsets.isEmpty()) {
            return null;
        }
        return maxOffsets.get(0);
    }

    public Map<String, SysTableName> initTopicTable(final Set<String> topicNameSet) {
        final Set<String> topicNames = filterExistedTopicTable(topicNameSet);
        final List<SysTableName> sysTableNameList = insertTableNames(topicNames);
        if (!sysTableNameList.isEmpty()) {
            this.baseMapper.createTableIfNotExists(sysTableNameList);
        }
        return this.sysTableNameService.listMap();
    }

    @TranRead
    public Set<String> filterExistedTopicTable(final Set<String> topicNameSet) {
        Set<String> result = new HashSet<>(topicNameSet.size());
        for (String topicName : topicNameSet) {
            final SysTableName sysTableName = this.sysTableNameService.getByTopicName(topicName);
            if (null == sysTableName) {
                result.add(topicName);
            }
        }
        return result;
    }

    @TranSave
    public List<SysTableName> insertTableNames(Set<String> topicNameSet) {
        List<SysTableName> result = new ArrayList<>(topicNameSet.size());
        for (String topicName : topicNameSet) {
            boolean existed = true;
            String randomStr = RandomStringUtils.randomAlphanumeric(10);
            String recordTableName = String.format("t_%s_0", randomStr);
            String recordDetailTableName = String.format("t_%s_1", randomStr);

            while (existed) {
                randomStr = RandomStringUtils.randomAlphanumeric(10);
                recordTableName = String.format("t_%s_0", randomStr);
                recordDetailTableName = String.format("t_%s_1", randomStr);
                final SysTableName sysTableName = this.sysTableNameService.getByTableName(recordTableName, recordDetailTableName);
                existed = (sysTableName != null);
            }

            result.add(this.sysTableNameService.insert(topicName, recordTableName, recordDetailTableName));
        }
        return result;
    }

    @TranSave
    public void deleteExpired(Set<String> topicNames) {
        if (null == topicNames || topicNames.isEmpty()) {
            return;
        }

        Date now = new Date();
        Date date = DateUtils.addDays(now, -this.propertyConfig.getRetentionDays());

        for (String topicName : topicNames) {
            final SysTableName sysTableName = this.sysTableNameService.getByTopicName(topicName);
            if (null == sysTableName) {
                continue;
            }
            this.baseMapper.deleteExpired(sysTableName.getRecordTableName(), sysTableName.getRecordDetailTableName(), date);
        }
    }

    @TranSave
    public void dropTable(final String recordTableName,
                          final String recordDetailTableName) {
        this.baseMapper.dropTable(recordTableName, recordDetailTableName);
    }

    @TranSave
    public void truncateTable(final String recordTableName,
                              final String recordDetailTableName) {
        this.baseMapper.truncateTable(recordTableName, recordDetailTableName);
    }

    public void saveRecords(final Map<String, List<TopicRecord>> topicRecordMap,
                            final Map<String, SysTableName> sysTableNameMap) throws Exception {
        if (null == topicRecordMap || topicRecordMap.isEmpty()) {
            return;
        }
        final CountDownLatch countDownLatch = new CountDownLatch(topicRecordMap.size());
        for (final Map.Entry<String, List<TopicRecord>> pair : topicRecordMap.entrySet()) {
            final SysTableName sysTableName = sysTableNameMap.get(pair.getKey());
            if (null == sysTableName) {
                log.error(String.format("主题[%s]没有找到对应的物理表", pair.getKey()));
                continue;
            }
            this.executorService.submit(() -> {
                Connection connection = null;
                try {
                    connection = dataSource.getConnection();
                    connection.setAutoCommit(false);
                    try (final PreparedStatement preparedStatement = connection.prepareStatement(String.format("INSERT IGNORE INTO `%s`(`partition_id`,`offset`,`key`,`value`,`timestamp`) VALUES(?,?,?,?,?)", sysTableName.getRecordTableName()))) {
                        for (final TopicRecord topicRecord : pair.getValue()) {

                            String value = topicRecord.getValue();
                            if (value.length() > 125) {
                                value = value.substring(0, 125).concat("...");
                            }

                            preparedStatement.setInt(1, topicRecord.getPartitionId());
                            preparedStatement.setLong(2, topicRecord.getOffset());
                            preparedStatement.setString(3, topicRecord.getKey());
                            preparedStatement.setString(4, value);
                            preparedStatement.setTimestamp(5, new java.sql.Timestamp(topicRecord.getTimestamp().getTime()));
                            preparedStatement.addBatch();
                        }
                        preparedStatement.executeBatch();
                        connection.commit();
                    }

                    try (final PreparedStatement preparedStatement = connection.prepareStatement(String.format("INSERT IGNORE INTO `%s`(`partition_id`,`offset`,`value`) VALUES(?,?,?)", sysTableName.getRecordDetailTableName()))) {
                        for (final TopicRecord topicRecord : pair.getValue()) {
                            preparedStatement.setInt(1, topicRecord.getPartitionId());
                            preparedStatement.setLong(2, topicRecord.getOffset());
                            preparedStatement.setString(3, topicRecord.getValue());
                            preparedStatement.addBatch();
                        }
                        preparedStatement.executeBatch();
                        connection.commit();
                    }
                    connection.setAutoCommit(true);
                } catch (final Exception exception) {
                    log.error(ExceptionTool.getRootCauseMessage(exception), exception);
                } finally {
                    ResourceTool.closeQuietly(connection);
                    countDownLatch.countDown();
                }
            });

        }
        countDownLatch.await();
    }

    @Override
    public void destroy() {
        this.executorService.shutdownNow();
    }

    @TranSave
    public void deleteTopic(final List<String> topicNameList) throws Exception {
        final Set<SysTableName> sysTableNameSet = new HashSet<>(topicNameList.size());
        for (String topicName : topicNameList) {
            if (StringUtils.isEmpty(topicName.trim())) {
                continue;
            }
            final SysTableName sysTableName = this.sysTableNameService.getByTopicName(topicName);
            if (null == sysTableName) {
                continue;
            }
            sysTableNameSet.add(sysTableName);
        }

        if (!sysTableNameSet.isEmpty()) {
            this.processorService.stop();
            this.kafkaService.deleteTopic(sysTableNameSet.stream().map(SysTableName::getTopicName).collect(Collectors.toList()));
            for (final SysTableName sysTableName : sysTableNameSet) {
                this.sysTableNameService.deleteTopic(sysTableName.getTopicName());
                this.sysTopicSizeService.deleteTopic(sysTableName.getTopicName());
                this.sysTopicLagService.deleteTopic(sysTableName.getTopicName());
                this.sysAlertTopicService.deleteTopic(sysTableName.getTopicName());
                this.dropTable(sysTableName.getRecordTableName(), sysTableName.getRecordDetailTableName());
            }
            this.processorService.start();
        }
    }

    @TranSave
    public void deleteConsumer(final List<String> groupdIdList) throws Exception {
        final Set<String> groupdIdSet = new HashSet<>();
        for (final String groupId : groupdIdList) {
            if (StringUtils.isEmpty(groupId)) {
                continue;
            }
            groupdIdSet.add(groupId.trim());
        }
        this.kafkaService.deleteConsumer(new ArrayList<>(groupdIdSet));
        for (final String groupId : groupdIdSet) {
            this.sysTopicLagService.deleteConsumer(groupId);
            this.sysAlertConsumerService.deleteConsumer(groupId);
        }
    }
}