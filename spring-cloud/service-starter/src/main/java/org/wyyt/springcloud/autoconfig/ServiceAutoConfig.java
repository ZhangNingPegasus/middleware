package org.wyyt.springcloud.autoconfig;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import com.nepxion.discovery.plugin.strategy.context.StrategyContextHolder;
import com.nepxion.discovery.plugin.strategy.service.constant.ServiceStrategyConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.instrument.async.LazyTraceExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.wyyt.springcloud.advice.ExceptionControllerAdvice;
import org.wyyt.springcloud.exception.PermissionException;
import org.wyyt.springcloud.permission.PermissionAutoScanProxy;
import org.wyyt.springcloud.permission.PermissionInterceptor;
import org.wyyt.springcloud.permission.PermissionPersister;
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
    private final ConfigurableEnvironment environment;
    private final PluginAdapter pluginAdapter;
    private final StrategyContextHolder strategyContextHolder;


    public ServiceAutoConfig(final BeanFactory beanFactory,
                             final ConfigurableEnvironment environment,
                             final PluginAdapter pluginAdapter,
                             final StrategyContextHolder strategyContextHolder) {
        this.beanFactory = beanFactory;
        this.environment = environment;
        this.pluginAdapter = pluginAdapter;
        this.strategyContextHolder = strategyContextHolder;
    }

    @Bean
    public Executor executor() {
        return new LazyTraceExecutor(this.beanFactory, Common.generateExecutor("Pool-Executor-"));
    }

    @Bean
    public ExceptionControllerAdvice exceptionControllerAdvice() {
        return new ExceptionControllerAdvice();
    }

    @Bean
    public PermissionAutoScanProxy permissionAutoScanProxy() {
        final String scanPackages = this.environment.getProperty(ServiceStrategyConstant.SPRING_APPLICATION_STRATEGY_SCAN_PACKAGES);
        if (StringUtils.isEmpty(scanPackages)) {
            throw new PermissionException(ServiceStrategyConstant.SPRING_APPLICATION_STRATEGY_SCAN_PACKAGES + "'s value can't be empty");
        }
        if (scanPackages.contains(DiscoveryConstant.ENDPOINT_SCAN_PACKAGES)) {
            throw new PermissionException("It can't scan packages for '" + DiscoveryConstant.ENDPOINT_SCAN_PACKAGES + "', please check '" + ServiceStrategyConstant.SPRING_APPLICATION_STRATEGY_SCAN_PACKAGES + "'");
        }
        return new PermissionAutoScanProxy(scanPackages, this.pluginAdapter);
    }

    @Bean
    public PermissionInterceptor permissionInterceptor() {
        return new PermissionInterceptor(pluginAdapter, strategyContextHolder);
    }

    @Bean
    public PermissionPersister permissionPersister() {
        return new PermissionPersister();
    }
}