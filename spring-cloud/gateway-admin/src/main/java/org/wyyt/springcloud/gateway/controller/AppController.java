package org.wyyt.springcloud.gateway.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.wyyt.admin.ui.exception.BusinessException;
import org.wyyt.redis.service.RedisService;
import org.wyyt.springcloud.gateway.config.PropertyConfig;
import org.wyyt.springcloud.gateway.entity.ClientVo;
import org.wyyt.springcloud.gateway.entity.contants.Names;
import org.wyyt.springcloud.gateway.entity.entity.App;
import org.wyyt.springcloud.gateway.entity.entity.vo.AccessToken;
import org.wyyt.springcloud.gateway.service.AppServiceImpl;
import org.wyyt.springcloud.gateway.service.GatewayService;
import org.wyyt.tool.rpc.Result;
import org.wyyt.tool.rpc.RpcService;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wyyt.springcloud.gateway.controller.AppController.PREFIX;


/**
 * The controller of App
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021        Initialize  *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class AppController {
    public static final String PREFIX = "app";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final PropertyConfig propertyConfig;
    private final AppServiceImpl appServiceImpl;
    private final RedisService redisService;
    private final GatewayService gatewayService;
    private final RpcService rpcService;

    public AppController(final PropertyConfig propertyConfig,
                         final AppServiceImpl appServiceImpl,
                         final RedisService redisService,
                         final GatewayService gatewayService,
                         final RpcService rpcService) {
        this.propertyConfig = propertyConfig;
        this.appServiceImpl = appServiceImpl;
        this.redisService = redisService;
        this.gatewayService = gatewayService;
        this.rpcService = rpcService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("toadd")
    public String toAdd() {
        return String.format("%s/%s", PREFIX, "add");
    }

    @GetMapping("toedit")
    public String toEdit(final Model model,
                         @RequestParam(value = "id") final Long id) throws Exception {
        final App app = this.appServiceImpl.getById(id);
        if (null == app) {
            throw new BusinessException(String.format("App[id=%s]不存在", id));
        }

        final Object accessToken = this.redisService.get(Names.getAccessTokenRedisKey(app.getClientId()));

        if (!ObjectUtils.isEmpty(accessToken)) {
            model.addAttribute("ak", this.getAccessToken(app.getClientId()));
        }
        model.addAttribute("app", app);
        model.addAttribute("createTokenUrl", this.getCreateAccessTokenUrl());
        model.addAttribute("logoutTokenUrl", this.getLogoutAccessTokenUrl());
        return String.format("%s/%s", PREFIX, "edit");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<App>> list(@RequestParam(value = "page") final Integer pageNum,
                                  @RequestParam(value = "limit") final Integer pageSize,
                                  @RequestParam(value = "clientId", required = false) final String clientId,
                                  @RequestParam(value = "name", required = false) final String name) {
        final IPage<App> page = this.appServiceImpl.page(pageNum, pageSize, clientId, name);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "clientId") final String clientId,
                         @RequestParam(value = "clientSecret") final String clientSecret,
                         @RequestParam(value = "name") final String name,
                         @RequestParam(value = "isAdmin") final Boolean isAdmin,
                         @RequestParam(value = "description") final String description) {
        this.appServiceImpl.add(clientId,
                clientSecret,
                this.passwordEncoder.encode(clientSecret),
                name,
                isAdmin,
                description);
        return Result.ok();
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id") final Long id,
                          @RequestParam(value = "name") final String name,
                          @RequestParam(value = "isAdmin") final Boolean isAdmin,
                          @RequestParam(value = "description") final String description) throws Exception {
        this.appServiceImpl.edit(id, name, isAdmin, description);
        return Result.ok();
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "id") final Long id) throws Exception {
        this.appServiceImpl.del(id);
        return Result.ok();
    }

    @PostMapping("genName")
    @ResponseBody
    public Result<ClientVo> genName() {
        String clientId;
        do {
            clientId = RandomStringUtils.randomAlphanumeric(15);
        } while (null != this.appServiceImpl.getByClientId(clientId));

        final ClientVo clientVo = new ClientVo();
        clientVo.setClientId(clientId);
        clientVo.setClientSecret(RandomStringUtils.randomAlphanumeric(25));
        return Result.ok(clientVo);
    }

    @PostMapping("createAccessToken")
    @ResponseBody
    public Result<AccessToken> createAccessToken(@RequestParam(value = "clientId") final String clientId) throws Exception {
        final App app = this.appServiceImpl.getByClientId(clientId);
        if (null == app) {
            throw new BusinessException(String.format("Client Id[%s]不存在", clientId));
        }

        final Map<String, Object> params = new HashMap<>();
        params.put("clientId", app.getClientId());
        params.put("clientSecret", app.getClientSecret());
        final Result<AccessToken> result = this.rpcService.post(this.getCreateAccessTokenUrl(),
                params,
                new com.alibaba.fastjson.TypeReference<Result<AccessToken>>() {
                });
        if (null == result) {
            return Result.ok(new AccessToken());
        }
        return Result.ok(result.getData());
    }

    @PostMapping("logoutAccessToken")
    @ResponseBody
    public Result<AccessToken> logoutAccessToken(@RequestParam(value = "clientId") final String clientId) throws Exception {
        final App app = this.appServiceImpl.getByClientId(clientId);
        if (null == app) {
            throw new BusinessException(String.format("Client Id[%s]不存在", clientId));
        }

        final Object accessToken = this.redisService.get(Names.getAccessTokenRedisKey(clientId));
        if (ObjectUtils.isEmpty(accessToken)) {
            return Result.ok(this.getAccessToken(clientId));
        }

        final Map<String, Object> params = new HashMap<>();
        params.put("clientId", app.getClientId());
        params.put("accessToken", accessToken.toString());
        this.rpcService.post(this.getLogoutAccessTokenUrl(), params);
        return Result.ok(this.getAccessToken(clientId));
    }

    @PostMapping("refreshAccessToken")
    @ResponseBody
    public Result<AccessToken> refreshAccessToken(@RequestParam(value = "clientId") final String clientId) {
        final App app = this.appServiceImpl.getByClientId(clientId);
        if (null == app) {
            return Result.ok();
        }
        return Result.ok(this.getAccessToken(app.getClientId()));
    }

    @PostMapping("clearCache")
    @ResponseBody
    public Result<?> clearCache(@RequestParam(value = "clientId") final String clientId) throws Exception {
        this.gatewayService.clearAllCache(clientId);
        return Result.ok();
    }

    private String getGatewayUrl() throws Exception {
        final URI result = this.gatewayService.getAvaiableServiceUri(this.propertyConfig.getGatewayConsulName());
        if (null == result) {
            throw new BusinessException("无可用的网关, 请检查网关是否运行正常");
        }
        return result.toString();
    }

    private String getCreateAccessTokenUrl() throws Exception {
        return String.format("%s/auth/v1/oauth/token", this.getGatewayUrl());
    }

    private String getLogoutAccessTokenUrl() throws Exception {
        return String.format("%s/auth/v1/oauth/logout", this.getGatewayUrl());
    }

    private AccessToken getAccessToken(final String clientId) {
        final String key = Names.getAccessTokenRedisKey(clientId);
        final AccessToken result = new AccessToken();
        final Object accessToken = this.redisService.get(key);
        if (!ObjectUtils.isEmpty(accessToken)) {
            result.setAccessToken(accessToken.toString());
            result.setExpiresTime(this.redisService.getExpire(key) / 1000);
        }
        return result;
    }
}