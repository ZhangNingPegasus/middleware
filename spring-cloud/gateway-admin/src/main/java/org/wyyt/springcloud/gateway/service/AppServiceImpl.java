package org.wyyt.springcloud.gateway.service;

import org.springframework.stereotype.Service;
import org.wyyt.springcloud.gateway.entity.anno.TranSave;
import org.wyyt.springcloud.gateway.entity.contants.Names;
import org.wyyt.springcloud.gateway.entity.entity.App;
import org.wyyt.springcloud.gateway.entity.entity.OauthClientDetails;
import org.wyyt.springcloud.gateway.entity.entity.enums.GrantType;
import org.wyyt.springcloud.gateway.entity.service.AppService;

/**
 * The service of `t_app` table
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Service
public class AppServiceImpl extends AppService {

    private final GatewayService gatewayService;
    private final AuthServiceImpl authServiceImpl;

    public AppServiceImpl(final GatewayService gatewayService,
                          final AuthServiceImpl authServiceImpl) {
        this.gatewayService = gatewayService;
        this.authServiceImpl = authServiceImpl;
    }

    @TranSave
    public void add(final String clientId,
                    final String clientSecret,
                    final String encClientSecret,
                    final String name,
                    final Boolean isAdmin,
                    final String description) {
        App app = this.getByClientId(clientId);
        if (null != app) {
            throw new RuntimeException(String.format("应用[%s]已存在", clientId));
        }
        app = new App();
        app.setClientId(clientId);
        app.setClientSecret(clientSecret);
        app.setName(name);
        app.setIsAdmin(isAdmin);
        app.setDescription(description);
        this.save(app);

        final OauthClientDetails oauthClientDetails = new OauthClientDetails();
        oauthClientDetails.setClientId(clientId);
        oauthClientDetails.setClientSecret(encClientSecret);
        oauthClientDetails.setScope("all");
        oauthClientDetails.setAuthorizedGrantTypes(GrantType.CLIENT_CREDENTIALS.getCode());
        this.oauthClientDetailsService.save(oauthClientDetails);
        this.redisService.set(Names.getAppOfClientId(app.getClientId()), app);
    }

    @TranSave
    public void edit(final Long id,
                     final String name,
                     final Boolean isAdmin,
                     final String description) throws Exception {
        final App app = this.getById(id);
        if (null == app) {
            return;
        }
        app.setName(name);
        app.setIsAdmin(isAdmin);
        app.setDescription(description);
        this.updateById(app);
        this.removeRedis(app.getClientId());
    }

    @TranSave
    public void del(final Long appId) throws Exception {
        final App app = this.getById(appId);
        if (null == app) {
            return;
        }
        this.removeById(appId);
        this.authServiceImpl.removeByAppId(app.getId());
        this.oauthClientDetailsService.removeById(app.getClientId());
        this.redisService.del(Names.getAccessTokenRedisKey(app.getClientId()));
        this.removeRedis(app.getClientId());
    }

    private void removeRedis(final String clientId) throws Exception {
        this.redisService.del(Names.getAppOfClientId(clientId));
        this.redisService.del(Names.getApiListOfClientIdKey(clientId));
        this.gatewayService.removeClientIdLocalCache(clientId);
    }
}