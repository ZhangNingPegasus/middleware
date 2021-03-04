package org.springcloud.sms.core.provider;

import org.wyyt.sms.enums.ProviderType;
import org.wyyt.sms.request.SmsRequest;
import org.wyyt.sms.response.SmsResponse;

/**
 * The interface of SMS provider
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public interface SmsProvider {
    SmsResponse send(SmsRequest smsRequest);

    ProviderType getProvider();
}
