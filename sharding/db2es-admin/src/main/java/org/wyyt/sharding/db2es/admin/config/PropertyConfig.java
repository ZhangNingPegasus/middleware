package org.wyyt.sharding.db2es.admin.config;

import com.sijibao.nacos.spring.util.NacosNativeUtils;
import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * the entity of configuration information in application.yml
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
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

    private final static String ZOOKEEPER_SERVERS = "zookeeper.servers";
    private final static String ELASTICSEARCH_HOSTNAMES = "elasticsearch.hostnames";
    private final static String ELASTICSEARCH_USERNAME = "elasticsearch.username";
    private final static String ELASTICSEARCH_PASSWORD = "encrypt.elasticsearch.password";
    private final static String DB_HOST = "db.host";
    private final static String DB_PORT = "db.port";
    private final static String DB_DATABASENAME = "db.databaseName";
    private final static String DB_USERNAME = "db.username";
    private final static String DB_PASSWORD = "encrypt.db.password";

    @Getter
    private String zkServers;
    @Getter
    private String esHost;
    @Getter
    private String esUid;
    @Getter
    private String esPwd;
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

    @Override
    public void afterPropertiesSet() throws Exception {
        NacosNativeUtils.loadAcmInfo(this.dataId, this.groupId, this.configPath, this.snapshotPath, this.logPath);
        final Properties acmProperties = NacosNativeUtils.getConfig();
        this.zkServers = acmProperties.getProperty(ZOOKEEPER_SERVERS, "");
        this.esHost = acmProperties.getProperty(ELASTICSEARCH_HOSTNAMES, "");
        this.esUid = acmProperties.getProperty(ELASTICSEARCH_USERNAME, "");
        this.esPwd = acmProperties.getProperty(ELASTICSEARCH_PASSWORD, "");
        this.dbHost = acmProperties.getProperty(DB_HOST, "");
        this.dbPort = acmProperties.getProperty(DB_PORT, "");
        this.dbName = acmProperties.getProperty(DB_DATABASENAME, "");
        this.dbUid = acmProperties.getProperty(DB_USERNAME, "");
        this.dbPwd = acmProperties.getProperty(DB_PASSWORD, "");
    }
}