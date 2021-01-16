package org.wyyt.kafka.monitor.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.wyyt.kafka.monitor.alert.AlertService;
import org.wyyt.kafka.monitor.entity.dto.SysAlertCluster;
import org.wyyt.kafka.monitor.entity.po.Alert;
import org.wyyt.kafka.monitor.entity.po.ZooKeeperKpi;
import org.wyyt.kafka.monitor.entity.vo.KafkaBrokerVo;
import org.wyyt.kafka.monitor.service.common.KafkaService;
import org.wyyt.kafka.monitor.service.dto.SysAlertClusterService;
import org.wyyt.kafka.monitor.util.ZooKeeperKpiUtil;

import java.util.List;
import java.util.stream.Collectors;

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
public class ClusterSchedule {
    private final SysAlertClusterService sysAlertClusterService;
    private final KafkaService kafkaService;
    private final AlertService alertService;

    public ClusterSchedule(final SysAlertClusterService sysAlertClusterService,
                           final KafkaService kafkaService,
                           final AlertService alertService) {
        this.sysAlertClusterService = sysAlertClusterService;
        this.kafkaService = kafkaService;
        this.alertService = alertService;
    }

    @Scheduled(cron = "0 0/2 * * * ?") //每分钟执行一次
    public void checkCluster() throws Exception {
        final List<SysAlertCluster> list = this.sysAlertClusterService.list();
        if (list == null || list.isEmpty()) {
            return;
        }
        final List<KafkaBrokerVo> KafkaBrokerVofoList = this.kafkaService.listBrokerInfos();
        final List<SysAlertCluster> zooKeepers = list.stream().filter(p -> p.getType().equals(SysAlertCluster.Type.ZOOKEEPER.getCode())).collect(Collectors.toList());
        final List<SysAlertCluster> kafkas = list.stream().filter(p -> p.getType().equals(SysAlertCluster.Type.KAFKA.getCode())).collect(Collectors.toList());

        for (final SysAlertCluster zooKeeper : zooKeepers) {
            final String[] split = zooKeeper.getServer().split(":");
            final String ip = split[0];
            final int port = Integer.parseInt(split[1]);

            final ZooKeeperKpi zooKeeperKpi = ZooKeeperKpiUtil.listKpi(ip, port);
            if (ObjectUtils.isEmpty(zooKeeperKpi.getZkNumAliveConnections())) {
                final Alert alert = new Alert();
                alert.setEmail(zooKeeper.getEmail());
                alert.setEmailTitle(String.format("ZOOKEEPER主机[%s]不可用, 请检查.", ip));
                alert.setEmailContent(alert.getEmailTitle());

                alert.setDingContent(alert.getEmailContent());
                alert.setDingAccessToken(zooKeeper.getAccessToken());
                alert.setDingSecret(zooKeeper.getSecret());
                this.alertService.offer(String.format("alert_zookeeper_%s", zooKeeper.getId()), alert);
            }
        }

        for (final SysAlertCluster kafka : kafkas) {
            final String[] split = kafka.getServer().split(":");
            final String ip = split[0];
            final String port = split[1];
            final List<KafkaBrokerVo> result = KafkaBrokerVofoList.stream().filter(p -> p.getHost().equals(ip) && p.getPort().equals(port)).collect(Collectors.toList());
            if (result.isEmpty()) {
                final Alert alert = new Alert();
                alert.setEmail(kafka.getEmail());
                alert.setEmailTitle(String.format("KAFKA主机[%s]不可用, 请检查.", ip));
                alert.setEmailContent(alert.getEmailTitle());

                alert.setDingContent(alert.getEmailContent());
                alert.setDingAccessToken(kafka.getAccessToken());
                alert.setDingSecret(kafka.getSecret());
                this.alertService.offer(String.format("alert_kafka_%s", kafka.getId()), alert);
            }
        }
    }
}