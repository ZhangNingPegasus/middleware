package org.wyyt.springcloud.auth.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wyyt.springcloud.auth.service.AccessTokenService;
import org.wyyt.springcloud.gateway.entity.entity.vo.AccessToken;
import org.wyyt.tool.rpc.Result;

/**
 * The controller of authtication
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Api("接口授权")
@RestController
public class ClientTokenController {
    private final AccessTokenService clientTokenService;

    public ClientTokenController(final AccessTokenService clientTokenService) {
        this.clientTokenService = clientTokenService;
    }

    @ApiOperation(value = "获取Access Token")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clientId", value = "注册的客户端id", required = true, dataType = "String"),
            @ApiImplicitParam(name = "clientSecret", value = "注册的客户端secret", required = true, dataType = "String")
    })
    @PostMapping("/v1/oauth/token")
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
}