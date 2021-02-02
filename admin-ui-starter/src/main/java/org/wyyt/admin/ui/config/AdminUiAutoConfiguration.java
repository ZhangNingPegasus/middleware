package org.wyyt.admin.ui.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.wyyt.tool.db.CrudService;

import javax.sql.DataSource;

/**
 * Providing the CRUD of database
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@EnableConfigurationProperties({AdminUiProperties.class})
public class AdminUiAutoConfiguration {

    @Bean(name = "adminUiCrudService")
    public CrudService crudService(final DataSource dataSource) {
        return new CrudService(dataSource);
    }
}