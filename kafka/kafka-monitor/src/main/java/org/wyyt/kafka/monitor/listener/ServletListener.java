package org.wyyt.kafka.monitor.listener;

import org.springframework.stereotype.Component;
import org.wyyt.kafka.monitor.service.dto.SchemaService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

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
public class ServletListener implements ServletContextListener {
    private final SchemaService schemaService;

    public ServletListener(final SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        this.schemaService.createTableIfNotExists();
    }
}