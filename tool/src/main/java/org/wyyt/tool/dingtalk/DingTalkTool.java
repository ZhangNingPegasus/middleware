package org.wyyt.tool.dingtalk;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.wyyt.tool.date.DateTool;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * the common functions of DingTalk
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class DingTalkTool {
    private static final String URL = "https://oapi.dingtalk.com/robot/send?access_token=%s&timestamp=%s&sign=%s";

    public static void send(final Message message,
                            final String accessToken,
                            final String secret) throws Exception {
        Assert.isTrue(!ObjectUtils.isEmpty(accessToken), "钉钉机器人的Access Token不允许为空");
        Assert.isTrue(!ObjectUtils.isEmpty(secret), "钉钉机器人的secret不允许为空");

        final Long timestamp = System.currentTimeMillis();
        final String stringToSign = timestamp + "\n" + secret;
        final Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        final byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        final String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), StandardCharsets.UTF_8.name());
        final String WEBHOOK_TOKEN = String.format(URL, accessToken, timestamp, sign);
        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
            final HttpPost httppost = new HttpPost(WEBHOOK_TOKEN);
            httppost.addHeader("Content-Type", String.format("application/json; charset=%s", StandardCharsets.UTF_8.name()));
            final StringEntity se = new StringEntity(JSON.toJSONString(message), StandardCharsets.UTF_8.name());
            httppost.setEntity(se);
            final HttpResponse response = httpclient.execute(httppost);
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                String responseText = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                final JSONObject jsonObject = new JSONObject(responseText);
                int errcode = jsonObject.getInt("errcode");
                if (0 != errcode) {
                    throw new RuntimeException(String.format("钉钉消息发送失败, 原因: %s. 消息: %s; access_token:%s; secret:%s",
                            jsonObject.getString("errmsg"),
                            message,
                            accessToken,
                            secret));
                }
            }
        }
    }

    public static void prettySend(final String content,
                                  @Nullable final String hostName,
                                  @Nullable final String host,
                                  @Nullable final List<String> mobileList,
                                  final WarningLevel warningLevel,
                                  final String accessToken,
                                  final String secret) throws Exception {

        final Message message = new Message();
        message.setMsgtype("text");

        final StringBuilder stringBuilder = new StringBuilder();
        if (!ObjectUtils.isEmpty(hostName)) {
            stringBuilder.append(String.format("告警主机：%s\n", hostName));
        }
        if (!ObjectUtils.isEmpty(host)) {
            stringBuilder.append(String.format("主机地址：%s\n", host));
        }
        stringBuilder.append(String.format("告警等级：%s\n", warningLevel.getDescription()));
        stringBuilder.append("当前状态：OK\n");
        stringBuilder.append(String.format("问题详情：%s\n", content));
        stringBuilder.append(String.format("告警时间：%s", DateTool.format(new Date())));
        message.setText(new Message.Text(stringBuilder.toString()));
        message.setAt(new Message.At(mobileList, (mobileList == null || mobileList.isEmpty())));
        send(message, accessToken, secret);
    }

    public static void prettySend(final String content,
                                  final String hostName,
                                  final String host,
                                  final WarningLevel warningLevel,
                                  final String accessToken,
                                  final String secret) throws Exception {
        prettySend(content, hostName, host, null, warningLevel, accessToken, secret);
    }

    public static void prettySend(final String content,
                                  final WarningLevel warningLevel,
                                  final String accessToken,
                                  final String secret) throws Exception {
        prettySend(content, null, null, null, warningLevel, accessToken, secret);
    }
}