package org.wyyt.springcloud.gateway.entity.utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.util.ObjectUtils;
import org.wyyt.springcloud.entity.constants.Names;
import org.wyyt.springcloud.gateway.entity.contants.Constant;

import java.nio.charset.StandardCharsets;

/**
 * The common function
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public class Common {
    public static String getClientIdFromAccessToken(final String accessToken) {
        if (ObjectUtils.isEmpty(accessToken)) {
            return null;
        }
        final Claims claims = Jwts.parser()
                .setSigningKey(Constant.JWT_SIGNING_KEY.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(accessToken)
                .getBody();
        return claims.getOrDefault(Names.CLIENT_ID, "").toString();
    }
}
