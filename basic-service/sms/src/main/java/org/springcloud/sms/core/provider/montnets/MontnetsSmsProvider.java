package org.springcloud.sms.core.provider.montnets;

import org.springcloud.sms.core.provider.AbstractSmsProvider;
import org.springframework.stereotype.Service;
import org.wyyt.sms.enums.ProviderType;
import org.wyyt.sms.request.SmsRequest;
import org.wyyt.sms.response.SmsResponse;

/**
 * The SMS provider of Montnets
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class MontnetsSmsProvider extends AbstractSmsProvider {
    @Override
    public SmsResponse send(final SmsRequest smsRequest) {
        return null;
    }

    @Override
    public void processSendDetails() {
        System.out.println("梦网处理短信发送回执");
    }

    @Override
    public ProviderType getProvider() {
        return ProviderType.MONTNETS;
    }
}
