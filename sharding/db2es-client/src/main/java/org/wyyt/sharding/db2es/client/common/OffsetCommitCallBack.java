package org.wyyt.sharding.db2es.client.common;

/**
 * the callback function of check-point committing.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public interface OffsetCommitCallBack {
    CheckpointExt commit(final CheckpointExt checkpoint);
}