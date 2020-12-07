package org.wyyt.kafka.monitor.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wyyt.kafka.monitor.alert.AlertService;
import org.wyyt.kafka.monitor.entity.dto.SysAlertConsumer;
import org.wyyt.kafka.monitor.entity.dto.SysAlertTopic;
import org.wyyt.kafka.monitor.entity.dto.SysTopicLag;
import org.wyyt.kafka.monitor.entity.dto.SysTopicSize;
import org.wyyt.kafka.monitor.entity.po.Alert;
import org.wyyt.kafka.monitor.entity.po.TopicSizeLag;
import org.wyyt.kafka.monitor.service.common.KafkaService;
import org.wyyt.kafka.monitor.service.dto.SysAlertConsumerService;
import org.wyyt.kafka.monitor.service.dto.SysAlertTopicService;
import org.wyyt.kafka.monitor.service.dto.SysTopicLagService;
import org.wyyt.kafka.monitor.service.dto.SysTopicSizeService;
import org.wyyt.tool.date.DateTool;
import org.wyyt.tool.exception.ExceptionTool;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * The schedule job for collection log size of topics.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Slf4j
@Component
public class LogSizeSchedule {
    private final KafkaService kafkaService;
    private final SysTopicSizeService sysTopicSizeService;
    private final SysTopicLagService sysTopicLagService;
    private final SysAlertConsumerService sysAlertConsumerService;
    private final SysAlertTopicService sysAlertTopicService;
    private final AlertService alertService;

    public LogSizeSchedule(final KafkaService kafkaService,
                           final SysTopicSizeService sysTopicSizeService,
                           final SysTopicLagService sysTopicLagService,
                           final SysAlertConsumerService sysAlertConsumerService,
                           final SysAlertTopicService sysAlertTopicService,
                           final AlertService alertService) {
        this.kafkaService = kafkaService;
        this.sysTopicSizeService = sysTopicSizeService;
        this.sysTopicLagService = sysTopicLagService;
        this.sysAlertConsumerService = sysAlertConsumerService;
        this.sysAlertTopicService = sysAlertTopicService;
        this.alertService = alertService;
    }

    //每10分钟执行一次
    @Scheduled(cron = "0 0/10 * * * ?")
    public void collect() throws Exception {
        final Date now = new Date();
        final TopicSizeLag topicSizeLag = this.sysTopicSizeService.kpi(now);
        String kafkaUrl = kafkaService.getBootstrapServers(false);
        final CountDownLatch cdl = new CountDownLatch(4);

        new Thread(() -> {
            try {
                this.sysTopicSizeService.saveBatch(topicSizeLag.getSysTopicSizeList());
            } catch (final Exception e) {
                log.error(ExceptionTool.getRootCauseMessage(e), e);
            } finally {
                cdl.countDown();
            }
        }).start();

        new Thread(() -> {
            try {
                this.sysTopicLagService.saveBatch(topicSizeLag.getSysTopicLagList());
            } catch (final Exception e) {
                log.error(ExceptionTool.getRootCauseMessage(e), e);
            } finally {
                cdl.countDown();
            }
        }).start();

        new Thread(() -> {
            try {
                lagAlert(topicSizeLag, now, kafkaUrl);
            } catch (final Exception e) {
                log.error(ExceptionTool.getRootCauseMessage(e), e);
            } finally {
                cdl.countDown();
            }
        }).start();

        new Thread(() -> {
            try {
                tpsAlert(kafkaUrl);
            } catch (final Exception e) {
                log.error(ExceptionTool.getRootCauseMessage(e), e);
            } finally {
                cdl.countDown();
            }
        }).start();

        cdl.await();
    }

    private void lagAlert(final TopicSizeLag topicSizeLag,
                          final Date now,
                          final String kafkaUrl) {
        final List<SysAlertConsumer> sysAlertConsumerList = this.sysAlertConsumerService.list();
        if (null == sysAlertConsumerList || sysAlertConsumerList.isEmpty()) {
            return;
        }

        for (final SysAlertConsumer sysAlertConsumer : sysAlertConsumerList) {
            final String consumerName = sysAlertConsumer.getGroupId();
            final String topicName = sysAlertConsumer.getTopicName();

            final List<SysTopicLag> sysTopicLagList = topicSizeLag.getSysTopicLagList().stream().filter(p -> p.getGroupId().equals(consumerName) && p.getTopicName().equals(topicName)).collect(Collectors.toList());

            for (final SysTopicLag sysTopicLag : sysTopicLagList) {
                if (sysTopicLag.getLag() > sysAlertConsumer.getLagThreshold()) {
                    final Alert alert = new Alert();
                    alert.setEmail(sysAlertConsumer.getEmail());
                    alert.setEmailTitle(String.format("消费组[%s]订阅的主题[%s]堆积的消息量已超过阀值%s, 现有积压消息量%s", consumerName, topicName, sysAlertConsumer.getLagThreshold(), sysTopicLag.getLag()));
                    alert.setEmailContent("告警主机：" + kafkaUrl + "<br/>" +
                            "告警等级：警告<br/>" +
                            "当前状态：OK<br/>" +
                            "问题详情：" + alert.getEmailTitle() + "<br/>" +
                            "告警时间：" + DateTool.format(now) + "<br/>");
                    alert.setDingContent("告警主机：" + kafkaUrl + "\n" +
                            "告警等级：警告\n" +
                            "当前状态：OK\n" +
                            "问题详情：" + alert.getEmailTitle() + "\n" +
                            "告警时间：" + DateTool.format(now) + "\n");
                    alert.setDingAccessToken(sysAlertConsumer.getAccessToken());
                    alert.setDingSecret(sysAlertConsumer.getSecret());
                    this.alertService.offer(String.format("alert_lag_topic_%s", sysAlertConsumer.getId()), alert);
                }
            }
        }
    }

    private void tpsAlert(final String kafkaUrl) {
        final List<SysAlertTopic> sysAlertTopicList = sysAlertTopicService.list();
        if (null == sysAlertTopicList || sysAlertTopicList.isEmpty()) {
            return;
        }
        final Date now = new Date();
        final Date from = DateUtils.addMinutes(now, -4);
        final Map<String, List<SysTopicSize>> sysLogSizeMap = this.sysTopicSizeService.listByTopicNames(sysAlertTopicList.stream().map(SysAlertTopic::getTopicName).collect(Collectors.toList()), from, now);

        if (null == sysLogSizeMap || sysLogSizeMap.isEmpty()) {
            return;
        }

        for (final SysAlertTopic sysAlertTopic : sysAlertTopicList) {
            final Date start = DateTool.parse(sysAlertTopic.getFromTime());
            final Date end = DateTool.parse(sysAlertTopic.getToTime());

            final String topicName = sysAlertTopic.getTopicName();

            if (!sysLogSizeMap.containsKey(topicName)) {
                final Alert alert = new Alert();
                alert.setEmail(sysAlertTopic.getEmail());
                alert.setEmailTitle(String.format("主题[%s]设置了TPS警告, %s, 但目前没有检测到任何数据", topicName, sysAlertTopic.toInfo()));
                alert.setEmailContent("告警主机：Kafka集群<br/>" +
                        "主机地址：" + kafkaUrl + "<br/>" +
                        "告警等级：警告<br/>" +
                        "当前状态：OK<br/>" +
                        "问题详情：" + alert.getEmailTitle() + "<br/>" +
                        "告警时间：" + DateTool.format(now) + "<br/>");
                alert.setDingContent("告警主机：Kafka集群\n" +
                        "主机地址：" + kafkaUrl + "\n" +
                        "告警等级：警告\n" +
                        "当前状态：OK\n" +
                        "问题详情：" + alert.getEmailTitle() + "\n" +
                        "告警时间：" + DateTool.format(now) + "\n");
                alert.setDingAccessToken(sysAlertTopic.getAccessToken());
                alert.setDingSecret(sysAlertTopic.getSecret());
                this.alertService.offer(String.format("alert_no_data_topic_%s", sysAlertTopic.getId()), alert);
                continue;
            }
            final List<SysTopicSize> sysLogSizeList = sysLogSizeMap.get(topicName);
            if ((null != start && sysLogSizeList.get(0).getRowCreateTime().before(start)) || (end != null && sysLogSizeList.get(0).getRowCreateTime().after(end))) {
                continue;
            }
            Integer tps = null;
            Integer momTps = null;
            if (sysLogSizeList.size() > 1) {
                final SysTopicSize s2 = sysLogSizeList.get(0);
                final SysTopicSize s1 = sysLogSizeList.get(1);
                tps = (int) ((s2.getLogSize() - s1.getLogSize()) / ((s2.getRowCreateTime().getTime() - s1.getRowCreateTime().getTime()) / 1000.0));
            }

            if (sysLogSizeList.size() > 2) {
                final SysTopicSize s3 = sysLogSizeList.get(0);
                final SysTopicSize s2 = sysLogSizeList.get(1);
                final SysTopicSize s1 = sysLogSizeList.get(2);
                final int a = (int) ((s3.getLogSize() - s2.getLogSize()) / ((s3.getRowCreateTime().getTime() - s2.getRowCreateTime().getTime()) / 1000.0));
                final int b = (int) ((s2.getLogSize() - s1.getLogSize()) / ((s2.getRowCreateTime().getTime() - s1.getRowCreateTime().getTime()) / 1000.0));
                momTps = a - b;
            }

            boolean tpsAlert = false;
            boolean momTpsAlert = false;
            if (null != tps && null != sysAlertTopic.getFromTps()) {
                if (tps < sysAlertTopic.getFromTps()) {
                    tpsAlert = true;
                }
            }

            if (null != tps && null != sysAlertTopic.getToTps()) {
                if (tps > sysAlertTopic.getToTps()) {
                    tpsAlert = true;
                }
            }
            if (null != momTps && null != sysAlertTopic.getFromMomTps()) {
                if (momTps < sysAlertTopic.getFromMomTps()) {
                    momTpsAlert = true;
                }
            }
            if (null != momTps && null != sysAlertTopic.getToMomTps()) {
                if (momTps > sysAlertTopic.getToMomTps()) {
                    momTpsAlert = true;
                }
            }

            if (tpsAlert) {
                final Alert alert = new Alert();
                alert.setEmail(sysAlertTopic.getEmail());
                alert.setEmailTitle(String.format("主题[%s]当前的TPS是: %s, 不满足设定: %s", topicName, tps, sysAlertTopic.toTpsInfo()));
                alert.setEmailContent("告警主机：Kafka集群<br/>" +
                        "主机地址：" + kafkaUrl + "<br/>" +
                        "告警等级：警告<br/>" +
                        "当前状态：OK<br/>" +
                        "问题详情：" + alert.getEmailTitle() + "<br/>" +
                        "告警时间：" + DateTool.format(now) + "<br/>");
                alert.setDingContent("告警主机：Kafka集群\n" +
                        "主机地址：" + kafkaUrl + "\n" +
                        "告警等级：警告\n" +
                        "当前状态：OK\n" +
                        "问题详情：" + alert.getEmailTitle() + "\n" +
                        "告警时间：" + DateTool.format(now) + "\n");
                alert.setDingAccessToken(sysAlertTopic.getAccessToken());
                alert.setDingSecret(sysAlertTopic.getSecret());
                this.alertService.offer(String.format("alert_tps_topic_%s", sysAlertTopic.getId()), alert);
            }

            if (momTpsAlert) {
                final Alert alert = new Alert();
                alert.setEmail(sysAlertTopic.getEmail());
                alert.setEmailTitle(String.format("主题[%s]当前的TPS变化是: %s, 不满足设定: %s", topicName, momTps, sysAlertTopic.toMomTpsInfo()));
                alert.setEmailContent("告警主机：Kafka集群<br/>" +
                        "主机地址：" + kafkaUrl + "<br/>" +
                        "告警等级：警告<br/>" +
                        "当前状态：OK<br/>" +
                        "问题详情：" + alert.getEmailTitle() + "<br/>" +
                        "告警时间：" + DateTool.format(now) + "<br/>");
                alert.setDingContent("告警主机：Kafka集群\n" +
                        "主机地址：" + kafkaUrl + "\n" +
                        "告警等级：警告\n" +
                        "当前状态：OK\n" +
                        "问题详情：" + alert.getEmailTitle() + "\n" +
                        "告警时间：" + DateTool.format(now) + "\n");
                alert.setDingAccessToken(sysAlertTopic.getAccessToken());
                alert.setDingSecret(sysAlertTopic.getSecret());
                this.alertService.offer(String.format("alert_mon_tps_topic_%s", sysAlertTopic.getId()), alert);
            }
        }
    }
}