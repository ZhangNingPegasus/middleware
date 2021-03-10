package org.springcloud.sms.controller;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.QuerySendDetailsRequest;
import com.aliyun.dysmsapi20170525.models.QuerySendDetailsResponse;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.montnets.mwgate.common.GlobalParams;
import com.montnets.mwgate.common.Message;
import com.montnets.mwgate.smsutil.ConfigManager;
import com.montnets.mwgate.smsutil.SmsSendConn;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springcloud.sms.core.SmsService;
import org.springcloud.sms.service.PendingMsgAliService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wyyt.sms.request.SmsRequest;
import org.wyyt.sms.response.SmsResponse;
import org.wyyt.tool.rpc.Result;

/**
 * The controller of SMS
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Api("短信相关接口")
@Slf4j
@RestController
public class SmsController {
    private final SmsService smsService;
    private final PendingMsgAliService pendingMsgAliService;

    public SmsController(final SmsService smsService,
                         final PendingMsgAliService pendingMsgAliService) {
        this.smsService = smsService;
        this.pendingMsgAliService = pendingMsgAliService;
    }

    @ApiOperation(value = "同步发送短信")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phoneNumbers", value = "发送方手机号码, 多个用逗号分隔", required = true, dataType = "String"),
            @ApiImplicitParam(name = "signCode", value = "短信签名编码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "templateCode", value = "短信模板编码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "templateParam", value = "短信模板参数, 必须是json格式", required = true, dataType = "String"),
            @ApiImplicitParam(name = "extra", value = "扩展字段", required = true, dataType = "String")
    })
    @PostMapping(path = "send")
    public Result<SmsResponse> send(@RequestParam("phoneNumbers") final String phoneNumbers,
                                    @RequestParam("signCode") final String signCode,
                                    @RequestParam("templateCode") final String templateCode,
                                    @RequestParam("templateParam") final String templateParam,
                                    @RequestParam("extra") final String extra) throws Exception {
        final SmsRequest smsRequest = new SmsRequest();
        smsRequest.setPhoneNumbers(phoneNumbers);
        smsRequest.setSignCode(signCode);
        smsRequest.setTemplateCode(templateCode);
        smsRequest.setTemplateParam(templateParam);
        smsRequest.setExtra(extra);
        return Result.ok(this.smsService.send(smsRequest));
    }

    @PostMapping(path = "processSendingMsg")
    public Result<?> processSendingMsg() throws InterruptedException {
        this.smsService.processSendDetails();
        return Result.ok();
    }

    public static void main(String[] args) {
        mengWang();
    }

    private static void mengWang() {
        String uid = "JG8607";
        String pwd = "222110";

        // 创建全局参数
        GlobalParams globalParams = new GlobalParams();
        // 设置请求路径
        globalParams.setRequestPath("/sms/v2/std/");
        // 设置是否需要日志 1:需要日志;0:不需要日志
        globalParams.setNeedLog(1);
        // 设置全局参数
        ConfigManager.setGlobalParams(globalParams);

        // 设置用户账号信息
        // 返回值
        int result1 = -310007;
        try {
            // 设置用户账号信息
            result1 = ConfigManager.setAccountInfo(
                    uid,
                    pwd,
                    1,
                    "114.67.58.227:8901",
                    null,
                    null,
                    null);
            // 判断返回结果，0设置成功，否则失败
            if (result1 == 0) {
                System.out.println("设置用户账号信息成功！");
            } else {
                System.out.println("设置用户账号信息失败，错误码：" + result1);
            }
        } catch (Exception e) {
            // 异常处理
            e.printStackTrace();
        }

        // 实例化短信处理对象
        SmsSendConn smsSendConn = new SmsSendConn(false);
        try {
            // 参数类
            Message message = new Message();
            // 设置用户账号 指定用户账号发送，需要填写用户账号，不指定用户账号发送，无需填写用户账号
            message.setUserid(uid);
            // 设置手机号码 此处只能设置一个手机号码
            message.setMobile("18207131101");
            // 设置内容
            message.setContent("【司机宝】测试短信");
            // 自定义扩展数据
            message.setExdata("abcdef");
            // 业务类型
//            message.setSvrtype("SMS001");

            // 返回的流水号
            StringBuffer returnValue = new StringBuffer();
            // 返回值
            int result2 = -310099;
            // 发送短信
            result2 = smsSendConn.singleSend(message, returnValue);


            // result为0:成功
            if (result2 == 0) {
                System.out.println("单条发送提交成功！");
                System.out.println(returnValue.toString());
            }

            // result为非0：失败
            else {
                System.out.println("单条发送提交失败,错误码：" + result2);
            }

            new Thread(new RecvRptThread(smsSendConn, uid, 500)).start();

        } catch (Exception e) {
            // 异常处理
            e.printStackTrace();
        }
    }

}
