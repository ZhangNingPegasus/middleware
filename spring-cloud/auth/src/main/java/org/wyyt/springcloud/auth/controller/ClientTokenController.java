package org.wyyt.springcloud.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wyyt.springcloud.auth.entity.AccessToken;
import org.wyyt.springcloud.auth.service.AccessTokenService;
import org.wyyt.tool.rpc.Result;

/**
 * The controller of authtication
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021        Initialize  *
 * *****************************************************************
 */
@RestController
public class ClientTokenController {
    private final AccessTokenService clientTokenService;

    public ClientTokenController(final AccessTokenService clientTokenService) {
        this.clientTokenService = clientTokenService;
    }

    @PostMapping("/v1/oauth/token")
    public Result<AccessToken> clientLoginToken(@RequestParam("clientId") final String clientId,
                                                @RequestParam("clientSecret") final String clientSecret) throws Exception {
        return Result.ok(this.clientTokenService.getClientCredentialsToken(clientId, clientSecret));
    }


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