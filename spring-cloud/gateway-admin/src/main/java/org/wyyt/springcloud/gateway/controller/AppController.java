package org.wyyt.springcloud.gateway.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.wyyt.redis.service.RedisService;
import org.wyyt.springcloud.gateway.config.PropertyConfig;
import org.wyyt.springcloud.gateway.entity.ClientVo;
import org.wyyt.springcloud.gateway.entity.contants.Names;
import org.wyyt.springcloud.gateway.entity.entity.App;
import org.wyyt.springcloud.gateway.entity.entity.vo.AccessToken;
import org.wyyt.springcloud.gateway.service.AppServiceImpl;
import org.wyyt.springcloud.gateway.service.ConsulService;
import org.wyyt.springcloud.gateway.service.GatewayService;
import org.wyyt.tool.common.CommonTool;
import org.wyyt.tool.exception.BusinessException;
import org.wyyt.tool.rpc.Result;
import org.wyyt.tool.rpc.RpcService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
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
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class AppController {
    public static final String PREFIX = "app";
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final PropertyConfig propertyConfig;
    private final AppServiceImpl appServiceImpl;
    private final ConsulService consulService;
    private final RedisService redisService;
    private final GatewayService gatewayService;
    private final RpcService rpcService;

    public AppController(final PropertyConfig propertyConfig,
                         final AppServiceImpl appServiceImpl,
                         final ConsulService consulService,
                         final RedisService redisService,
                         final GatewayService gatewayService,
                         final RpcService rpcService) {
        this.propertyConfig = propertyConfig;
        this.appServiceImpl = appServiceImpl;
        this.consulService = consulService;
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
    public Result<?> del(@RequestParam(value = "ids") final String ids) throws Exception {
        final List<Long> idList = CommonTool.parseList(ids, ",", Long.class);
        this.appServiceImpl.del(new HashSet<>(idList));
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
        final String createAccessTokenUrl = this.getCreateAccessTokenUrl();
        if (null == createAccessTokenUrl) {
            return Result.error("无可用的网关或鉴权中心, 请假检查网关和鉴权中心是否正常开启");
        }

        final Map<String, Object> params = new HashMap<>();
        params.put("clientId", app.getClientId());
        params.put("clientSecret", app.getClientSecret());

        final Result<AccessToken> result = this.rpcService.post(createAccessTokenUrl,
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
        final String logoutAccessTokenUrl = this.getLogoutAccessTokenUrl();
        if (null == logoutAccessTokenUrl) {
            return Result.error("无可用的网关或鉴权中心, 请假检查网关和鉴权中心是否正常开启");
        }

        final Object accessToken = this.redisService.get(Names.getAccessTokenRedisKey(clientId));
        if (ObjectUtils.isEmpty(accessToken)) {
            return Result.ok(this.getAccessToken(clientId));
        }

        final Map<String, Object> params = new HashMap<>();
        params.put("clientId", app.getClientId());
        params.put("accessToken", accessToken.toString());

        this.rpcService.post(logoutAccessTokenUrl, params);
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

    private String getCreateAccessTokenUrl() throws URISyntaxException {
        return this.getRemoteUrl(this.propertyConfig.getAuthConsulName(), "v1/oauth/token");
    }

    private String getLogoutAccessTokenUrl() throws URISyntaxException {
        return this.getRemoteUrl(this.propertyConfig.getAuthConsulName(), "v1/oauth/logout");
    }

    private String getRemoteUrl(final String serviceName,
                                final String path) throws URISyntaxException {
        final URI gatewayUrl = this.consulService.getGatewayUri();
        if (null == gatewayUrl) {
            return null;
        }
        final URI authUri = this.consulService.getServiceUri(serviceName);
        if (null == authUri) {
            return null;
        }
        return String.format("%s/%s/%s", gatewayUrl, serviceName, path);
    }

    private AccessToken getAccessToken(final String clientId) {
        final String key = Names.getAccessTokenRedisKey(clientId);
        final AccessToken result = new AccessToken();
        final Object accessToken = this.redisService.get(key);
        final Long expire = this.redisService.getExpire(key);
        if (!ObjectUtils.isEmpty(accessToken) && null != expire) {
            result.setAccessToken(accessToken.toString());
            result.setExpiresTime(expire / 1000);
        }
        return result;
    }
}