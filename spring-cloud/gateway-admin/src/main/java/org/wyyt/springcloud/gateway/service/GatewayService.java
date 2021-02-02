package org.wyyt.springcloud.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.wyyt.redis.service.RedisService;
import org.wyyt.springcloud.gateway.config.PropertyConfig;
import org.wyyt.springcloud.gateway.entity.contants.Names;
import org.wyyt.springcloud.gateway.entity.exception.GatewayException;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.rpc.Result;
import org.wyyt.tool.rpc.RpcService;
import org.wyyt.tool.rpc.SignTool;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The service of RPC for gateway
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
@Service
public class GatewayService {
    private final PropertyConfig propertyConfig;
    private final RpcService rpcService;
    private final RedisService redisService;
    private final ConsulService consulService;
    private final static String REFRESH_URL = "/route/refresh";
    private final static String LIST_ROUTES_URL = "/route/listRoutes";
    private final static String REMOVE_IGNORE_URLS_LOCAL_CACHE = "cache/removeIngoreUrlSetLocalCache";
    private final static String REMOVE_CLIENT_ID_LOCAL_CACHE = "cache/removeClientIdLocalCache";

    public GatewayService(final PropertyConfig propertyConfig,
                          final RpcService rpcService,
                          final RedisService redisService,
                          final ConsulService consulService) {
        this.propertyConfig = propertyConfig;
        this.rpcService = rpcService;
        this.redisService = redisService;
        this.consulService = consulService;
    }

    public void refresh() throws Exception {
        this.rpcAll(REFRESH_URL, null);
    }

    public List<String> listWorkingRoutes() throws Exception {
        final URI uri = this.consulService.getServiceUri(this.propertyConfig.getGatewayConsulName());
        if (null == uri) {
            return null;
        }
        final Result<?> respondResult = this.rpc(uri, LIST_ROUTES_URL, null);
        return (List<String>) ConvertUtils.convert(respondResult.getData(), List.class);
    }

    public void removeIgnoreUrlSetLocalCache() throws Exception {
        this.rpcAll(REMOVE_IGNORE_URLS_LOCAL_CACHE, null);
    }

    public void removeClientIdLocalCache(final String clientId) throws Exception {
        final Map<String, Object> params = new HashMap<>();
        params.put(Names.CLIENT_ID, clientId);
        this.rpcAll(REMOVE_CLIENT_ID_LOCAL_CACHE, params);
    }

    public void rpcAll(final String serviceUrl,
                       final Map<String, Object> params) throws Exception {
        final List<URI> serviceUris = this.consulService.getServiceUris(this.propertyConfig.getGatewayConsulName());
        if (null == serviceUris || serviceUris.isEmpty()) {
            return;
        }
        final List<Exception> exceptionList = new ArrayList<>();
        for (final URI uri : serviceUris) {
            try {
                final Result<?> respondResult = this.rpc(uri, serviceUrl, params);
                if (!respondResult.getOk()) {
                    throw new GatewayException(respondResult.getError());
                }
            } catch (final Exception e) {
                exceptionList.add(e);
            }
        }

        if (!exceptionList.isEmpty()) {
            for (final Exception exception : exceptionList) {
                log.error(ExceptionTool.getRootCauseMessage(exception));
            }
            throw exceptionList.get(0);
        }
    }

    public void clearAllCache(final String clientId) throws Exception {
        this.redisService.del(Names.getApiListOfClientIdKey(clientId));
        this.redisService.del(Names.getAppOfClientId(clientId));
        this.removeClientIdLocalCache(clientId);
        this.removeClientIdLocalCache(clientId);
    }

    private Result<?> rpc(final URI uri,
                          final String serviceUrl,
                          Map<String, Object> params) throws Exception {
        if (null == params) {
            params = new HashMap<>();
        }
        final Map<String, String> headers = new HashMap<>();
        headers.put("sign", SignTool.sign(params, Names.API_KEY, Names.API_IV));

        final URI remoteAddress = UriComponentsBuilder.fromHttpUrl(uri.toString().concat(serviceUrl)).build().toUri();

        return this.rpcService.post(remoteAddress.toString(), params, headers, new com.alibaba.fastjson.TypeReference<Result<?>>() {
        });
    }
}