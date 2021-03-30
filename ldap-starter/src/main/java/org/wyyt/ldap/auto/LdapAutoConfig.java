package org.wyyt.ldap.auto;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.ldap.LdapProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.wyyt.ldap.service.LdapService;

import java.util.HashMap;
import java.util.Map;

/**
 * The auto configuration of ldap
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Configuration
@EnableConfigurationProperties({LdapProperties.class, LdapCustomProperty.class})
@ConditionalOnProperty(name = "admin.ui.login-mode", havingValue = "LDAP")
public class LdapAutoConfig {
    private final LdapProperties ldapProperties;
    private final Environment environment;

    public LdapAutoConfig(final LdapProperties ldapProperties,
                          final Environment environment) {
        this.ldapProperties = ldapProperties;
        this.environment = environment;
    }

    @Bean
    @Primary
    public LdapContextSource contextSource() {
        final LdapContextSource contextSource = new LdapContextSource();
        final Map<String, Object> config = new HashMap();
        config.put("java.naming.ldap.attributes.binary", "objectGUID");
        contextSource.setCacheEnvironmentProperties(false);
        contextSource.setUrls(this.ldapProperties.determineUrls(this.environment));
        contextSource.setUserDn(this.ldapProperties.getUsername());
        contextSource.setPassword(this.ldapProperties.getPassword());
        contextSource.setPooled(true);
        contextSource.setBaseEnvironmentProperties(config);
        return contextSource;
    }

    @Bean
    @Primary
    public LdapTemplate ldapTemplate(final ContextSource contextSource) {
        final LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
        ldapTemplate.setIgnorePartialResultException(true);
        return ldapTemplate;
    }

    @Bean
    @Primary
    public LdapService ldapService(final LdapTemplate ldapTemplate,
                                   final LdapProperties ldapProperties,
                                   final LdapCustomProperty ldapCustomProperty) {
        return new LdapService(ldapTemplate, ldapProperties, ldapCustomProperty);
    }
}
