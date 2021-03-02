package org.springcloud.sms.core;

import org.springcloud.sms.core.provider.SmsProvider;
import org.springcloud.sms.core.provider.SmsProviderFactory;
import org.springframework.stereotype.Service;
import org.wyyt.sms.request.SmsRequest;
import org.wyyt.sms.response.SmsResponse;

/**
 * The implementation of SmsService
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class SmsService {
    protected final SmsProviderFactory smsProviderFactory;

    public SmsService(final SmsProviderFactory smsProviderFactory) {
        this.smsProviderFactory = smsProviderFactory;
    }

    public SmsResponse send(final SmsRequest smsRequest) {
        final SmsProvider provider = this.smsProviderFactory.getProvider();
        return provider.send(smsRequest);
    }
}