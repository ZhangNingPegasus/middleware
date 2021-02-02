package org.wyyt.kafka.monitor.common;

import java.util.Arrays;
import java.util.List;

/**
 * Constant variable.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public class Constants {
    public static final List<String> IGNORE_TABLE = Arrays.asList("sys_admin", "sys_alert_cluster", "sys_alert_consumer", "sys_alert_topic", "sys_dingding_config", "sys_mail_config", "sys_page", "sys_permission", "sys_role", "sys_table_name");

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String MAX_VALUE = "MAXVALUE";
    public static final String MAX_VALUE_PARTITION_NAME = "p_max";

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