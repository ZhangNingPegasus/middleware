package org.wyyt.springcloud.auth.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.redis.service.RedisService;
import org.wyyt.springcloud.auth.config.PropertyConfig;
import org.wyyt.springcloud.entity.constants.Names;
import org.wyyt.springcloud.gateway.entity.contants.Constant;
import org.wyyt.springcloud.gateway.entity.entity.App;
import org.wyyt.springcloud.gateway.entity.entity.enums.GrantType;
import org.wyyt.springcloud.gateway.entity.entity.vo.AccessToken;
import org.wyyt.springcloud.gateway.entity.service.AppService;
import org.wyyt.tool.exception.BusinessException;
import org.wyyt.tool.rpc.RpcService;

import java.util.HashMap;
import java.util.Map;

/**
 * The service of access token
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
@Service
public class AccessTokenService {
    private final RpcService rpcService;
    private final PropertyConfig propertyConfig;
    private final AppService appService;
    private final RedisService redisService;

    public AccessTokenService(final RpcService rpcService,
                              final PropertyConfig propertyConfig,
                              final AppService appService,
                              final RedisService redisService) {
        this.rpcService = rpcService;
        this.propertyConfig = propertyConfig;
        this.appService = appService;
        this.redisService = redisService;
    }

    public App getByClientId(final String clientId) {
        return this.appService.getByClientId(clientId);
    }

    public AccessToken getClientCredentialsToken(final String clientId,
                                                 final String clientSecret) {
        final App app = this.getByClientId(clientId);
        if (null == app || !app.getClientSecret().equals(clientSecret)) {
            throw new BusinessException(String.format("The clientId [%s] not match clientSecret [%s]", clientId, clientSecret));
        }
        return this.getClientCredentialsToken(app);
    }

    public AccessToken getClientCredentialsToken(final App app) {
        if (null == app) {
            return null;
        }
        final Map<String, Object> params = new HashMap<>();
        params.put(Names.CLIENT_ID, app.getClientId());
        params.put(Constant.CLIENT_SECRET, app.getClientSecret());
        params.put(Constant.GRANT_TYPE, GrantType.CLIENT_CREDENTIALS.getCode());
        final String response = this.rpcService.post(
                String.format("http://localhost:%s/%s", this.propertyConfig.getServerPort(), Constant.OAUTH_TOKEN),
                new HashMap<>(),
                params);
        final Map<String, Object> map = JSON.parseObject(response, new TypeReference<Map<String, Object>>() {
        });
        if (map.containsKey("error")) {
            throw new BusinessException(response);
        }
        final AccessToken result = new AccessToken();
        result.setAccessToken(map.get(StrUtil.toUnderlineCase(Names.ACCESS_TOKEN)).toString());
        result.setExpiresTime(Long.parseLong(map.get(Constant.EXPIRES_IN).toString()));  //单位:秒
        this.redisService.set(
                Constant.getAccessTokenRedisKey(app.getClientId()),
                result.getAccessToken(),
                result.getExpiresTime() * 1000);
        return result;
    }

    public boolean logoutClientCredentialsToken(final String clientId,
                                                final String accessToken) {
        if (ObjectUtils.isEmpty(clientId) || ObjectUtils.isEmpty(accessToken)) {
            return false;
        }

        final String key = Constant.getAccessTokenRedisKey(clientId);

        final String redisAccessToken = (String) this.redisService.get(key);
        if (ObjectUtils.isEmpty(redisAccessToken)) {
            return false;
        }

        if (redisAccessToken.equals(accessToken)) {
            this.redisService.del(key);
            return true;
        }
        return false;
    }
}