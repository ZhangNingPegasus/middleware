package org.wyyt.kafka.monitor.common;

import org.wyyt.kafka.monitor.util.SecurityUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Constant variable.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
public class Constants {
    public static final String DEFAULT_ADMIN_USER_NAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = SecurityUtil.hash("admin");
    public static final String SYSTEM_ROLE_NAME = "超级管理员";
    public static final Integer MAX_PAGE_NUM = 10000;

    public static final String CURRENT_ADMIN_LOGIN = "CURRENT_ADMIN_LOGIN";

    public static final String ZK_BROKER_IDS_PATH = "/brokers/ids";
    public static final String ZK_BROKERS_TOPICS_PATH = "/brokers/topics";

    public static final String KAFKA_MONITOR_PEGASUS_SYSTEM_PREFIX = "kafka_monitor_system_for_";
    public static final String KAFKA_MONITOR_SYSTEM_GROUP_NAME_FOR_MONITOR = KAFKA_MONITOR_PEGASUS_SYSTEM_PREFIX + "monitor";
    public static final String KAFKA_MONITOR_SYSTEM_GROUP_NAME_FOR_MESSAGE = KAFKA_MONITOR_PEGASUS_SYSTEM_PREFIX + "message";
    public static final String KAFKA_COMPRESS_TYPE = "lz4";

    public static final Integer CAPACITY = 2048;
    public static final int INIT_TOPIC_COUNT = 128;

    public static final String HOST_NOT_AVAIABLE = "主机不可用，请检查";

    public static final String SESSION_KAFKA_CONSUMER_INFO = "SESSION_KAFKA_CONSUMER_INFO";

    public final static List<String> KAFKA_SYSTEM_TOPIC = Arrays.asList("__consumer_offsets", "__transaction_state");
}