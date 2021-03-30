package org.wyyt.ldap.auto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.ObjectUtils;

import java.util.Properties;

/**
 * The Context initializer
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public class LdapApplicationContextInitializer implements EnvironmentPostProcessor, Ordered {
    @Override
    public void postProcessEnvironment(final ConfigurableEnvironment configurableEnvironment,
                                       final SpringApplication springApplication) {
        if (null == configurableEnvironment || null == springApplication) {
            return;
        } else if (configurableEnvironment.getClass().getName().equals(org.springframework.core.env.StandardEnvironment.class.getName())) {
            return;
        }

        final Properties properties = new Properties();
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.main.allow-bean-definition-overriding",
                true);
        if (!properties.isEmpty()) {
            configurableEnvironment.getPropertySources().addFirst(new PropertiesPropertySource("springCloudApplicationProperties", properties));
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private void addDefaultConfig(final ConfigurableEnvironment configurableEnvironment,
                                  final Properties properties,
                                  final String name,
                                  final Object value) {
        try {
            final String oldProperty = configurableEnvironment.getProperty(name);
            if (ObjectUtils.isEmpty(oldProperty)) {
                properties.put(name, value);
            }
        } catch (final IllegalArgumentException ignored) {
        }
    }
}
