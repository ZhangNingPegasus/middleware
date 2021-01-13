package org.wyyt.springcloud.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wyyt.springcloud.auth.entity.ClientToken;
import org.wyyt.springcloud.auth.service.ClientTokenService;
import org.wyyt.tool.rpc.Result;

/**
 * The controller of authtication
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@RestController
public class ClientTokenController {

    private final ClientTokenService clientTokenService;

    public ClientTokenController(final ClientTokenService clientTokenService) {
        this.clientTokenService = clientTokenService;
    }

    @PostMapping("/v1/access_token")
    public Result<ClientToken> clientLoginToken(@RequestParam("clientId") String clientId,
                                                @RequestParam("clientSecret") String clientSecret) throws Exception {
        return Result.ok(this.clientTokenService.getClientCredentialsToken(clientId, clientSecret));
    }
}