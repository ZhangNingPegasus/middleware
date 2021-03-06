package org.springcloud.sms.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * the entity of configuration information
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Getter
@Configuration
public class PropertyConfig {
    @Value("${spring.cloud.client.ip-address}")
    private String host;

    @Value("${server.port}")
    private String port;

    @Value("${sms.provider.type}")
    private Integer providerType;

    @Value("${aliyun_endpoint}")
    private String aliyunEndpoint;

    @Value("${aliyun_ak}")
    private String aliyunAk;

    @Value("${aliyun_sk}")
    private String aliyunSk;

    @Value("${mengwang_userId}")
    private String mengwangUid;

    @Value("${mengwang_password}")
    private String mengwangPwd;

    @Value("${db_host}")
    private String dbHost;

    @Value("${db_port}")
    private String dbPort;

    @Value("${db_name}")
    private String dbName;

    @Value("${db_username}")
    private String dbUid;

    @Value("${db_password}")
    private String dbPwd;

    @Value("${db_min_idle}")
    private int dbMinIdle;

    @Value("${db_maximum}")
    private int dbMaximum;

    @Value("${mq_msg_topic}")
    private String msgTopic;
}