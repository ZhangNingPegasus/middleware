package org.wyyt.gateway.listener;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.wyyt.gateway.service.DynamicRouteService;

/**
 * The servlet context listener
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Component
public class ApplicationStartupListener implements ApplicationRunner {

    private final DynamicRouteService dynamicRouteService;

    public ApplicationStartupListener(final DynamicRouteService dynamicRouteService) {
        this.dynamicRouteService = dynamicRouteService;
    }

    @Override
    public void run(ApplicationArguments args) {
        this.dynamicRouteService.refresh();
    }
}