package org.springcloud.sms.core;

import org.springcloud.sms.core.provider.SmsProvider;
import org.springcloud.sms.core.provider.SmsProviderFactory;
import org.springframework.stereotype.Service;
import org.wyyt.sms.request.SmsRequest;
import org.wyyt.sms.response.SmsResponse;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

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
    private final Executor executor;

    public SmsService(final SmsProviderFactory smsProviderFactory,
                      final Executor executor) {
        this.smsProviderFactory = smsProviderFactory;
        this.executor = executor;
    }

    public SmsResponse send(final SmsRequest smsRequest) {
        final SmsProvider provider = this.smsProviderFactory.getProvider();
        return provider.send(smsRequest);
    }

    public void processSendDetails() throws InterruptedException {
        final List<SmsProvider> providers = this.smsProviderFactory.getProviders();
        final CountDownLatch cdl = new CountDownLatch(providers.size());
        for (final SmsProvider provider : providers) {
            this.executor.execute(() -> {
                try {
                    provider.processSendDetails();
                } finally {
                    cdl.countDown();
                }
            });
        }
        cdl.await();
    }
}