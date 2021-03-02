package org.wyyt.sms.request;

import lombok.Data;
import lombok.ToString;

/**
 * 发送短信请求实体类
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@ToString
@Data
public class SmsRequest {
    /**
     * 发送方手机号码, 多个用逗号分隔
     */
    private String phoneNumbers;

    /**
     * 短信签名编码
     */
    private String signCode;

    /**
     * 短信模板编码
     */
    private String templateCode;

    /**
     * 短信模板参数, 必须是json格式
     */
    private String templateParam;

    /**
     * 扩展字段
     */
    private String extra;
}