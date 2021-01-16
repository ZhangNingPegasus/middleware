package org.wyyt.kafka.monitor.service.common;

import org.springframework.stereotype.Service;
import org.wyyt.kafka.monitor.entity.dto.SysMailConfig;
import org.wyyt.kafka.monitor.service.dto.SysMailConfigService;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * The service for sending email.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class MailService {

    private final SysMailConfigService sysMailConfigService;

    public MailService(final SysMailConfigService sysMailConfigService) {
        this.sysMailConfigService = sysMailConfigService;
    }

    public void send(final String to,
                     final String subject,
                     final String html) throws Exception {
        final SysMailConfig sysMailConfig = sysMailConfigService.get();
        if (null == sysMailConfig) {
            return;
        }

        final Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", sysMailConfig.getHost().trim());
        props.setProperty("mail.port", sysMailConfig.getPort().trim());
        props.setProperty("mail.smtp.auth", "true");
        final Session session = Session.getInstance(props);
        session.setDebug(false);
        final MimeMessage message = new MimeMessage(session);

        //设置邮件的头
        message.setFrom(new InternetAddress(sysMailConfig.getUsername()));
        message.setRecipients(Message.RecipientType.TO, new Address[]{new InternetAddress(to)});

        message.setSubject(subject, "UTF-8");
        //设置正文
        message.setContent(html, "text/html;charset=utf-8");
        message.saveChanges();

        //发送邮件
        final Transport ts = session.getTransport();
        ts.connect(sysMailConfig.getUsername(), sysMailConfig.getPassword());
        ts.sendMessage(message, message.getAllRecipients());
    }
}