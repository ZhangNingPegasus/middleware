package org.wyyt.sharding.db2es.client.common;

/**
 * the user-defined function used for how to commit check-point
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
public interface UserCommitCallBack {
    void commit(final CheckpointExt checkpoint);
}