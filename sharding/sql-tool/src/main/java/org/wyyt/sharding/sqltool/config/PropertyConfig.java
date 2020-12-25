package org.wyyt.sharding.sqltool.config;

import com.sijibao.nacos.spring.util.NacosNativeUtils;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.wyyt.tool.db.CrudService;

import java.util.Properties;

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
@Data
public class PropertyConfig implements InitializingBean {
    public static String SERVER_PORT_NAME = "sql.tool.port";
    public static String DB_HOST_NAME = "db.host";
    public static String DB_PORT_NAME = "db.port";
    public static String DB_UID_NAME = "db.username";
    public static String DB_PWD_NAME = "encrypt.db.password";
    public static String DB_NAME = "db.dbName";

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

    private Integer serverPort;
    private String dbHost;
    private String dbPort;
    private String dbUid;
    private String dbPwd;
    private String dbName;

    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        return new TomcatServletWebServerFactory(this.serverPort);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public CrudService crudService() {
        return new CrudService(
                this.dbHost,
                this.dbPort,
                this.dbName,
                this.dbUid,
                this.dbPwd,
                10,
                20
        );
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        NacosNativeUtils.loadAcmInfo(this.dataId,
                this.groupId,
                this.configPath,
                this.snapshotPath,
                this.logPath);
        final Properties acmProperties = NacosNativeUtils.getConfig();
        this.serverPort = Integer.parseInt(acmProperties.getProperty(SERVER_PORT_NAME, "10086"));
        this.dbHost = acmProperties.getProperty(DB_HOST_NAME, "");
        this.dbPort = acmProperties.getProperty(DB_PORT_NAME, "3306");
        this.dbUid = acmProperties.getProperty(DB_UID_NAME, "");
        this.dbPwd = acmProperties.getProperty(DB_PWD_NAME, "");
        this.dbName = acmProperties.getProperty(DB_NAME, "");
    }
}