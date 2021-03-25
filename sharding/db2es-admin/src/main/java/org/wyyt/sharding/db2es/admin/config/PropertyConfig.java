package org.wyyt.sharding.db2es.admin.config;

import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.wyyt.apollo.tool.ApolloReader;
import org.wyyt.sharding.db2es.core.entity.domain.Names;

import java.util.Map;

/**
 * the entity of configuration information in application.yml
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Configuration
public class PropertyConfig implements InitializingBean {

    private final ApolloReader apolloReader;

    @Value("${apollo.app-id}")
    private String appId;
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

    public PropertyConfig(ApolloReader apolloReader) {
        this.apolloReader = apolloReader;
    }

    @Override
    public void afterPropertiesSet() {
        final Map<String, String> properties = this.apolloReader.getProperties();
        this.zkServers = properties.get(Names.ZOOKEEPER_SERVERS);
        this.esHost = properties.get(Names.ELASTICSEARCH_HOSTNAMES);
        this.esUid = properties.get(Names.ELASTICSEARCH_USERNAME);
        this.esPwd = properties.get(Names.ELASTICSEARCH_PASSWORD);
        this.dbHost = properties.get(Names.DATABASE_HOST);
        this.dbPort = properties.get(Names.DATABASE_PORT);
        this.dbName = properties.get(Names.DATABASE_NAME);
        this.dbUid = properties.get(Names.DATABASE_USERNAME);
        this.dbPwd = properties.get(Names.DATABASE_PASSWORD);
    }
}