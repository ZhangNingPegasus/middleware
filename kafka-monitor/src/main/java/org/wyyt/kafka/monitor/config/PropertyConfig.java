package org.wyyt.kafka.monitor.config;

import com.sijibao.nacos.spring.util.NacosNativeUtils;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * the entity of configuration information in application.yml
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Configuration
public class PropertyConfig implements InitializingBean {
    @Value("${acm.data-id}")
    private String dataId;

    @Value("${acm.group}")
    private String groupId;

    @Value("${acm.acmConfigPath}")
    private String configPath;

    @Value("${acm.nacosLocalSnapshotPath}")
    private String snapshotPath;

    @Value("${acm.nacosLogPath}")
    private String logPath;

    @Getter
    private Integer port;
    @Getter
    private String zkServers;
    @Getter
    private String dbHost;
    @Getter
    private String dbPort;
    @Getter
    private String dbName;
    @Getter
    private String dbUid;
    @Getter
    private String dbPwd;
    @Getter
    private Integer retentionDays;
    @Getter
    private Set<String> blackListTopicSet;

    private final static String PORT = "port";
    private final static String ZOOKEEPER_SERVERS = "zk.connect";
    private final static String DB_HOST = "db.host";
    private final static String DB_PORT = "db.port";
    private final static String DB_NAME = "db.name";
    private final static String DB_USERNAME = "db.username";
    private final static String DB_PASSWORD = "encrypt.db.password";
    private final static String DB_RETENTION_DAYS = "db.retention.days";
    private final static String TOPIC_BLACKLIST = "topic.blacklist";

    @Override
    public void afterPropertiesSet() throws Exception {
        NacosNativeUtils.loadAcmInfo(this.dataId, this.groupId, this.configPath, this.snapshotPath, this.logPath);
        final Properties acmProperties = NacosNativeUtils.getConfig();
        this.port = Integer.parseInt(acmProperties.getProperty(PORT, "9999"));
        this.zkServers = acmProperties.getProperty(ZOOKEEPER_SERVERS, "");
        this.dbHost = acmProperties.getProperty(DB_HOST, "");
        this.dbPort = acmProperties.getProperty(DB_PORT, "3306");
        this.dbName = acmProperties.getProperty(DB_NAME, "kafka_monitor");
        this.dbUid = acmProperties.getProperty(DB_USERNAME, "");
        this.dbPwd = acmProperties.getProperty(DB_PASSWORD, "");
        this.retentionDays = Integer.parseInt(acmProperties.getProperty(DB_RETENTION_DAYS, "3"));
        this.blackListTopicSet = new HashSet<>();
        String blackListTopics = acmProperties.getProperty(TOPIC_BLACKLIST, "");
        for (String topicName : blackListTopics.split(",")) {
            if (StringUtils.isEmpty(topicName) || StringUtils.isEmpty(topicName.trim())) {
                continue;
            }
            this.blackListTopicSet.add(topicName.trim());
        }

        this.dbName = this.dbName + "_temp";
    }
}