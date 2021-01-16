package org.wyyt.sharding.db2es.client;

import com.sijibao.nacos.spring.util.NacosNativeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.wyyt.sharding.db2es.client.boot.Boot;
import org.wyyt.sharding.db2es.client.common.Context;
import org.wyyt.sharding.db2es.core.entity.domain.Config;
import org.wyyt.sharding.db2es.core.entity.domain.Names;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.wyyt.sharding.db2es.client.common.Constant.PROPERTIES_FILE_NAME;

/**
 * Responsible for consuming canal messages in Kafka and load data into Elastic-Search
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class Db2EsClient {
    private static final String CLASSPATH_URL_PREFIX = "classpath:";
    private static final String PROPERTIES_FILE = CLASSPATH_URL_PREFIX + PROPERTIES_FILE_NAME;

    public static void main(final String[] args) throws Exception {
        try (Context context = new Context(getConfig())) {
            Boot.boot(context);
        }
    }

    public static Config getConfig() throws Exception {
        final Config result = new Config();

        final Properties properties = new Properties();
        final String workDir = System.getProperty("work.dir", PROPERTIES_FILE);

        if (workDir.startsWith(CLASSPATH_URL_PREFIX)) {
            try (final InputStream in = Db2EsClient.class.getClassLoader().getResourceAsStream(StringUtils.substringAfter(workDir, CLASSPATH_URL_PREFIX))) {
                properties.load(in);
            }
        } else {
            final File configFolder = new File(workDir.concat(File.separator).concat("config"));
            try (final InputStream in = new FileInputStream(configFolder.getCanonicalPath().concat(File.separator).concat(PROPERTIES_FILE_NAME))) {
                properties.load(in);
            }
        }
        readAcmSetting(properties);

        result.setDb2EsId(Integer.parseInt(properties.getProperty(Names.DB2ES_ID, "1").trim()));
        result.setDb2EsHost(properties.getProperty(Names.DB2ES_HOST, "").trim());
        result.setDb2EsPort(Integer.parseInt(properties.getProperty(Names.DB2ES_PORT, "10086").trim()));
        result.setContinueOnError(Boolean.parseBoolean(properties.getProperty(Names.CONTINUE_ON_ERROR, "false").trim()));
        result.setZkServers(properties.getProperty(Names.ZOOKEEPER_SERVERS, "").trim());
        result.setEsHost(properties.getProperty(Names.ELASTICSEARCH_HOSTNAMES, "").trim());
        result.setEsUid(properties.getProperty(Names.ELASTICSEARCH_USERNAME, "").trim());
        result.setEsPwd(properties.getProperty(Names.ELASTICSEARCH_PASSWORD, "").trim());
        result.setDbHost(properties.getProperty(Names.DATABASE_HOST, "").trim());
        result.setDbPort(Integer.parseInt(properties.getProperty(Names.DATABASE_PORT, "3306").trim()));
        result.setDbName(properties.getProperty(Names.DATABASE_NAME, "").trim());
        result.setDbUid(properties.getProperty(Names.DATABASE_USERNAME, "").trim());
        result.setDbPwd(properties.getProperty(Names.DATABASE_PASSWORD, "").trim());
        result.setInitialCheckpoint(properties.getProperty(Names.INITIAL_CHECKPOINT, "").trim());
        result.setAcmDataId(properties.getProperty(Names.ACM_DATA_ID, "").trim());
        result.setAcmGroupId(properties.getProperty(Names.ACM_GROUP_ID, "").trim());
        result.setAcmConfigPath(properties.getProperty(Names.ACM_CONFIG_PATH, "").trim());
        result.setAcmNacosLocalSnapshotPath(properties.getProperty(Names.ACM_NACOS_LOCAL_SNAPSHOT_PATH, "").trim());
        result.setAcmNacosLogPath(properties.getProperty(Names.ACM_NACOS_LOG_PATH, "").trim());

        final Map<String, String> topicCheckpointSet = new HashMap<>();
        for (final Map.Entry<Object, Object> pair : properties.entrySet()) {
            if (Pattern.matches("db2es.*-.*checkpoint", pair.getKey().toString())) {
                topicCheckpointSet.put(pair.getKey().toString().trim(), pair.getValue().toString().trim());
            }
        }
        result.setTopicCheckpointMap(topicCheckpointSet);
        return result;
    }

    private static void readAcmSetting(final Properties properties) throws Exception {
        NacosNativeUtils.loadAcmInfo(properties.getProperty(Names.ACM_DATA_ID, ""),
                properties.getProperty(Names.ACM_GROUP_ID, ""),
                properties.getProperty(Names.ACM_CONFIG_PATH, ""),
                properties.getProperty(Names.ACM_NACOS_LOCAL_SNAPSHOT_PATH, ""),
                properties.getProperty(Names.ACM_NACOS_LOG_PATH, ""));
        final Properties acmProperties = NacosNativeUtils.getConfig();

        for (final Map.Entry<Object, Object> item : acmProperties.entrySet()) {
            final String key = item.getKey().toString();
            final String value = item.getValue().toString();
            properties.setProperty(key, value);
        }
    }
}