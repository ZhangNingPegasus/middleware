package org.wyyt.db2es.client.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.wyyt.db2es.client.common.Context;

/**
 * the entity used for logging error inforamtion.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@ToString
public final class Db2EsLog {
    @Getter
    @Setter
    private String indexName;
    @Getter
    @Setter
    private String errorMessage;
    @Getter
    private final String databaseName;
    @Getter
    private final String tableName;
    @Getter
    private String primaryKeyValue;
    @Getter
    private final String consumerRecord;
    @Getter
    private final String topicName;
    @Getter
    private final Integer partition;
    @Getter
    private final Long offset;

    public Db2EsLog(final Context context,
                    final FlatMessge flatMessage) {
        this.consumerRecord = flatMessage.getConsumerRecord().value();
        this.topicName = flatMessage.getConsumerRecord().topic();
        this.partition = flatMessage.getConsumerRecord().partition();
        this.offset = flatMessage.getConsumerRecord().offset();

        this.databaseName = flatMessage.getDatabase();
        this.tableName = flatMessage.getTable();

        if (null == flatMessage.getData()) {
            this.primaryKeyValue = "";
        } else if (!flatMessage.getData().isEmpty()) {
            String pkName;
            if (null != flatMessage.getPkNames() && !flatMessage.getPkNames().isEmpty()) {
                pkName = flatMessage.getPkNames().iterator().next();
            } else {
                pkName = context.getConfig().getPrimaryKey();
            }
            this.primaryKeyValue = flatMessage.getData().get(0).get(pkName);
        }
    }
}