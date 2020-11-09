package org.wyyt.kafka.monitor.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wyyt.kafka.monitor.entity.dto.SysKpi;
import org.wyyt.kafka.monitor.service.common.KafkaService;
import org.wyyt.kafka.monitor.service.common.KafkaZkService;
import org.wyyt.kafka.monitor.service.dto.SysKpiService;
import org.wyyt.tool.exception.ExceptionTool;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * The schedule job for collection kpi of kafka and zookeeper.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Slf4j
@Component
public class KpiSchedule {
    private final KafkaZkService kafkaZkService;
    private final KafkaService kafkaService;
    private final SysKpiService sysKpiService;

    public KpiSchedule(final KafkaZkService kafkaZkService,
                       final KafkaService kafkaService,
                       final SysKpiService sysKpiService) {
        this.kafkaZkService = kafkaZkService;
        this.kafkaService = kafkaService;
        this.sysKpiService = sysKpiService;
    }

    //每10分钟执行一次
    @Scheduled(cron = "0 0/5 * * * ?")
    public void collect() throws InterruptedException {
        final Date now = new Date();
        final List<SysKpi> sysKpiList = new ArrayList<>(32);
        final CountDownLatch cdl = new CountDownLatch(2);
        new Thread(() -> {
            try {
                sysKpiList.addAll(this.kafkaZkService.kpi(now));
            } catch (final Exception exception) {
                log.error(ExceptionTool.getRootCauseMessage(exception), exception);
            } finally {
                cdl.countDown();
            }
        }).start();

        new Thread(() -> {
            try {
                sysKpiList.addAll(this.kafkaService.kpi(now));
            } catch (final Exception exception) {
                log.error(ExceptionTool.getRootCauseMessage(exception), exception);
            } finally {
                cdl.countDown();
            }
        }).start();
        cdl.await();
        this.sysKpiService.saveBatch(sysKpiList);
    }
}