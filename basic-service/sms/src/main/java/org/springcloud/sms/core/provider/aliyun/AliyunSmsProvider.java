package org.springcloud.sms.core.provider.aliyun;

import com.alibaba.fastjson.JSON;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.*;
import com.aliyun.teaopenapi.models.Config;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springcloud.sms.config.PropertyConfig;
import org.springcloud.sms.core.provider.AbstractSmsProvider;
import org.springcloud.sms.entity.Msg;
import org.springcloud.sms.entity.PendingMsgAli;
import org.springcloud.sms.entity.SendDetail;
import org.springcloud.sms.entity.enums.SendStatus;
import org.springcloud.sms.service.MsgService;
import org.springcloud.sms.service.PendingMsgAliService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.wyyt.sms.enums.ProviderType;
import org.wyyt.sms.request.SmsRequest;
import org.wyyt.sms.response.SmsResponse;
import org.wyyt.tool.anno.TranSave;
import org.wyyt.tool.date.DateTool;
import org.wyyt.tool.exception.BusinessException;
import org.wyyt.tool.exception.ExceptionTool;

import java.util.*;
import java.util.stream.Collectors;

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
@Slf4j
@Service
public class AliyunSmsProvider extends AbstractSmsProvider implements InitializingBean {
    private final static String OK_STATUS = "OK";
    private final PropertyConfig propertyConfig;
    private final PendingMsgAliService pendingMsgAliService;
    private final MsgService msgService;
    private final static Map<String, String> ERROR_CODE_MAP = new HashMap<String, String>() {
        {
            put("isv.SMS_SIGNATURE_SCENE_ILLEGAL", "短信所使用签名场景非法");
            put("isv.EXTEND_CODE_ERROR", "扩展码使用错误，相同的扩展码不可用于多个签名");
            put("isv.DOMESTIC_NUMBER_NOT_SUPPORTED", "国际/港澳台消息模板不支持发送境内号码");
            put("isv.DENY_IP_RANGE", "源IP地址所在的地区被禁用");
            put("isv.DAY_LIMIT_CONTROL", "触发日发送限额");
            put("isv.SMS_CONTENT_ILLEGAL", "短信内容包含禁止发送内容");
            put("isv.SMS_SIGN_ILLEGAL", "签名禁止使用");
            put("isp.RAM_PERMISSION_DENY", "RAM权限DENY");
            put("isv.OUT_OF_SERVICE", "业务停机");
            put("isv.PRODUCT_UN_SUBSCRIPT", "未开通云通信产品的阿里云客户");
            put("isv.PRODUCT_UNSUBSCRIBE", "产品未开通");
            put("isv.ACCOUNT_NOT_EXISTS", "账户不存在");
            put("isv.ACCOUNT_ABNORMAL", "账户异常");
            put("isv.SMS_TEMPLATE_ILLEGAL", "短信模版不合法");
            put("isv.SMS_SIGNATURE_ILLEGAL", "短信签名不合法");
            put("isv.INVALID_PARAMETERS", "参数异常");
            put("isp.SYSTEM_ERROR", "系统错误");
            put("isv.MOBILE_NUMBER_ILLEGAL", "非法手机号");
            put("isv.MOBILE_COUNT_OVER_LIMIT", "手机号码数量超过限制");
            put("isv.TEMPLATE_MISSING_PARAMETERS", "模版缺少变量");
            put("isv.BUSINESS_LIMIT_CONTROL", "业务限流");
            put("isv.INVALID_JSON_PARAM", "JSON参数不合法，只接受字符串值");
            put("isv.BLACK_KEY_CONTROL_LIMIT", "黑名单管控");
            put("isv.PARAM_LENGTH_LIMIT", "参数超出长度限制");
            put("isv.PARAM_NOT_SUPPORT_URL", "不支持URL");
            put("isv.AMOUNT_NOT_ENOUGH", "账户余额不足");
            put("isv.TEMPLATE_PARAMS_ILLEGAL", "模版变量里包含非法关键字");
            put("SignatureDoesNotMatch", "Specified signature is not matched with our calculation.");
            put("InvalidTimeStamp.Expired", "Specified time stamp or date value is expired.");
            put("SignatureNonceUsed", "Specified signature nonce was used already.");
            put("InvalidVersion", "Specified parameter Version is not valid.");
            put("InvalidAction.NotFound", "Specified api is not found, please check your url and method");
            put("isv.SIGN_COUNT_OVER_LIMIT", "一个自然日中申请签名数量超过限制。");
            put("isv.TEMPLATE_COUNT_OVER_LIMIT", "一个自然日中申请模板数量超过限制。");
            put("isv.SIGN_NAME_ILLEGAL", "签名名称不符合规范。");
            put("isv.SIGN_FILE_LIMIT", "签名认证材料附件大小超过限制。");
            put("isv.SIGN_OVER_LIMIT", "签名字符数量超过限制。");
            put("isv.TEMPLATE_OVER_LIMIT", "签名字符数量超过限制。");
            put("SIGNATURE_BLACKLIST", "签名黑名单");
            put("isv.SHORTURL_OVER_LIMIT", "一天创建短链数量超过限制");
            put("isv.NO_AVAILABLE_SHORT_URL", "无有效短链");
            put("isv.SHORTURL_NAME_ILLEGAL", "短链名称非法");
            put("isv.SOURCEURL_OVER_LIMIT", "原始链接字符数量超过限制");
            put("isv.SHORTURL_TIME_ILLEGAL", "短链有效期期限超过限制");
            put("isv.PHONENUMBERS_OVER_LIMIT", "上传手机号个数超过上限");
            put("isv.SHORTURL_STILL_AVAILABLE", "原始链接生成的短链仍在有效期内");
            put("isv.SHORTURL_NOT_FOUND", "没有可删除的短链");
            put("isv.ERROR_SIGN_NOT_MODIFY", "签名不支持修改");
        }
    };
    private Client client;

    public AliyunSmsProvider(final PropertyConfig propertyConfig,
                             final PendingMsgAliService pendingMsgAliService,
                             final MsgService msgService) {
        this.propertyConfig = propertyConfig;
        this.pendingMsgAliService = pendingMsgAliService;
        this.msgService = msgService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.client = new Client(new Config()
                .setEndpoint(this.propertyConfig.getAliyunEndpoint().trim())
                .setAccessKeyId(this.propertyConfig.getAliyunAk().trim())
                .setAccessKeySecret(this.propertyConfig.getAliyunSk().trim()));
    }

    @Override
    public SmsResponse send(final SmsRequest smsRequest) {
        final Set<String> phoneNumberList = this.splitPhoneNumbers(smsRequest.getPhoneNumbers());
        if (phoneNumberList.size() == 1) {
            return this.sendSingle(smsRequest);
        } else if (phoneNumberList.size() >= 1) {
            return this.sendBatch(phoneNumberList, smsRequest);
        }
        throw new BusinessException("接受短信的手机号至少需要填写一个");
    }

    @Override
    public ProviderType getProvider() {
        return ProviderType.AliYun;
    }

    @Override
    public void processSendDetails() {
        final QueryWrapper<PendingMsgAli> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(PendingMsgAli::getId).last("LIMIT 100");
        final List<PendingMsgAli> pendingMsgAliList = this.pendingMsgAliService.list(queryWrapper);
        final List<SendDetail> sendDetailList = new ArrayList<>();
        for (final PendingMsgAli pendingMsgAli : pendingMsgAliList) {
            try {
                final SendDetail sendDetail = this.querySendDetails(pendingMsgAli.getPhoneNumber(), pendingMsgAli.getMsgId(), pendingMsgAli.getRowCreateTime());
                if (null == sendDetail) {
                    continue;
                }
                sendDetailList.add(sendDetail);
            } catch (final Exception exception) {
                log.error(ExceptionTool.getRootCauseMessage(exception), exception);
            }
        }
        this.syncSendDetail(sendDetailList);
    }

    @TranSave
    public void saveMsg(final SmsResponse smsResponse,
                        final SmsRequest smsRequest) {
        final Date rowCreateDate = new Date();
        final List<Msg> msgList = this.convertToMsg(smsResponse, smsRequest, rowCreateDate);

        if (null == msgList || msgList.isEmpty()) {
            return;
        }

        final List<PendingMsgAli> pendingMsgAliList = new ArrayList<>(msgList.size());

        for (final Msg msg : msgList) {
            final PendingMsgAli pendingMsgAli = new PendingMsgAli();
            pendingMsgAli.setMsgId(msg.getMsgId());
            pendingMsgAli.setPhoneNumber(msg.getPhoneNumber());
            pendingMsgAli.setRowCreateTime(rowCreateDate);
            pendingMsgAliList.add(pendingMsgAli);
        }

        this.saveSms(msgList);
        this.pendingMsgAliService.batchSave(pendingMsgAliList);
    }

    @TranSave
    public void syncSendDetail(final List<SendDetail> sendDetailList) {
        if (null == sendDetailList || sendDetailList.isEmpty()) {
            return;
        }
        final List<Msg> msgList = new ArrayList<>(sendDetailList.size());
        for (final SendDetail sendDetail : sendDetailList) {
            final Msg msg = this.msgService.getByMsgId(sendDetail.getMsgId(), sendDetail.getPhoneNumber(), sendDetail.getProvider(), sendDetail.getRowCreateTime());
            if (null == msg) {
                continue;
            }
            msg.setContent(sendDetail.getContent());
            msg.setSendTime(sendDetail.getSendTime());
            msg.setReceiveTime(sendDetail.getReceiveTime());
            msg.setSendStatus(sendDetail.getSendStatus().getCode());
            msg.setErrMsg(sendDetail.getErrMsg());
            msgList.add(msg);
        }
        if (!msgList.isEmpty()) {
            this.msgService.batchUpdate(msgList);
            this.pendingMsgAliService.deleteByMsgIds(msgList.stream().map(Msg::getMsgId).collect(Collectors.toList()));
        }
    }

    private SendDetail querySendDetails(final String phoneNumber,
                                        final String bizId,
                                        final Date sendDate) throws Exception {
        final QuerySendDetailsRequest querySendDetailsRequest = new QuerySendDetailsRequest()
                .setPhoneNumber(phoneNumber)
                .setBizId(bizId)
                .setSendDate(DateTool.format(sendDate, "yyyyMMdd"))
                .setPageSize(50L)
                .setCurrentPage(1L);
        final QuerySendDetailsResponse querySendDetailsResponse = this.client.querySendDetails(querySendDetailsRequest);
        final QuerySendDetailsResponseBody body = querySendDetailsResponse.getBody();
        final List<QuerySendDetailsResponseBody.QuerySendDetailsResponseBodySmsSendDetailDTOsSmsSendDetailDTO> smsSendDetailDTO = body.getSmsSendDetailDTOs().getSmsSendDetailDTO();

        if (smsSendDetailDTO.size() >= 1) {
            final QuerySendDetailsResponseBody.QuerySendDetailsResponseBodySmsSendDetailDTOsSmsSendDetailDTO sendDetailDTO = smsSendDetailDTO.get(0);

            //1: 等待回执
            if (sendDetailDTO.getSendStatus().equals(1L)) {
                return null;
            }

            final SendDetail result = new SendDetail();
            result.setMsgId(bizId);
            result.setPhoneNumber(phoneNumber);
            result.setErrMsg("");
            result.setProvider(this.getProvider());
            result.setRowCreateTime(sendDate);
            result.setContent(sendDetailDTO.getContent());
            result.setReceiveTime(DateTool.parse(sendDetailDTO.getReceiveDate()));
            result.setSendTime(DateTool.parse(sendDetailDTO.getSendDate()));

            //2: 发送失败
            if (sendDetailDTO.getSendStatus().equals(2L)) {
                final String errMsg = ERROR_CODE_MAP.get(body.getCode());
                result.setSendStatus(SendStatus.ERROR);
                result.setErrMsg(StringUtils.isEmpty(errMsg) ? body.getCode() : errMsg);
            }

            //3: 发送成功
            if (sendDetailDTO.getSendStatus().equals(3L)) {
                result.setSendStatus(SendStatus.SUCCESS);
            }
            return result;
        }
        return null;
    }

    private SmsResponse sendSingle(final SmsRequest smsRequest) {
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

    private SmsResponse sendBatch(final Set<String> phoneNumbers,
                                  final SmsRequest smsRequest) {
        final SmsResponse result = new SmsResponse();
        result.setProviderType(this.getProvider());

        final List<String> signNameList = new ArrayList<>();
        for (final String phoneNumber : phoneNumbers) {
            signNameList.add(smsRequest.getSignCode());
        }

        final List<String> templateParamList = new ArrayList<>();
        for (final String phoneNumber : phoneNumbers) {
            templateParamList.add(smsRequest.getTemplateParam());
        }

        final String phoneNumberJson = JSON.toJSONString(phoneNumbers);
        final String signNameJson = JSON.toJSONString(signNameList);
        final String templateParamJson = JSON.toJSONString(templateParamList);

        final SendBatchSmsRequest sendBatchSmsRequest = new SendBatchSmsRequest()
                .setPhoneNumberJson(phoneNumberJson)
                .setSignNameJson(signNameJson)
                .setTemplateCode(smsRequest.getTemplateCode())
                .setTemplateParamJson(templateParamJson);
        SendBatchSmsResponse sendBatchSmsResponse = null;
        try {
            sendBatchSmsResponse = this.client.sendBatchSms(sendBatchSmsRequest);
            final SendBatchSmsResponseBody body = sendBatchSmsResponse.getBody();
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
            if (null != sendBatchSmsResponse && null != sendBatchSmsResponse.getBody()) {
                result.setId(sendBatchSmsResponse.getBody().getBizId());
                result.setCode(sendBatchSmsResponse.getBody().getCode());
            }
        }

        saveMsg(result, smsRequest);
        return result;
    }
}