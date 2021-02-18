package org.wyyt.springcloud.boot;

import brave.Tracer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.instrument.async.LazyTraceExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ObjectUtils;
import org.wyyt.springcloud.advice.ExceptionControllerAdvice;
import org.wyyt.springcloud.trace.HttpResponseInjectingTraceFilter;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * The configuration of services
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@EnableAsync
@Configuration
@EnableConfigurationProperties(SpringCloudApplicationProperties.class)
public class SpringCloudApplicationAutoConfig {
    private final BeanFactory beanFactory;
    private final SpringCloudApplicationProperties springCloudApplicationProperties;

    public SpringCloudApplicationAutoConfig(final BeanFactory beanFactory,
                                            final SpringCloudApplicationProperties springCloudApplicationProperties) {
        this.beanFactory = beanFactory;
        this.springCloudApplicationProperties = springCloudApplicationProperties;
    }

    @Bean
    public HttpResponseInjectingTraceFilter httpResponseInjectingTraceFilter(final Tracer tracer) {
        return new HttpResponseInjectingTraceFilter(tracer);
    }

    @Bean
    public Executor executor() {
        final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(
                getOrDefault(this.springCloudApplicationProperties.getCorePoolSize(), Runtime.getRuntime().availableProcessors())
        );

        threadPoolTaskExecutor.setMaxPoolSize(
                getOrDefault(this.springCloudApplicationProperties.getMaxPoolSize(), Runtime.getRuntime().availableProcessors() * 5)
        );

        threadPoolTaskExecutor.setQueueCapacity(
                getOrDefault(this.springCloudApplicationProperties.getQueueCapacity(), Runtime.getRuntime().availableProcessors() * 2)
        );

        threadPoolTaskExecutor.setThreadNamePrefix(
                getOrDefault(this.springCloudApplicationProperties.getThreadNamePrefix(), SpringCloudApplicationProperties.DEFAULT_THREAD_NAME_PREFIX)
        );
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.initialize();
        return new LazyTraceExecutor(this.beanFactory, threadPoolTaskExecutor);
    }

    @Bean
    public ExceptionControllerAdvice exceptionControllerAdvice() {
        return new ExceptionControllerAdvice();
    }

    private <T> T getOrDefault(T value, T def) {
        if (ObjectUtils.isEmpty(value)) {
            return def;
        }
        return value;
    }
}