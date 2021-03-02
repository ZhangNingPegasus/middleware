package org.springcloud.sms.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.wyyt.tool.cache.CacheService;

/**
 * The config of cache
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Configuration
public class CacheConfig {
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public CacheService cacheService() {
        return new CacheService();
    }
}
