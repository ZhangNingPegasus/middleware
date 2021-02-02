package org.wyyt.springcloud.auth.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wyyt.springcloud.auth.service.AccessTokenService;
import org.wyyt.springcloud.gateway.entity.entity.App;
import org.wyyt.springcloud.gateway.entity.entity.vo.AccessToken;
import org.wyyt.springcloud.gateway.entity.service.AppService;
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
    private final AccessTokenService clientTokenService;
    private final AppService appService;

    public ClientTokenController(final AccessTokenService clientTokenService,
                                 final AppService appService) {
        this.clientTokenService = clientTokenService;
        this.appService = appService;
    }

    @ApiOperation(value = "获取Access Token")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clientId", value = "注册的客户端id", required = true, dataType = "String"),
            @ApiImplicitParam(name = "clientSecret", value = "注册的客户端secret", required = true, dataType = "String")
    })
    @PostMapping(value = {"/v1/oauth/token", "v1/access_token"})
    public Result<AccessToken> clientLoginToken(@RequestParam("clientId") final String clientId,
                                                @RequestParam("clientSecret") final String clientSecret) throws Exception {
        return Result.ok(this.clientTokenService.getClientCredentialsToken(clientId, clientSecret));
    }

    @ApiOperation(value = "直接通过client id获取Access Token (为了兼容原来的使用方式, 建议不要使用, 有一定的风险, 后期可能会取消改接口)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "注册的客户端id", required = true, dataType = "String")
    })
    @PostMapping(value = "v1/app/access_token")
    public Result<AccessToken> clientLoginToken(@RequestParam("appId") final String clientId) throws Exception {
        final App app = this.appService.getByClientId(clientId);
        if (null == app) {
            return Result.error(String.format("不存在client id=[%s]的应用", clientId));
        }
        return Result.ok(this.clientTokenService.getClientCredentialsToken(app));
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
}