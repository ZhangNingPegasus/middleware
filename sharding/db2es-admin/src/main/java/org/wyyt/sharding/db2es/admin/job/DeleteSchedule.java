package org.wyyt.sharding.db2es.admin.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wyyt.sharding.db2es.admin.service.ErrorLogService;

/**
 * The schedule job for deleting the expired data in table error_log
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Component
public class DeleteSchedule {

    private final ErrorLogService errorLogService;

    public DeleteSchedule(final ErrorLogService errorLogService) {
        this.errorLogService = errorLogService;
    }

    //每天凌晨3点执行
    @Scheduled(cron = "0 0 3 * * ?")
    public void collect() {
        this.errorLogService.deleteExpired(30);
    }
}