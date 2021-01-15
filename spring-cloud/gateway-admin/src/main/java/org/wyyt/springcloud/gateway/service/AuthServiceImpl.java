package org.wyyt.springcloud.gateway.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.wyyt.springcloud.gateway.entity.anno.TranSave;
import org.wyyt.springcloud.gateway.entity.contants.Names;
import org.wyyt.springcloud.gateway.entity.entity.App;
import org.wyyt.springcloud.gateway.entity.entity.Auth;
import org.wyyt.springcloud.gateway.entity.service.AuthService;

import java.util.List;

/**
 * The service of `t_auth` table
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Service
public class AuthServiceImpl extends AuthService {

    private final GatewayService gatewayService;

    public AuthServiceImpl(final GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @TranSave
    public void add(final Long appId,
                    final Long apiId) throws Exception {
        Auth auth = this.getByAppIdAndApiId(appId, apiId);
        if (null != auth) {
            throw new RuntimeException(String.format("App[id=%s]已存在Api[id=%s]的权限", appId, apiId));
        }

        auth = new Auth();
        auth.setAppId(appId);
        auth.setApiId(apiId);
        this.save(auth);
        this.removeRedis(appId);
    }

    @TranSave
    public void edit(final Long id,
                     final Long appId,
                     final Long apiId) throws Exception {
        final Auth auth = this.getById(id);
        if (null == auth) {
            throw new RuntimeException(String.format("不存在id=%s的权限", id));
        }
        auth.setAppId(appId);
        auth.setApiId(apiId);
        this.updateById(auth);
        this.removeRedis(appId);
    }

    @TranSave
    public void del(final Long appId,
                    final Long apiId) throws Exception {
        final Auth auth = this.getByAppIdAndApiId(appId, apiId);
        if (null == auth) {
            throw new RuntimeException(String.format("App[id=%s]不存在Api[id=%s]的权限", appId, apiId));
        }
        this.removeById(auth);
        this.removeRedis(appId);
    }

    @TranSave
    public void removeByAppId(final Long appId) throws Exception {
        if (null == appId) {
            return;
        }
        final QueryWrapper<Auth> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Auth::getAppId, appId);
        this.remove(queryWrapper);
        removeRedis(appId);
    }

    @TranSave
    public void removeByApiId(final Long apiId) throws Exception {
        if (null == apiId) {
            return;
        }
        final QueryWrapper<Auth> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Auth::getApiId, apiId);
        final List<Auth> authList = this.list(queryWrapper);
        this.remove(queryWrapper);

        for (final Auth auth : authList) {
            this.removeRedis(auth.getAppId());
        }
    }

    private void removeRedis(final Long appId) throws Exception {
        if (null == appId) {
            return;
        }
        this.removeRedis(this.appService.getById(appId));
    }

    private void removeRedis(App app) throws Exception {
        if (null == app) {
            return;
        }
        this.redisService.del(Names.getApiListOfClientIdKey(app.getClientId()));
        this.gatewayService.removeClientIdLocalCache(app.getClientId());
    }
}