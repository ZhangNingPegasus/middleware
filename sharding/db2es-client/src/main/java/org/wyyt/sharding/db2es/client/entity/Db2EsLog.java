package org.wyyt.sharding.db2es.client.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.wyyt.sharding.db2es.client.common.Context;
import org.wyyt.sharding.db2es.core.entity.domain.TableInfo;

/**
 * the entity used for logging error inforamtion.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
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
                TableInfo tableInfo = context.getConfig().getTableMap().getByFactTableName(this.tableName);
                pkName = tableInfo.getPrimaryKeyFieldName();
            }
            this.primaryKeyValue = flatMessage.getData().get(0).get(pkName);
        }
    }
}