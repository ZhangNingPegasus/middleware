package org.springcloud.sms.entity;

import lombok.Data;
import org.springcloud.sms.entity.enums.SendStatus;
import org.wyyt.sms.enums.ProviderType;

import java.util.Date;

/**
 * SMS detail entity
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Data
public class SendDetail {
    private String msgId;
    private String phoneNumber;
    private String content;
    private SendStatus sendStatus;
    private String errMsg;
    private ProviderType provider;
    private Date sendTime;
    private Date receiveTime;
    private Date rowCreateTime;
}
