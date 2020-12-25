package org.wyyt.kafka.auto;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.wyyt.kafka.service.KafkaService;
import org.wyyt.kafka.tran.KafkaTranAop;

/**
 * Auto-configuration of ShardingSphere property
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * ******************************************************************
 * Name               Action            Time          Description   *
 * Ning.Zhang       Initialize         10/1/2020        Initialize  *
 * ******************************************************************
 */
@Configuration
@EnableConfigurationProperties
public class KafkaAutoConfig {
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public KafkaTranAop kafkaTranAop(final KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaTranAop(kafkaTemplate);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public KafkaService kafkaService(final KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaService(kafkaTemplate);
    }
}