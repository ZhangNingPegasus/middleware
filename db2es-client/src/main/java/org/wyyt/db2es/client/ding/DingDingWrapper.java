package org.wyyt.db2es.client.ding;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.wyyt.db2es.client.common.Context;
import org.wyyt.db2es.client.common.Utils;
import org.wyyt.tool.date.DateTool;
import org.wyyt.tool.exception.ExceptionTool;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.Closeable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * the wapper class of DingDing
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
public final class DingDingWrapper implements Closeable {
    private static final String URL = "https://oapi.dingtalk.com/robot/send?access_token=%s&timestamp=%s&sign=%s";
    private final Context context;
    private final Cache<String, Long> cache;

    public DingDingWrapper(final Context context) {
        this.context = context;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    public final String send(final Message message) throws Exception {
        Assert.isTrue(!StringUtils.isEmpty(this.context.getConfig().getDingAccessToken()), "钉钉机器人的Access Token不允许为空");
        Assert.isTrue(!StringUtils.isEmpty(this.context.getConfig().getDingSecret()), "钉钉机器人的secret不允许为空");

        final Long timestamp = System.currentTimeMillis();
        final String stringToSign = timestamp + "\n" + this.context.getConfig().getDingSecret();
        final Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(this.context.getConfig().getDingSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        final byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        final String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
        final String WEBHOOK_TOKEN = String.format(URL, this.context.getConfig().getDingAccessToken(), timestamp, sign);
        final HttpClient httpclient = HttpClients.createDefault();
        final HttpPost httppost = new HttpPost(WEBHOOK_TOKEN);
        httppost.addHeader("Content-Type", "application/json; charset=UTF-8");
        final StringEntity se = new StringEntity(JSON.toJSONString(message), "UTF-8");
        httppost.setEntity(se);
        final HttpResponse response = httpclient.execute(httppost);
        if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        }
        return null;
    }

    public final String send(final String content,
                             final List<String> mobileList,
                             final WarningLevel warningLevel) {
        try {
            final Message message = new Message();
            message.setMsgtype("text");
            message.setText(new Message.Text("告警主机：" + Utils.getLocalIp(this.context).getLocalName() + "\n" +
                    "主机地址：" + Utils.getLocalIp(this.context).getLocalIp() + "\n" +
                    "告警等级：" + warningLevel.getDescription() + "\n" +
                    "当前状态：OK\n" +
                    "问题详情：" + content + "\n" +
                    "告警时间：" + DateTool.format(new Date()) + "\n"));
            message.setAt(new Message.At(mobileList, (mobileList == null || mobileList.isEmpty())));
            return this.send(message);
        } catch (final Exception e) {
            log.error(String.format("DingDingWrapper: send message meet error, [%s]", ExceptionTool.getRootCauseMessage(e)), e);
        }
        return null;
    }

    public final String send(final String content,
                             final WarningLevel warningLevel) {
        try {
            return this.send(content, this.context.getConfig().getDingMobiles(), warningLevel);
        } catch (final Exception e) {
            log.error(String.format("DingDingWrapper: send message meet error, [%s]", ExceptionTool.getRootCauseMessage(e)), e);
        }
        return null;
    }

    public final void sendIfNoDuplicate(final String content,
                                        final WarningLevel warningLevel) {
        final Long ifPresent = this.cache.getIfPresent(content);
        if (null != ifPresent) {
            return;
        }
        final String respond = send(content, warningLevel);
        if (!StringUtils.isEmpty(respond)) {
            this.cache.put(content, 1L);
        }
    }

    @Override
    public final void close() {
        if (null != this.cache) {
            this.cache.invalidateAll();
            this.cache.cleanUp();
        }
    }
}