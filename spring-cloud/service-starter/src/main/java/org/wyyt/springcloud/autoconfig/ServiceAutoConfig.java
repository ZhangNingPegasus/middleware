package org.wyyt.springcloud.autoconfig;

import brave.Tracer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.instrument.async.LazyTraceExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
@EnableConfigurationProperties(ServiceProperties.class)
public class ServiceAutoConfig {
    private final BeanFactory beanFactory;

    public ServiceAutoConfig(final BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Bean
    public HttpResponseInjectingTraceFilter httpResponseInjectingTraceFilter(final Tracer tracer) {
        return new HttpResponseInjectingTraceFilter(tracer);
    }

    @Bean
    public Executor executor() {
        final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        threadPoolTaskExecutor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 5);
        threadPoolTaskExecutor.setQueueCapacity(Runtime.getRuntime().availableProcessors() * 2);
        threadPoolTaskExecutor.setThreadNamePrefix("Pool-Executor-");
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.initialize();
        return new LazyTraceExecutor(this.beanFactory, threadPoolTaskExecutor);
    }

    @Bean
    public ExceptionControllerAdvice exceptionControllerAdvice() {
        return new ExceptionControllerAdvice();
    }
}