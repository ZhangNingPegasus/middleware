package org.wyyt.db2es.client.common;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.kafka.common.TopicPartition;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.wyyt.db2es.client.entity.FlatMessge;
import org.wyyt.db2es.core.entity.domain.Checkpoint;
import org.wyyt.db2es.core.entity.domain.TableInfo;
import org.wyyt.tool.date.DateTool;

import java.util.Date;

/**
 * the entity of check point of Kafka
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@ToString(callSuper = true)
@Getter
@Slf4j
public final class CheckpointExt extends Checkpoint {
    public static final CheckpointExt INITIALIZED_CHECKPOINT = new CheckpointExt(null, -1, -1);

    public CheckpointExt(final TopicPartition topicPartition,
                         final long offset,
                         final long timestamp) {
        super(topicPartition, offset, timestamp);
    }

    public CheckpointExt(final Checkpoint checkpoint) {
        this(checkpoint.getTopicPartition(), checkpoint.getOffset(), checkpoint.getTimestamp());
    }

    public static CheckpointExt toCommitCheckPoint(final Context context,
                                                   final FlatMessge flatMessage) {
        return new CheckpointExt(new TopicPartition(flatMessage.getConsumerRecord().topic(),
                flatMessage.getConsumerRecord().partition()),
                flatMessage.getConsumerRecord().offset() + 1,  //+1表示接下来要消费但还没有消费的消息
                getTimestamp(context, flatMessage)
        );
    }

    public static long getTimestamp(final Context context,
                                    final FlatMessge flatMessage) {

        TableInfo tableInfo = context.getConfig().getTableMap().getByFactTableName(flatMessage.getTable());

        final String rowUpdateTimeField = tableInfo.getRowUpdateTimeFieldName();
        Long timestamp = null;

        if (null != flatMessage.getData()) {
            for (final CaseInsensitiveMap<String, String> datum : flatMessage.getData()) {
                final String rowUpdateTime = datum.get(rowUpdateTimeField);
                if (ObjectUtils.isEmpty(rowUpdateTime)) {
                    continue;
                }
                final long rowUpdateTimeLong = getTimestamp(rowUpdateTime);
                if (rowUpdateTimeLong > 0L &&
                        (
                                timestamp == null ||
                                        timestamp > rowUpdateTimeLong)) {
                    timestamp = rowUpdateTimeLong;
                }
            }
        }

        if (null == timestamp) {
            timestamp = flatMessage.getEs();
        }

        return timestamp;
    }

    private static long getTimestamp(final String datetime) {
        if (ObjectUtils.isEmpty(datetime)) {
            return 0L;
        }
        final Date date = DateTool.parse(datetime);
        Assert.notNull(date, "the parameter datetime required");
        return date.getTime();
    }
}