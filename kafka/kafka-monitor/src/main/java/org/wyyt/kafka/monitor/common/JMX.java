package org.wyyt.kafka.monitor.common;

/**
 * Constants variable used for Kafka's JMX.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public class JMX {
    public static final String BYTES_IN_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec";
    public static final String BYTES_OUT_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec";
    public static final String BYTES_REJECTED_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=BytesRejectedPerSec";
    public static final String FAILED_FETCH_REQUESTS_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=FailedFetchRequestsPerSec";
    public static final String FAILED_PRODUCE_REQUESTS_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=FailedProduceRequestsPerSec";
    public static final String MESSAGES_IN_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec";
    public static final String PRODUCE_MESSAGE_CONVERSIONS_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=ProduceMessageConversionsPerSec";
    public static final String TOTAL_FETCH_REQUESTS_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=TotalFetchRequestsPerSec";
    public static final String TOTAL_PRODUCE_REQUESTS_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=TotalProduceRequestsPerSec";
    public static final String REPLICATION_BYTES_IN_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=ReplicationBytesInPerSec";
    public static final String REPLICATION_BYTES_OUT_PER_SEC = "kafka.server:type=BrokerTopicMetrics,name=ReplicationBytesOutPerSec";

    public static final String OPERATING_SYSTEM = "java.lang:type=OperatingSystem";
    public static final String THREADING = "java.lang:type=Threading";

    public static final String TOTAL_PHYSICAL_MEMORY_SIZE = "TotalPhysicalMemorySize";
    public static final String FREE_PHYSICAL_MEMORY_SIZE = "FreePhysicalMemorySize";
    public static final String SYSTEM_CPU_LOAD = "SystemCpuLoad";
    public static final String PROCESS_CPU_LOAD = "ProcessCpuLoad";

    public static final String THREAD_COUNT = "ThreadCount";

    public final static String MESSAGES_IN = "Messages in /sec";
    public final static String BYTES_IN = "Bytes in /sec";
    public final static String BYTES_OUT = "Bytes out /sec";
    public final static String BYTES_REJECTED = "Bytes rejected /sec";
    public final static String FAILED_FETCH_REQUEST = "Failed fetch request /sec";
    public final static String FAILED_PRODUCE_REQUEST = "Failed produce request /sec";
    public final static String TOTAL_FETCH_REQUESTS = "Total fetch requests /sec";
    public final static String TOTAL_PRODUCE_REQUESTS = "Total produce requests /sec";
    public final static String PRODUCE_MESSAGE_CONVERSIONS = "Produce message conversions /sec";
    public final static String OS_TOTAL_MEMORY = "os_total_memory";
    public final static String OS_FREE_MEMORY = "os_free_memory";

    public static final String ZK_PACKETS_SENT = "zk_packets_sent";
    public static final String ZK_PACKETS_RECEIVED = "zk_packets_received";
    public static final String ZK_NUM_ALIVE_CONNECTIONS = "zk_num_alive_connections";
    public static final String ZK_OUTSTANDING_REQUESTS = "zk_outstanding_requests";
}