package org.wyyt.kafka.monitor.service.core;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.wyyt.kafka.monitor.common.Constants;
import org.wyyt.kafka.monitor.entity.dto.TopicRecord;
import org.wyyt.kafka.monitor.service.dto.TopicRecordService;
import org.wyyt.tool.exception.ExceptionTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * the thread used for populating the kafka recrods into Elastic-Search
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class ConsumerRunner extends BaseRunner {
    @Setter
    private RecordRunner recordRunner;
    private final TopicRecordService topicRecordService;
    private final LinkedBlockingQueue<TopicRecord> toProcessRecords;

    public ConsumerRunner(final TopicRecordService topicRecordService) {
        this.topicRecordService = topicRecordService;
        this.toProcessRecords = new LinkedBlockingQueue<>(Constants.CAPACITY * 3);
    }

    @Override
    public final void run() {
        final List<TopicRecord> toProcessList = new ArrayList<>(Constants.CAPACITY);
        final Map<String, List<TopicRecord>> topicRecordMap = new HashMap<>(Constants.CAPACITY / 10);
        while (this.continued()) {
            try {
                toProcessList.clear();
                topicRecordMap.clear();
                this.toProcessRecords.drainTo(toProcessList, Constants.CAPACITY);
                while (this.continued() && toProcessList.isEmpty()) {
                    sleepInterval(500);
                    this.toProcessRecords.drainTo(toProcessList, Constants.CAPACITY);
                }

                for (final TopicRecord topicRecord : toProcessList) {
                    String key = topicRecord.getTopicName();
                    if (topicRecordMap.containsKey(key)) {
                        topicRecordMap.get(key).add(topicRecord);
                    } else {
                        int count = (int) toProcessList.stream().filter(p -> p.getTopicName().equals(key)).count();
                        List<TopicRecord> topicRecordList = new ArrayList<>(count);
                        topicRecordList.add(topicRecord);
                        topicRecordMap.put(topicRecord.getTopicName(), topicRecordList);
                    }
                }
                this.topicRecordService.saveRecords(topicRecordMap, this.recordRunner.getSysTableNameMap());
            } catch (final Exception exception) {
                log.error(ExceptionTool.getRootCauseMessage(exception), exception);
            }
        }
        this.toProcessRecords.clear();
        toProcessList.clear();
    }

    public final boolean offer(final TopicRecord topicRecord,
                               final long timeOut,
                               final TimeUnit timeUnit) {
        try {
            return this.toProcessRecords.offer(topicRecord, timeOut, timeUnit);
        } catch (final Exception exception) {
            log.error(ExceptionTool.getRootCauseMessage(exception), exception);
            return false;
        }
    }
}