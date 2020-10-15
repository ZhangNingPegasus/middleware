package org.wyyt.redis.auto;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.wyyt.redis.service.RedisService;

import java.time.Duration;

/**
 * The configuration of redis
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Configuration
@ConditionalOnClass({RedisOperations.class})
@EnableConfigurationProperties(RedisProperties.class)
public class RedisAutoConfig {
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public RedisService redisService() {
        return new RedisService();
    }

    @Bean(name = "redisSerializer")
    @Primary
    @ConditionalOnMissingBean
    public RedisSerializer<Object> jackson2JsonRedisSerializer() {
        final Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, Visibility.ANY);
        objectMapper.activateDefaultTyping(BasicPolymorphicTypeValidator.builder().build(), ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        return jackson2JsonRedisSerializer;
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            final StringBuilder result = new StringBuilder();
            result.append(target.getClass().getName());
            result.append(".").append(method.getName());
            final StringBuilder strParams = new StringBuilder();
            for (Object param : params) {
                if (null == param) {
                    param = "null";
                }
                strParams.append(param.toString()).append(":");
            }
            if (strParams.length() > 0) {
                result.append("_").append(strParams);
            }
            return result.toString();
        };
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public CacheManager cacheManager(final RedisConnectionFactory redisConnectionFactory,
                                     final RedisSerializer<Object> redisSerializer) {
        final RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig() // 开启默认配置
                .entryTtl(Duration.ofSeconds(60L * 60L * 24L * 7L)) // 设置超时时间. 7天
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())) // 设置key序列化器
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer)) // 设置value序列化器
                .disableCachingNullValues(); // 不允许存储null值

        return RedisCacheManager.builder(redisConnectionFactory) // 指定redis生产者
                .cacheDefaults(cacheConfiguration) // 指定redis配置
                .build(); // 构建
    }

    @Bean(name = "redisTemplateNoTransactional")
    @Primary
    public RedisTemplate<String, Object> redisTemplateNoTransactional(final RedisConnectionFactory factory,
                                                                      final RedisSerializer<Object> redisSerializer) {
        final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        final StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer); // String对应key的序列化器
        redisTemplate.setHashKeySerializer(stringRedisSerializer); // HashKey对应key的序列化器
        redisTemplate.setDefaultSerializer(redisSerializer);// value对应key的序列化器
        redisTemplate.setEnableTransactionSupport(false); // 关闭事务支持
        // 如果开启Redis事务, 且业务没有使用Spring的事务进行管理, 则连接不会自动释放, 需要使用下面的代码进行手动释放
        // RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}