package org.wyyt.sharding.db2es.core.entity.domain;

/**
 * the domain entity of db2es' configuration item names
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public final class Names {
    /**
     * 分布式id, 具有同样的id为主备模式, 不同的id为分布式
     */
    public static final String DB2ES_ID = "db2es.id";
    /**
     * 用于HttpServer的主机
     */
    public static final String DB2ES_HOST = "db2es.host";
    /**
     * 用于HttpServer的端口
     */
    public static final String DB2ES_PORT = "db2es.port";
    /**
     * 当导入Elastic-Search失败时的后续动作，true表示将错误信息记录到数据库日志中并继续后续消费;false表示将持续消费当前失败的消息,直至成功为止
     */
    public static final String CONTINUE_ON_ERROR = "db2es.continueOnError";
    /**
     * db2es_admin的ip地址
     */
    public static final String DB2ES_ADMIN_HOST = "db2es_admin_host";
    /**
     * db2es_admin的端口
     */
    public static final String DB2ES_ADMIN_PORT = "db2es_admin_port";
    /**
     * 钉钉机器人的access_token
     */
    public static final String DING_ACCESS_TOKEN = "ding_access_token";
    /**
     * 钉钉机器人的加签秘钥
     */
    public static final String DING_SECRET = "ding_secret";
    /**
     * 钉钉机器人发送的对象(手机号), 如果有多个用逗号分隔, 如果为空, 则发送全体成员
     */
    public static final String DING_MOBILES = "ding_mobiles";
    /**
     * 指定某个具体topic的消费位点, 优先级高于db2es.checkpoint。格式:db2es.{topic_name}-{partition}.checkpoint
     */
    public static final String TOPIC_CHECKPOINT_FORMAT = "db2es.%s-%s.checkpoint";
    /**
     * 指定所有Topic的消费位点, 优先级低于db2es.{topic_name}-{partition}.checkpoint
     */
    public static final String INITIAL_CHECKPOINT = "db2es.checkpoint";
    /**
     * kafka集群所使用的zookeeper集群地址, 多个用逗号隔开
     */
    public static final String ZOOKEEPER_SERVERS = "zookeeper.servers";
    /**
     * 目标ElasticSearch的地址, 多个用逗号隔开
     */
    public static final String ELASTICSEARCH_HOSTNAMES = "elasticsearch.hostnames";
    /**
     * ElasticSearch的用户名
     */
    public static final String ELASTICSEARCH_USERNAME = "elasticsearch.username";
    /**
     * ElasticSearch的密码
     */
    public static final String ELASTICSEARCH_PASSWORD = "encrypt.elasticsearch.password";
    /**
     * 创建索时, 主分片的个数
     */
    public static final String ELASTICSEARCH_NUMBER_OF_SHARDS = "elasticsearch.numberOfShards";
    /**
     * 用于同步失败记录异常数据库的地址
     */
    public static final String DATABASE_HOST = "db.host";
    /**
     * 用于同步失败记录异常数据库的端口
     */
    public static final String DATABASE_PORT = "db.port";
    /**
     * 用于同步失败记录异常数据库的库名
     */
    public static final String DATABASE_NAME = "db.databaseName";
    /**
     * 用于同步失败记录异常数据库的用户名
     */
    public static final String DATABASE_USERNAME = "db.username";
    /**
     * 用于同步失败记录异常数据库的密码
     */
    public static final String DATABASE_PASSWORD = "encrypt.db.password";
    /**
     * ACM配置
     */
    public static final String ACM_DATA_ID = "acm.data.id";
    /**
     * ACM配置
     */
    public static final String ACM_GROUP_ID = "acm.group.id";
    /**
     * ACM配置
     */
    public static final String ACM_CONFIG_PATH = "acm.config.path";
    /**
     * ACM配置
     */
    public static final String ACM_NACOS_LOCAL_SNAPSHOT_PATH = "acm.nacos.local.snapshot.path";
    /**
     * ACM配置
     */
    public static final String ACM_NACOS_LOG_PATH = "acm.nacos.log.path";
    /**
     * ES索引主分片设置
     */
    public static final String NUMBER_OF_SHARDS = "index.number_of_shards";
    /**
     * ES索引副本分片设置
     */
    public static final String NUMBER_OF_REPLICAS = "index.number_of_replicas";
    /**
     * ES索引刷盘间隔时间设置
     */
    public static final String REFRESH_INTERVAL = "index.refresh_interval";

    /**
     * kafka集群地址
     */
    public static final String KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";

    /**
     * 接口调用签名
     */
    public static final String API_KEY = "pegasuszhangning";
    public static final String API_IV = "asusgeipganzgnhn";

    /**
     * DB2ES存储在zk中的路径
     */
    public static final String ZOOKEEPER_BROKER_IDS_PATH = "/brokers/ids";
}