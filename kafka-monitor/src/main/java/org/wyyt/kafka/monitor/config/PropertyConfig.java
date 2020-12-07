package org.wyyt.kafka.monitor.config;

import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

import java.util.HashSet;
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
    @Getter
    @Value("${zookeeper.servers}")
    private String zkServers;

    @Getter
    @Value("${db.host}")
    private String dbHost;

    @Getter
    @Value("${db.port}")
    private String dbPort;

    @Getter
    @Value("${db.name}")
    private String dbName;

    @Getter
    @Value("${db.username}")
    private String dbUid;

    @Getter
    @Value("${db.password}")
    private String dbPwd;

    @Getter
    @Value("${retention.days}")
    private Integer retentionDays;

    @Getter
    @Value("${topic.blacklist}")
    private String topicBlacklist;

    @Getter
    private Set<String> topicBlacklistSet;

    @Override
    public void afterPropertiesSet() {
        this.topicBlacklistSet = new HashSet<>();
        if (ObjectUtils.isEmpty(this.topicBlacklist)) {
            return;
        }

        for (final String topicName : this.topicBlacklist.split(",")) {
            if (ObjectUtils.isEmpty(topicName) || ObjectUtils.isEmpty(topicName.trim())) {
                continue;
            }
            this.topicBlacklistSet.add(topicName.trim());
        }
    }
}