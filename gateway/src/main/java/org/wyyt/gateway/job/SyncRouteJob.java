package org.wyyt.gateway.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wyyt.gateway.service.DynamicRouteService;

/**
 * The job for sync routes information from database
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Component
public class SyncRouteJob {
    private final DynamicRouteService dynamicRouteService;

    public SyncRouteJob(final DynamicRouteService dynamicRouteService) {
        this.dynamicRouteService = dynamicRouteService;
    }

    @Scheduled(cron = "0 0/10 * * * ?")
    public void syncRoute() {
        System.out.println("OK");
        this.dynamicRouteService.refresh();
    }
}
