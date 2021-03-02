package org.wyyt.springcloud.auth.controller;

import com.alibaba.fastjson.JSON;
import com.nepxion.discovery.plugin.strategy.context.StrategyContextHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wyyt.redis.service.RedisService;
import org.wyyt.springcloud.auth.entity.TokenVo;
import org.wyyt.springcloud.auth.service.AccessTokenService;
import org.wyyt.springcloud.entity.constants.Names;
import org.wyyt.springcloud.gateway.entity.contants.Constant;
import org.wyyt.springcloud.gateway.entity.entity.App;
import org.wyyt.springcloud.gateway.entity.entity.vo.AccessToken;
import org.wyyt.springcloud.gateway.entity.service.AppService;
import org.wyyt.springcloud.gateway.entity.utils.Common;
import org.wyyt.tool.rpc.Result;

/**
 * The controller of authtication
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Api("接口授权")
@RestController
public class ClientTokenController {
    private final StrategyContextHolder strategyContextHolder;
    private final AccessTokenService clientTokenService;
    private final AppService appService;
    private final RedisService redisService;

    public ClientTokenController(final StrategyContextHolder strategyContextHolder,
                                 final AccessTokenService clientTokenService,
                                 final AppService appService,
                                 final RedisService redisService) {
        this.strategyContextHolder = strategyContextHolder;
        this.clientTokenService = clientTokenService;
        this.appService = appService;
        this.redisService = redisService;
    }

    @ApiOperation(value = "获取应用信息")
    @PostMapping(value = {"/v1/oauth/info"})
    public Result<String> getInfoByAccessToken() {
        final String accessToken = this.strategyContextHolder.getHeader(Names.ACCESS_TOKEN);
        if (ObjectUtils.isEmpty(accessToken)) {
            return Result.ok("");
        }
        final String clientId = Common.getClientIdFromAccessToken(accessToken);
        if (ObjectUtils.isEmpty(clientId)) {
            return Result.ok("");
        }
        final Object at = this.redisService.get(Constant.getAccessTokenRedisKey(clientId));
        if (!accessToken.equals(at)) {
            return Result.ok("");
        }
        final App app = this.clientTokenService.getByClientId(clientId);
        app.setClientSecret(null);
        app.setRowCreateTime(null);
        app.setRowUpdateTime(null);
        return Result.ok(JSON.toJSONString(app));
    }

    @ApiOperation(value = "获取Access Token")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clientId", value = "注册的客户端id", required = true, dataType = "String"),
            @ApiImplicitParam(name = "clientSecret", value = "注册的客户端secret", required = true, dataType = "String")
    })
    @PostMapping(value = {"/v1/oauth/token"})
    public Result<AccessToken> clientLoginToken(@RequestParam("clientId") final String clientId,
                                                @RequestParam("clientSecret") final String clientSecret) throws Exception {
        return Result.ok(this.clientTokenService.getClientCredentialsToken(clientId, clientSecret));
    }

    @ApiOperation(value = "注销已授权的Access Token")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clientId", value = "注册的客户端id", required = true, dataType = "String"),
            @ApiImplicitParam(name = "accessToken", value = "已授权的Access Token", required = true, dataType = "String")
    })
    @PostMapping("/v1/oauth/logout")
    public Result<?> removeToken(@RequestParam("clientId") final String clientId,
                                 @RequestParam("accessToken") final String accessToken) {
        if (this.clientTokenService.logoutClientCredentialsToken(clientId, accessToken)) {
            return Result.ok();
        } else {
            return Result.error("Failed to logout");
        }
    }

    //---------------------------------------------兼容旧接口-------------------------------------------------------------

    @ApiOperation(value = "获取Access Token (接口已过时,请用v1/oauth/token接口代替)")
    @PostMapping(value = {"v1/access_token"})
    public Result<AccessToken> clientLoginTokenOld(@RequestBody final TokenVo tokenVo) throws Exception {
        return Result.ok(this.clientTokenService.getClientCredentialsToken(tokenVo.getApiKey(), tokenVo.getSecretKey()));
    }

    @ApiOperation(value = "直接通过AppId(Client Id)获取Access Token (接口已过时,请用v1/oauth/token接口代替)")
    @PostMapping(value = "v1/app/access_token")
    public Result<AccessToken> clientLoginToken(@RequestBody final TokenVo tokenVo) throws Exception {
        final String clientId = tokenVo.getAppId();
        final App app = this.appService.getByClientId(clientId);
        if (null == app) {
            return Result.error(String.format("不存在App id=[%s]的应用", clientId));
        }
        return Result.ok(this.clientTokenService.getClientCredentialsToken(app));
    }
}