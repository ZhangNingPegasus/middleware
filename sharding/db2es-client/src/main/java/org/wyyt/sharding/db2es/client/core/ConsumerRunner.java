package org.wyyt.sharding.db2es.client.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.wyyt.sharding.db2es.client.common.*;
import org.wyyt.sharding.db2es.client.entity.FlatMessge;
import org.wyyt.sharding.db2es.client.entity.Processor;
import org.wyyt.sharding.db2es.client.thread.WorkerThread;
import org.wyyt.sharding.db2es.core.util.flatmsg.FlatMsgUtils;
import org.wyyt.tool.exception.ExceptionTool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * the thread used for populating the kafka recrods into Elastic-Search
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class ConsumerRunner extends BaseRunner {
    private final WorkerThread commitThread;
    private final LinkedBlockingQueue<ConsumerRecord> toProcessRecords;
    private final RecordListener recordListener;
    private final OffsetCommitCallBack offsetCommitCallBack;
    private final RecordRunner recordRunner;
    private volatile CheckpointExt commitCheckpoint;

    public ConsumerRunner(final Context context,
                          final RecordRunner recordRunner,
                          final OffsetCommitCallBack offsetCommitCallBack,
                          final RecordListener recordListener) {
        super(context);
        this.recordRunner = recordRunner;
        this.offsetCommitCallBack = offsetCommitCallBack;
        this.recordListener = recordListener;
        this.toProcessRecords = new LinkedBlockingQueue<>(Constant.CAPACITY * 3);
        this.commitThread = getCommitThread();
        this.commitThread.start();
    }

    @Override
    public final void run() {
        final List<ConsumerRecord> toProcessList = new ArrayList<>(Constant.CAPACITY);
        final List<FlatMessge> flatMessageList = new ArrayList<>(Constant.CAPACITY);
        final Processor processor = this.context.getProcessorWrapper().getByTopicPartition(this.recordRunner.getTopicPartition());
        long start, end;
        while (!this.terminated) {
            try {
                toProcessList.clear();
                flatMessageList.clear();
                this.toProcessRecords.drainTo(toProcessList, Constant.CAPACITY);
                while (!this.terminated && toProcessList.isEmpty()) {
                    processor.setTps(0);
                    sleepInterval(500);
                    this.toProcessRecords.drainTo(toProcessList, Constant.CAPACITY);
                }
                for (final ConsumerRecord consumerRecord : toProcessList) {
                    flatMessageList.add(toFlatMessage(consumerRecord));
                }

                if (this.terminated) {
                    return;
                }

                if (null != this.recordListener) {
                    try {
                        start = System.currentTimeMillis();
                        final int count = this.recordListener.consume(flatMessageList);
                        end = System.currentTimeMillis();

                        final double seconds = ((end - start) / 1000.0);
                        if (seconds > 0) {
                            processor.setTps((int) (count / seconds));
                        } else {
                            processor.setTps(0);
                        }
                    } catch (final Exception exception) {
                        log.error(String.format("ConsumerRunner: process record failed for topic [%s] caused by, %s",
                                this.recordRunner.getTopicPartition(),
                                ExceptionTool.getRootCauseMessage(exception)), exception);
                        this.recordRunner.setConsumeException(exception);
                        if (!this.context.getConfig().getContinueOnError()) {
                            this.clearQueue();
                        }
                    }
                }
            } catch (final Exception exception) {
                log.error(String.format("ConsumerRunner: topic[%s] meet an error cause, %s",
                        this.recordRunner.getTopicPartition(),
                        ExceptionTool.getRootCauseMessage(exception)), exception);
                this.recordRunner.setConsumeException(exception);
                if (!this.context.getConfig().getContinueOnError()) {
                    this.clearQueue();
                }
            }
        }
        this.clearQueue();
        toProcessList.clear();
        flatMessageList.clear();
    }

    public final boolean offer(final ConsumerRecord record,
                               final long timeOut,
                               final TimeUnit timeUnit) {
        try {
            return this.toProcessRecords.offer(record, timeOut, timeUnit);
        } catch (final Exception exception) {
            log.error(String.format("ConsumerRunner: offser record failed, record [%s], cause %s",
                    record,
                    ExceptionTool.getRootCauseMessage(exception)), exception);
            return false;
        }
    }

    public final void clearQueue() {
        this.toProcessRecords.clear();
    }

    public final RecordRunner getRecordRunner() {
        return this.recordRunner;
    }

    @Override
    public final void close() {
        this.terminated = true;
        this.commitThread.stop();
    }

    private WorkerThread getCommitThread() {
        return new WorkerThread(() -> {
            while (!this.terminated) {
                sleepInterval(Constant.COMMIT_INTERVAL_MS);
                commit();
            }
        }, String.format("thread-ack-commit-for-topic-%s", this.recordRunner.getTopicPartition()));
    }

    private void commit() {
        if (null != this.offsetCommitCallBack &&
                null != this.commitCheckpoint &&
                null != this.commitCheckpoint.getTopicPartition() &&
                -1 != this.commitCheckpoint.getOffset()) {
            this.offsetCommitCallBack.commit(this.commitCheckpoint);
            this.commitCheckpoint = null;
        }
    }

    private FlatMessge toFlatMessage(final ConsumerRecord<String, String> consumerRecord) throws Exception {
        final FlatMessge result = FlatMsgUtils.toFlatMsg(consumerRecord, FlatMessge.class);
        result.setUserCommitCallBack((checkpoint) -> this.commitCheckpoint = checkpoint);
        return result;
    }
}