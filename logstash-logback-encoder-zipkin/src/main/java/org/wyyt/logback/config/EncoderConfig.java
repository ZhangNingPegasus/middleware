package org.wyyt.logback.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.wyyt.logback.core.ApplicationContextZipKin;

/**
 * The config of application context
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Configuration
@EnableConfigurationProperties
public class EncoderConfig {
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public ApplicationContextZipKin applicationContextZipKin() {
        return new ApplicationContextZipKin();
    }
}
