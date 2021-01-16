package org.wyyt.sharding.db2es.client.http;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * the parameter of RestController
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@AllArgsConstructor
public class Param {
    @Getter
    private final Map<String, Object> getParameters;
    @Getter
    private final Map<String, Object> postParameters;

    public final String getPostString(final String key) {
        final Object result = this.postParameters.get(key);
        if (null == result) {
            return null;
        } else {
            return result.toString();
        }
    }
}