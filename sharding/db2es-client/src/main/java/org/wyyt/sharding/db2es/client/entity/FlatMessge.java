package org.wyyt.sharding.db2es.client.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.wyyt.sharding.db2es.client.common.CheckpointExt;
import org.wyyt.sharding.db2es.client.common.Context;
import org.wyyt.sharding.db2es.client.common.UserCommitCallBack;
import org.wyyt.sharding.db2es.core.entity.domain.FlatMsg;

/**
 * the entity of Canal records which send to Kafka
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@ToString
@EqualsAndHashCode(callSuper = false)
@Data
public final class FlatMessge extends FlatMsg {
    /**
     * 用户自定义位点提交的回调接口
     */
    private UserCommitCallBack userCommitCallBack;

    public final void commit(final Context context) {
        this.userCommitCallBack.commit(CheckpointExt.toCommitCheckPoint(context, this));
    }
}