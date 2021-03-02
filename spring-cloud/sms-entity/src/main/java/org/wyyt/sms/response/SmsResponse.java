package org.wyyt.sms.response;

import lombok.Data;
import org.wyyt.sms.enums.ProviderType;

/**
 * 发送短信返回值实体类
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public class SmsResponse {
    /**
     * 短信发送后的唯一标识
     */
    private String id;

    /**
     * 短信提供商返回的响应编码
     */
    private String code;

    /**
     * 发送方法是否调用成功(调用成功不一定代表发送成功)
     */
    private Boolean ok;

    /**
     * 异常错误信息
     */
    private String errorMsg;

    /**
     * 短信服务商类型
     */
    private ProviderType providerType;
}