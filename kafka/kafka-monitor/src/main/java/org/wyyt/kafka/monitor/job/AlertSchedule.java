package org.wyyt.kafka.monitor.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.wyyt.kafka.monitor.alert.AlertService;
import org.wyyt.kafka.monitor.entity.po.Alert;
import org.wyyt.kafka.monitor.service.common.MailService;
import org.wyyt.tool.dingtalk.DingTalkTool;
import org.wyyt.tool.dingtalk.Message;
import org.wyyt.tool.exception.ExceptionTool;

import java.util.Collections;
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
public class AlertSchedule {
    private final MailService mailService;
    private final AlertService alertService;

    public AlertSchedule(final MailService mailService,
                         final AlertService alertService) {
        this.mailService = mailService;
        this.alertService = alertService;
    }

    //每10秒执行一次
    @Scheduled(cron = "0/10 * * * * ?")
    public void alert() {
        final List<Alert> alertList = this.alertService.getAll();
        for (final Alert alert : alertList) {
            if (null == alert) {
                continue;
            }
            if (!ObjectUtils.isEmpty(alert.getEmail())) {
                try {
                    this.mailService.send(alert.getEmail(), alert.getEmailTitle(), alert.getEmailContent());
                } catch (final Exception exception) {
                    log.error(ExceptionTool.getRootCauseMessage(exception), exception);
                }
            }
            if (!ObjectUtils.isEmpty(alert.getDingContent())) {
                try {
                    Message message = new Message();
                    message.setMsgtype("text");
                    message.setText(new Message.Text(alert.getDingContent()));
                    message.setAt(new Message.At(Collections.singletonList(""), true));
                    DingTalkTool.send(message, alert.getDingAccessToken(), alert.getDingSecret());
                } catch (final Exception exception) {
                    log.error(ExceptionTool.getRootCauseMessage(exception), exception);
                }
            }
        }
    }
}