package org.wyyt.springcloud.gateway.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wyyt.springcloud.gateway.service.DynamicRouteService;

/**
 * The job for sync routes information from database
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
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
        this.dynamicRouteService.refresh();
    }
}
