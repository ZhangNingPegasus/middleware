package org.springcloud.sms.core.provider.aliyun;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.dysmsapi20170525.models.SendSmsResponseBody;
import com.aliyun.teaopenapi.models.Config;
import org.springcloud.sms.config.PropertyConfig;
import org.springcloud.sms.core.provider.AbstractSmsProvider;
import org.springcloud.sms.entity.Msg;
import org.springcloud.sms.entity.PendingMsgAli;
import org.springcloud.sms.service.PendingMsgAliService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.wyyt.sms.enums.ProviderType;
import org.wyyt.sms.request.SmsRequest;
import org.wyyt.sms.response.SmsResponse;
import org.wyyt.tool.anno.TranSave;
import org.wyyt.tool.exception.ExceptionTool;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The SMS provider of Aliyun
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class AliyunSmsProvider extends AbstractSmsProvider implements InitializingBean {
    private final static String OK_STATUS = "OK";
    private final PropertyConfig propertyConfig;
    private final PendingMsgAliService pendingMsgAliService;
    private Client client;

    public AliyunSmsProvider(final PropertyConfig propertyConfig,
                             final PendingMsgAliService pendingMsgAliService) {
        this.propertyConfig = propertyConfig;
        this.pendingMsgAliService = pendingMsgAliService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.client = new Client(new Config()
                .setEndpoint(this.propertyConfig.getAliyunEndpoint().trim())
                .setAccessKeyId(this.propertyConfig.getAliyunAk().trim())
                .setAccessKeySecret(this.propertyConfig.getAliyunSk().trim()));
    }

    @Override
    public SmsResponse send(SmsRequest smsRequest) {
        final SmsResponse result = new SmsResponse();
        result.setProviderType(this.getProvider());
        final SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setPhoneNumbers(smsRequest.getPhoneNumbers())
                .setSignName(smsRequest.getSignCode())
                .setTemplateCode(smsRequest.getTemplateCode())
                .setTemplateParam(smsRequest.getTemplateParam())
                .setOutId(smsRequest.getExtra());

        SendSmsResponse sendSmsResponse = null;
        try {
            sendSmsResponse = this.client.sendSms(sendSmsRequest);
            final SendSmsResponseBody body = sendSmsResponse.getBody();
            result.setCode(body.getCode());
            result.setId(body.getBizId());
            if (OK_STATUS.equalsIgnoreCase(body.getCode())) {
                result.setOk(true);
            } else {
                result.setOk(false);
                result.setErrorMsg(body.getMessage());
            }
        } catch (final Exception exception) {
            result.setOk(false);
            result.setErrorMsg(ExceptionTool.getRootCauseMessage(exception));
            if (null != sendSmsResponse && null != sendSmsResponse.getBody()) {
                result.setId(sendSmsResponse.getBody().getBizId());
                result.setCode(sendSmsResponse.getBody().getCode());
            }
        }

        saveMsg(result, smsRequest);
        return result;
    }

    @TranSave
    public void saveMsg(final SmsResponse smsResponse,
                        final SmsRequest smsRequest) {
        final Date rowCreateDate = new Date();
        final List<Msg> msgList = this.convertToMsg(smsResponse, smsRequest, rowCreateDate);

        final List<PendingMsgAli> pendingMsgAliList = new ArrayList<>(msgList.size());

        for (final Msg msg : msgList) {
            final PendingMsgAli pendingMsgAli = new PendingMsgAli();
            pendingMsgAli.setMsgId(msg.getMsgId());
            pendingMsgAli.setRowCreateTime(rowCreateDate);
            pendingMsgAliList.add(pendingMsgAli);
        }

        this.saveSms(msgList);
        this.pendingMsgAliService.batchSave(pendingMsgAliList);
    }

    @Override
    public ProviderType getProvider() {
        return ProviderType.AliYun;
    }
}