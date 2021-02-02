package org.wyyt.springcloud.gateway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.wyyt.tool.dingtalk.DingTalkTool;
import org.wyyt.tool.dingtalk.Message;

/**
 * The service of Ding Talk
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class DingTalkService {
    @Value("${dingtalk_enabled}")
    private Boolean enabled;
    @Value("${dingtalk_accessToken}")
    private String accessToken;
    @Value("${dingtalk_secret}")
    private String secret;

    public void send(final Message message) throws Exception {
        if (!this.enabled) {
            return;
        }
        DingTalkTool.send(message, this.accessToken, this.secret);
    }

}
