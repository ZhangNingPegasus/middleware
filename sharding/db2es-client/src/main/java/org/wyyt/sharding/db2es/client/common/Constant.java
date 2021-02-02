package org.wyyt.sharding.db2es.client.common;

/**
 * the constant variables
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public class Constant {
    public static final String PROPERTIES_FILE_NAME = "db2es.properties";
    public static final int CAPACITY = (int) (2048 * 1.5);
    public static final long COMMIT_INTERVAL_MS = 5000;
}