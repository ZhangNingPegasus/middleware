package org.wyyt.springcloud.auth.config;


import lombok.SneakyThrows;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * the enhancement of JWT token
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
public class JWTTokenEnhancer implements TokenEnhancer {
    @SneakyThrows
    @Override
    public OAuth2AccessToken enhance(final OAuth2AccessToken oAuth2AccessToken,
                                     final OAuth2Authentication oAuth2Authentication) {
        final Map<String, Object> info = new HashMap<>();
        info.put("date", new Date());
        ((DefaultOAuth2AccessToken) oAuth2AccessToken).setAdditionalInformation(info);
        return oAuth2AccessToken;
    }
}
