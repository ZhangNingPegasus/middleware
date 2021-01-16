package org.wyyt.sharding.db2es.core.entity.domain;

/**
 * the common constant of db2es
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
public final class Common {
    public static final String ZK_ROOT_PATH = "/db2es";
    public static final String LEADER = "leader";
    public static final String ID = "id_";

    public static final String ZK_LEADER_PATH = ZK_ROOT_PATH.concat("/").concat(LEADER);
    public static final String ZK_ID_PATH = ZK_LEADER_PATH.concat("/").concat(ID);
}