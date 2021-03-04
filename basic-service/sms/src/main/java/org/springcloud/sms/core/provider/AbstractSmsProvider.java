package org.springcloud.sms.core.provider;

import org.springcloud.sms.entity.Msg;
import org.springcloud.sms.entity.enums.SendStatus;
import org.springcloud.sms.service.MsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.wyyt.sms.request.SmsRequest;
import org.wyyt.sms.response.SmsResponse;
import org.wyyt.springcloud.service.EnvironmentService;
import org.wyyt.tool.common.CommonTool;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The abstract class of SMS provider
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public abstract class AbstractSmsProvider implements SmsProvider {
    @Autowired
    protected MsgService msgService;
    @Autowired
    protected EnvironmentService environmentService;

    protected List<Msg> convertToMsg(final SmsResponse smsResponse,
                                     final SmsRequest smsRequest,
                                     final Date rowCreateTime,
                                     final PostProcessor postProcessor) {
        if (ObjectUtils.isEmpty(smsResponse.getId())) {
            return null;
        }

        final String phoneNumbers = smsRequest.getPhoneNumbers();
        final String[] phoneNumberArray = phoneNumbers.split(",");
        final List<Msg> result = new ArrayList<>(phoneNumberArray.length);

        for (final String phoneNumber : phoneNumberArray) {
            if (null == phoneNumber || ObjectUtils.isEmpty(phoneNumber.trim())) {
                continue;
            }
            Msg msg = new Msg();
            msg.setMsgId(smsResponse.getId());
            msg.setPhoneNumber(phoneNumber);
            msg.setContent("");
            msg.setSignCode(smsRequest.getSignCode());
            msg.setTemplateCode(smsRequest.getTemplateCode());
            msg.setTemplateParam(smsRequest.getTemplateParam());
            msg.setProvider(this.getProvider().getCode());
            msg.setSendIp(this.environmentService.getIpAddress());
            msg.setSendTime(CommonTool.getMinDate());
            msg.setReceiveTime(CommonTool.getMinDate());
            msg.setSendStatus(SendStatus.PENDING.getCode());
            msg.setExtra(smsRequest.getExtra());
            msg.setRowCreateTime(rowCreateTime);
            if (null != postProcessor) {
                msg = postProcessor.after(msg);
            }
            result.add(msg);
        }

        return result;
    }

    protected List<Msg> convertToMsg(final SmsResponse smsResponse,
                                     final SmsRequest smsRequest,
                                     final Date rowCreateTime) {
        return convertToMsg(smsResponse, smsRequest, rowCreateTime, null);
    }

    protected void saveSms(final List<Msg> msgList) {
        this.msgService.batchSave(msgList);
    }

    public interface PostProcessor {
        Msg after(final Msg msg);
    }
}
