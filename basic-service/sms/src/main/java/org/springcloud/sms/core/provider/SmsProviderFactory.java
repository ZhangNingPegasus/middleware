package org.springcloud.sms.core.provider;

import org.springcloud.sms.config.PropertyConfig;
import org.springcloud.sms.exception.SmsException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.wyyt.sms.enums.ProviderType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The SMS provider factory
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class SmsProviderFactory implements ApplicationContextAware, InitializingBean {
    private ApplicationContext applicationContext;
    private final PropertyConfig propertyConfig;
    private final Map<ProviderType, SmsProvider> providerMap = new HashMap<>();

    public SmsProviderFactory(final PropertyConfig propertyConfig) {
        this.propertyConfig = propertyConfig;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        final Map<String, SmsProvider> smsProviderMap = this.applicationContext.getBeansOfType(SmsProvider.class);
        for (final Map.Entry<String, SmsProvider> pair : smsProviderMap.entrySet()) {
            this.providerMap.put(pair.getValue().getProvider(), pair.getValue());
        }
    }

    public SmsProvider getProvider() {
        final String errorMsg = String.format("不存在类型为[%s]的短信提供商, 请检查枚举类型[%s]的枚举项",
                this.propertyConfig.getProviderType(),
                ProviderType.class.getCanonicalName());

        final ProviderType providerType = ProviderType.get(this.propertyConfig.getProviderType());
        if (null == providerType) {
            throw new SmsException(errorMsg);
        }
        final SmsProvider result = this.providerMap.get(providerType);
        if (null == result) {
            throw new SmsException(errorMsg);
        }
        return result;
    }

    public List<SmsProvider> getProviders(){
        return new ArrayList<>(this.providerMap.values());
    }
}