package org.wyyt.apollo.tool;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * the common functions of Apollo
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public class ApolloReader {
    private final Config config;

    public ApolloReader(final String apolloMeta,
                        final String appId) {
        final Map<String, String> result = new HashMap<>();
        if (!StringUtils.isEmpty(apolloMeta)) {
            System.setProperty("apollo.meta", apolloMeta);
        } else {
            if (StringUtils.isEmpty(System.getenv("apollo_meta"))) {
                System.setProperty("apollo.meta", "http://apolloconfig.dev.wyyt:8640/");
            }
        }
        System.setProperty("app.id", appId);
        System.setProperty("apollo.bootstrap.enabled", "true");
        System.setProperty("apollo.bootstrap.eagerLoad.enabled", "true");
        this.config = ConfigService.getAppConfig();
    }

    public ApolloReader(final String appId) {
        this(null, appId);
    }

    public Map<String, String> getProperties() {
        final Set<String> propertyNames = this.config.getPropertyNames();
        if (null == propertyNames) {
            return null;
        }
        final Map<String, String> result = new HashMap<>((int) (0.75 / propertyNames.size()));
        for (final String propertyName : propertyNames) {
            result.put(propertyName, config.getProperty(propertyName, ""));
        }
        return result;
    }

    public String getProperty(final String key) {
        return this.config.getProperty(key, "");
    }
}