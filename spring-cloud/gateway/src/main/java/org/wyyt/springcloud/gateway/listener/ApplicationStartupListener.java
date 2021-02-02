package org.wyyt.springcloud.gateway.listener;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.wyyt.springcloud.gateway.service.DataService;
import org.wyyt.springcloud.gateway.service.DynamicRouteService;

/**
 * The servlet context listener
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Component
public class ApplicationStartupListener implements ApplicationRunner {

    private final DynamicRouteService dynamicRouteService;
    private final DataService dataService;

    public ApplicationStartupListener(final DynamicRouteService dynamicRouteService,
                                      final DataService dataService) {
        this.dynamicRouteService = dynamicRouteService;
        this.dataService = dataService;
    }

    @Override
    public void run(ApplicationArguments args) {
        this.dataService.getIgnoreUrlSet();
        this.dynamicRouteService.refresh();
    }
}