package org.wyyt.springcloud.autoconfig;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.instrument.async.LazyTraceExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.wyyt.springcloud.advice.ExceptionControllerAdvice;
import org.wyyt.springcloud.util.Common;

import java.util.concurrent.Executor;

/**
 * The configuration of services
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@EnableAsync
@Configuration
@EnableConfigurationProperties(ServiceProperties.class)
public class ServiceAutoConfig {
    private final BeanFactory beanFactory;

    public ServiceAutoConfig(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Bean
    public Executor executor() {
        return new LazyTraceExecutor(this.beanFactory, Common.generateExecutor("Pool-Executor-"));
    }

    @Bean
    public ExceptionControllerAdvice exceptionControllerAdvice() {
        return new ExceptionControllerAdvice();
    }
}