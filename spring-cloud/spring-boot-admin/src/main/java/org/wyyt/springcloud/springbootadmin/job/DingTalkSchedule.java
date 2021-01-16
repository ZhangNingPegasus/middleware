package org.wyyt.springcloud.springbootadmin.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wyyt.springcloud.springbootadmin.notifier.DingTalkNotifier;
import org.wyyt.tool.dingtalk.DingTalkTool;
import org.wyyt.tool.dingtalk.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * The schedule job for providing an alert when a problem is detected.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
@Component
public class DingTalkSchedule {
    @Value("${dingtalk.enabled}")
    private Boolean enabled;
    @Value("${dingtalk.accessToken}")
    private String accessToken;
    @Value("${dingtalk.secret}")
    private String secret;

    private final DingTalkNotifier dingTalkNotifier;
    private final List<Message> messageList;

    public DingTalkSchedule(final DingTalkNotifier dingTalkNotifier) {
        this.dingTalkNotifier = dingTalkNotifier;
        this.messageList = new ArrayList<>(DingTalkNotifier.MAX_SIZE);
    }

    //每5秒执行一次
    @Scheduled(cron = "0/5 * * * * ?")
    public void alert() throws Exception {
        this.messageList.clear();
        this.dingTalkNotifier.getToProcessRecords().drainTo(messageList, 20);

        if (!this.messageList.isEmpty() && this.enabled) {
            final Message message = new Message();
            message.setMsgtype("text");

            final StringBuilder content = new StringBuilder();
            for (final Message one : this.messageList) {
                content.append(one.getText().getContent());
                content.append("\n");
                content.append("\n");
            }
            message.setText(new Message.Text(content.toString()));
            DingTalkTool.send(message, this.accessToken, this.secret);
        }
    }
}