package org.wyyt.springcloud.auth.service;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.wyyt.redis.service.RedisService;
import org.wyyt.springcloud.auth.config.PropertyConfig;
import org.wyyt.springcloud.auth.constant.Name;
import org.wyyt.springcloud.auth.entity.ClientToken;
import org.wyyt.springcloud.auth.entity.GrantType;
import org.wyyt.springcloud.exception.BusinessException;
import org.wyyt.springcloud.gateway.entity.contants.Names;
import org.wyyt.springcloud.gateway.entity.entity.App;
import org.wyyt.springcloud.gateway.entity.service.AppService;
import org.wyyt.tool.rpc.RpcTool;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ClientTokenService {
    private final RpcTool rpcTool;
    private final PropertyConfig propertyConfig;
    private final AppService appService;
    private final RedisService redisService;

    public ClientTokenService(final RpcTool rpcTool,
                              final PropertyConfig propertyConfig,
                              final AppService appService,
                              final RedisService redisService) {
        this.rpcTool = rpcTool;
        this.propertyConfig = propertyConfig;
        this.appService = appService;
        this.redisService = redisService;
    }

    public ClientToken getClientCredentialsToken(final String clientId,
                                                 final String clientSecret) throws Exception {
        final App app = this.appService.getByClientId(clientId);
        if (null == app || !app.getClientSecret().equals(clientSecret)) {
            throw new BusinessException(String.format("The clientId [%s] not match clientSecret [%s]", clientId, clientSecret));
        }
        final Map<String, Object> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("grant_type", GrantType.CLIENT_CREDENTIALS.getCode());
        final String response = this.rpcTool.post(this.getOuathTokenUrl(), params);
        final Map<String, Object> map = JSON.parseObject(response, Map.class);
        if (map.containsKey("error")) {
            throw new BusinessException(response);
        }
        final ClientToken result = new ClientToken();
        result.setAppId(app.getId());
        result.setAccessToken(map.get(Name.ACCESS_TOKEN).toString());
        result.setExpiresTime(Long.parseLong(map.get(Name.EXPIRES_IN).toString()));  //单位:秒
        this.redisService.set(String.format(Names.REDIS_ACCESS_TOKEN_KEY, clientId), result.getAccessToken(), result.getExpiresTime() * 1000);
        return result;
    }

    private String getOuathTokenUrl() {
        return String.format("http://localhost:%s/%s",
                this.propertyConfig.getPort(),
                Name.OAUTH_TOKEN);
    }

}
