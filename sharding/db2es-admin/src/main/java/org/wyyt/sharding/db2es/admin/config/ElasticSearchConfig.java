package org.wyyt.sharding.db2es.admin.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.wyyt.elasticsearch.auto.property.ElasticSearchProperty;
import org.wyyt.elasticsearch.service.ElasticSearchService;

import java.util.Arrays;

/**
 * the configuration of Elastic-Search
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Configuration
public class ElasticSearchConfig {
    private final PropertyConfig propertyConfig;

    public ElasticSearchConfig(final PropertyConfig propertyConfig) {
        this.propertyConfig = propertyConfig;
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public ElasticSearchService elasticSearchService() {
        final ElasticSearchProperty elasticSearchProperty = new ElasticSearchProperty();
        elasticSearchProperty.setHostnames(Arrays.asList(this.propertyConfig.getEsHost().split(",")));
        elasticSearchProperty.setUsername(this.propertyConfig.getEsUid());
        elasticSearchProperty.setPassword(this.propertyConfig.getEsPwd());
        elasticSearchProperty.setMaxConnTotal(100);
        elasticSearchProperty.setMaxConnPerRoute(20);
        return new ElasticSearchService(elasticSearchProperty);
    }
}
