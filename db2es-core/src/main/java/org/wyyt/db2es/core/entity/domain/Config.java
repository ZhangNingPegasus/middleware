package org.wyyt.db2es.core.entity.domain;

import lombok.Data;
import org.wyyt.db2es.core.entity.persistent.Topic;

import java.util.List;
import java.util.Map;

/**
 * the domain entity of configuration for db2es
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Data
public final class Config {
    /**
     * 分布式id, 具有同样的id为主备模式, 不同的id为分布式
     */
    private Integer db2EsId;
    /**
     * db2es-client的主机
     */
    private String db2EsHost;
    /**
     * db2es-client的端口
     */
    private Integer db2EsPort;
    /**
     * 当导入Elastic-Search失败时的后续动作，true表示将错误信息记录到数据库日志中并继续后续消费;false表示将持续消费当前失败的消息,直至成功为止
     */
    private Boolean continueOnError;
    /**
     * kafka集群所使用的zookeeper集群地址, 多个用逗号隔开
     */
    private String zkServers;
    /**
     * 目标ElasticSearch的地址, 多个用逗号隔开
     */
    private String esHost;
    /**
     * ElasticSearch的用户名
     */
    private String esUid;
    /**
     * ElasticSearch的密码
     */
    private String esPwd;
    /**
     * 用于同步失败记录异常数据库的地址
     */
    private String dbHost;
    /**
     * 用于同步失败记录异常数据库的端口
     */
    private Integer dbPort;
    /**
     * 用于同步失败记录异常数据库的库名
     */
    private String dbName;
    /**
     * 用于同步失败记录异常数据库的用户名
     */
    private String dbUid;
    /**
     * 用于同步失败记录异常数据库的密码
     */
    private String dbPwd;
    /**
     * 指定某个具体topic的消费位点, 优先级高于db2es.checkpoint。格式:db2es.{topic_name}-{partition}.checkpoint
     */
    private Map<String, String> topicCheckpointMap;
    /**
     * 指定所有Topic的消费位点, 优先级低于db2es.{topic_name}-{partition}.checkpoint
     */
    private String initialCheckpoint;
    /**
     * db2es_admin的ip地址
     */
    private String db2esAdminHost;
    /**
     * db2es_admin的端口
     */
    private Integer db2esAdminPort;
    /**
     * 钉钉机器人的access_token
     */
    private String dingAccessToken;
    /**
     * 钉钉机器人的加签秘钥
     */
    private String dingSecret;
    /**
     * 钉钉机器人发送的对象(手机号), 如果有多个用逗号分隔(例如:18XXXXXXXXX,18XXXXXXXXX), 如果为空, 则发送全体成员
     */
    private List<String> dingMobiles;
    /**
     * 该db2es负责消费的Topic. KEY: TopicName;  VALUE:Topic
     */
    private Map<String, Topic> topicMap;
    /**
     * ACM配置
     */
    private String acmDataId;
    /**
     * ACM配置
     */
    private String acmGroupId;
    /**
     * ACM配置
     */
    private String acmConfigPath;
    /**
     * ACM配置
     */
    private String acmNacosLocalSnapshotPath;
    /**
     * ACM配置
     */
    private String acmNacosLogPath;

    /**
     * 各个表的关键字段信息(如主键字段名等)
     */
    private TableMap tableMap;
}