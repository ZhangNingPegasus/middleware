package org.wyyt.kafka.monitor.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wyyt.kafka.monitor.template.*;

/**
 * FreeMarker's configuration
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Configuration
public class FreeMarkerConfig implements InitializingBean {
    private final freemarker.template.Configuration freeMarkerConfiguration;

    public FreeMarkerConfig(final freemarker.template.Configuration freeMarkerConfiguration) {
        this.freeMarkerConfiguration = freeMarkerConfiguration;
    }

    @Bean
    public InsertDirective insertDirective() {
        return new InsertDirective();
    }

    @Bean
    public NoInsertDirective noInsertDirective() {
        return new NoInsertDirective();
    }

    @Bean
    public DeleteDirective deleteDirective() {
        return new DeleteDirective();
    }

    @Bean
    public NoDeleteDirective noDeleteDirective() {
        return new NoDeleteDirective();
    }

    @Bean
    public UpdateDirective updateDirective() {
        return new UpdateDirective();
    }

    @Bean
    public NoUpdateDirective noUpdateDirective() {
        return new NoUpdateDirective();
    }

    @Bean
    public SelectDirective selectDirective() {
        return new SelectDirective();
    }

    @Bean
    public NoSelectDirective noSelectDirective() {
        return new NoSelectDirective();
    }

    @Bean
    public OnlySelectDirective onlySelectDirective() {
        return new OnlySelectDirective();
    }

    @Bean
    public NotOnlySelectDirective notOnlySelectDirective() {
        return new NotOnlySelectDirective();
    }

    @Override
    public void afterPropertiesSet() {
        this.freeMarkerConfiguration.setSharedVariable("insert", insertDirective());
        this.freeMarkerConfiguration.setSharedVariable("delete", deleteDirective());
        this.freeMarkerConfiguration.setSharedVariable("update", updateDirective());
        this.freeMarkerConfiguration.setSharedVariable("select", selectDirective());

        this.freeMarkerConfiguration.setSharedVariable("no_insert", noInsertDirective());
        this.freeMarkerConfiguration.setSharedVariable("no_delete", noDeleteDirective());
        this.freeMarkerConfiguration.setSharedVariable("no_update", noUpdateDirective());
        this.freeMarkerConfiguration.setSharedVariable("no_select", noSelectDirective());

        this.freeMarkerConfiguration.setSharedVariable("only_select", onlySelectDirective());

        this.freeMarkerConfiguration.setSharedVariable("not_only_select", notOnlySelectDirective());
    }
}