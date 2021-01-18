package org.wyyt.springcloud.gateway.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.wyyt.springcloud.gateway.entity.anno.TranSave;
import org.wyyt.springcloud.gateway.entity.entity.App;
import org.wyyt.springcloud.gateway.entity.entity.Auth;
import org.wyyt.springcloud.gateway.entity.entity.base.BaseDto;
import org.wyyt.springcloud.gateway.entity.service.AuthService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The service of `t_auth` table
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
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
                    final List<Long> apiIds) throws Exception {
        if (apiIds.isEmpty()) {
            return;
        }
        final Set<Long> apiIdSet = new HashSet<>(apiIds.size());
        for (final Long apiId : apiIds) {
            final Auth auth = this.getByAppIdAndApiId(appId, apiId);
            if (null == auth) {
                apiIdSet.add(apiId);
            }
        }

        if (!apiIdSet.isEmpty()) {
            for (final Long apiId : apiIdSet) {
                final Auth auth = new Auth();
                auth.setAppId(appId);
                auth.setApiId(apiId);
                this.save(auth);
            }
            this.removeRedis(appId);
        }
    }

    @TranSave
    public void del(final Long appId,
                    final Set<Long> apiIdSet) throws Exception {
        if (apiIdSet.isEmpty()) {
            return;
        }
        final List<Auth> authList = new ArrayList<>(apiIdSet.size());
        for (final Long apiId : apiIdSet) {
            final Auth auth = this.getByAppIdAndApiId(appId, apiId);
            if (null != auth) {
                authList.add(auth);
            }
        }
        if (!authList.isEmpty()) {
            this.removeByIds(authList.stream().map(BaseDto::getId).collect(Collectors.toSet()));
            this.removeRedis(appId);
        }
    }

    @TranSave
    public void removeByAppId(final Long appId) throws Exception {
        if (null == appId) {
            return;
        }
        final QueryWrapper<Auth> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Auth::getAppId, appId);
        this.remove(queryWrapper);
        this.removeRedis(appId);
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

        final Set<Long> appIdSet = authList.stream().map(Auth::getAppId).collect(Collectors.toSet());

        for (final Long appId : appIdSet) {
            this.removeRedis(appId);
        }
    }

    private void removeRedis(final Long appId) throws Exception {
        if (null == appId) {
            return;
        }
        this.removeRedis(this.appService.getById(appId));
    }

    private void removeRedis(final App app) throws Exception {
        if (null == app) {
            return;
        }
        this.gatewayService.clearAllCache(app.getClientId());
    }
}