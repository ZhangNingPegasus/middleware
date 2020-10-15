package org.wyyt.elasticsearch.auto;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.wyyt.elasticsearch.auto.property.ElasticSearchProperty;
import org.wyyt.elasticsearch.service.ElasticSearchService;

/**
 * the auto-configuration of Elastic-Search
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Configuration
@EnableConfigurationProperties(value = ElasticSearchProperty.class)
@ConditionalOnProperty(prefix = "elasticsearch", name = "enabled", havingValue = "true")
public class ElasticSearchAutoConfig {

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public ElasticSearchService elasticSearchService() {
        return new ElasticSearchService(this.elasticSearchProperty);
    }

    private final ElasticSearchProperty elasticSearchProperty;

    public ElasticSearchAutoConfig(final ElasticSearchProperty elasticSearchProperty) {
        this.elasticSearchProperty = elasticSearchProperty;
    }
}