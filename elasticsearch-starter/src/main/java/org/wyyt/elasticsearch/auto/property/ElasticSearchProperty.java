package org.wyyt.elasticsearch.auto.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * the auto-configuration property of Elastic-Search
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
@ConfigurationProperties("elasticsearch")
public class ElasticSearchProperty {
    /**
     * 是否开启ElasticSearch Service
     */
    private Boolean enabled;
    /**
     * ElasticSearch的地址和端口, 如:192.168.6.165:9200,192.168.6.166:9200
     */
    private List<String> hostnames;

    /**
     * ElasticSearch账号
     */
    private String username;

    /**
     * ElasticSearch密码
     */
    private String password;

    /**
     * 多线程访问时最大并发量
     */
    private int maxConnTotal = -1;

    /**
     * 单次路由线程上限
     */
    private int maxConnPerRoute = -1;
}