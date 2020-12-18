package org.wyyt.admin.ui.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.wyyt.tool.db.CrudService;

import javax.sql.DataSource;

@EnableConfigurationProperties({AdminUiProperties.class})
public class AdminUiAutoConfiguration {

    @Bean(name = "adminUiCrudService")
    public CrudService crudService(final DataSource dataSource) {
        return new CrudService(dataSource);
    }
}