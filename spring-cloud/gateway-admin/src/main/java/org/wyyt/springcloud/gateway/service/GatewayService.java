package org.wyyt.springcloud.gateway.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.wyyt.admin.ui.exception.BusinessException;
import org.wyyt.redis.service.RedisService;
import org.wyyt.springcloud.gateway.config.PropertyConfig;
import org.wyyt.springcloud.gateway.entity.EndpointVo;
import org.wyyt.springcloud.gateway.entity.ServiceVo;
import org.wyyt.springcloud.gateway.entity.contants.Names;
import org.wyyt.springcloud.gateway.entity.exception.GatewayException;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.rpc.Result;
import org.wyyt.tool.rpc.RpcService;
import org.wyyt.tool.rpc.SignTool;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The service of RPC for gateway
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
@Service
public class GatewayService {
    private final DiscoveryClient discoveryClient;
    private final PropertyConfig propertyConfig;
    private final RpcService rpcService;
    private final RedisService redisService;
    private final static String REFRESH_URL = "/route/refresh";
    private final static String LIST_ROUTES_URL = "/route/listRoutes";
    private final static String REMOVE_IGNORE_URLS_LOCAL_CACHE = "cache/removeIngoreUrlSetLocalCache";
    private final static String REMOVE_CLIENT_ID_LOCAL_CACHE = "cache/removeClientIdLocalCache";

    public GatewayService(final DiscoveryClient discoveryClient,
                          final PropertyConfig propertyConfig,
                          final RpcService rpcService,
                          final RedisService redisService) {
        this.discoveryClient = discoveryClient;
        this.propertyConfig = propertyConfig;
        this.rpcService = rpcService;
        this.redisService = redisService;
    }

    public URI getGatewayUri() throws Exception {
        return this.getAvaiableServiceUri(this.propertyConfig.getGatewayConsulName());
    }

    public List<ServiceVo> listService(boolean onlyAlive) throws Exception {
        final List<ServiceVo> result = new ArrayList<>();
        for (final String serviceId : this.listServiceIds()) {
            result.add(this.getService(serviceId, onlyAlive));
        }
        return result;
    }

    public List<ServiceVo> listService() throws Exception {
        return this.listService(true);
    }

    public ServiceVo getService(final String serviceId,
                                final boolean onlyAlive) throws Exception {
        final ServiceVo result = new ServiceVo();
        result.setName(serviceId);
        final List<EndpointVo> endpointVoList = new ArrayList<>();
        result.setEndpointVoList(endpointVoList);
        final String url = String.format("http://%s:%s/v1/health/service/%s",
                this.propertyConfig.getConsulHost(),
                this.propertyConfig.getConsulPort(),
                serviceId);
        final String json = this.rpcService.get(url);
        final JSONArray allJsonArray = JSON.parseArray(json);

        if (null == allJsonArray || allJsonArray.isEmpty()) {
            return null;
        }

        for (final Object object : allJsonArray) {
            if (object instanceof JSONObject) {
                final JSONObject jsonObject = (JSONObject) object;
                final Object service = jsonObject.get("Service");
                final Object checks = jsonObject.get("Checks");

                final Map<String, JSONObject> checksMap = new HashMap<>();
                if (checks instanceof JSONArray) {
                    JSONArray jsonArray = (JSONArray) checks;
                    for (final Object obj : jsonArray) {
                        if (obj instanceof JSONObject) {
                            final JSONObject jsonObj = (JSONObject) obj;
                            final String serviceID = jsonObj.getString("ServiceID");
                            if (ObjectUtils.isEmpty(serviceID)) {
                                continue;
                            }
                            checksMap.put(serviceID, jsonObj);
                        }
                    }
                }

                if (service instanceof JSONObject) {
                    final EndpointVo endpointVo = new EndpointVo();
                    final JSONObject jsonService = (JSONObject) service;

                    final JSONObject jsonCheck = checksMap.get(jsonService.getString("ID"));
                    final boolean alive = (null != jsonCheck && jsonCheck.getString("Status").equalsIgnoreCase("passing"));

                    if (onlyAlive && !alive) {
                        continue;
                    }
                    endpointVo.setId(jsonService.getString("ID"));
                    endpointVo.setHost(jsonService.getString("Address"));
                    endpointVo.setPort(jsonService.getInteger("Port"));
                    endpointVo.setAlive(alive);
                    final Object tags = jsonService.get("Tags");

                    if (tags instanceof JSONArray) {
                        final JSONArray jsonTags = (JSONArray) tags;
                        for (Object jsonTag : jsonTags) {
                            if (jsonTag instanceof String) {
                                final String tag = jsonTag.toString().trim();
                                final String[] all = tag.split("=");
                                if (all.length < 2) {
                                    continue;
                                } else if ("version".equals(all[0])) {
                                    endpointVo.setVersion(all[1]);
                                } else if ("group".equals(all[0])) {
                                    endpointVo.setGroup(all[1]);
                                }
                            }
                            if (!ObjectUtils.isEmpty(endpointVo.getVersion())) {
                                break;
                            }
                        }
                    }
                    endpointVoList.add(endpointVo);
                }
            }
        }
        return result;
    }

    public List<URI> getAvaiableServiceUris(final String serviceId) throws Exception {
        final ServiceVo serviceVo = this.getService(serviceId, true);
        if (null == serviceVo) {
            return null;
        }
        final List<EndpointVo> endpointVoList = serviceVo.getEndpointVoList();
        if (endpointVoList.isEmpty()) {
            return null;
        }

        return endpointVoList.stream().map(p -> {
            try {
                return new URI(String.format("http://%s:%s", p.getHost(), p.getPort()));
            } catch (URISyntaxException e) {
                throw new BusinessException(e);
            }
        }).collect(Collectors.toList());
    }

    public URI getAvaiableServiceUri(final String serviceId) throws Exception {
        final List<URI> avaiableServiceUris = this.getAvaiableServiceUris(serviceId);
        if (null == avaiableServiceUris || avaiableServiceUris.isEmpty()) {
            return null;
        }
        return avaiableServiceUris.get(RandomUtils.nextInt(0, avaiableServiceUris.size()));
    }

    public ServiceVo getService(final String serviceId) throws Exception {
        return this.getService(serviceId, true);
    }

    public List<String> listServiceIds() {
        final List<String> ignoredServiceNames = Arrays.asList("consul",
                this.propertyConfig.getServiceName(),
                this.propertyConfig.getGatewayConsulName());
        final List<String> services = this.discoveryClient.getServices();
        return services.stream().filter(p -> !ignoredServiceNames.contains(p))
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    public void refresh() throws Exception {
        this.rpcAll(REFRESH_URL, null);
    }

    public List<String> listWorkingRoutes() throws Exception {
        final URI uri = this.getAvaiableServiceUri(this.propertyConfig.getGatewayConsulName());
        if (null == uri) {
            return null;
        }
        final Result<?> respondResult = this.rpc(uri, LIST_ROUTES_URL, null);
        return (List<String>) ConvertUtils.convert(respondResult.getData(), List.class);
    }

    public void removeIngoreUrlSetLocalCache() throws Exception {
        this.rpcAll(REMOVE_IGNORE_URLS_LOCAL_CACHE, null);
    }

    public void removeClientIdLocalCache(final String clientId) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Names.CLIENT_ID, clientId);
        this.rpcAll(REMOVE_CLIENT_ID_LOCAL_CACHE, params);
    }

    public void rpcAll(final String serviceUrl,
                       final Map<String, Object> params) throws Exception {
        final List<URI> avaiableServiceUris = this.getAvaiableServiceUris(this.propertyConfig.getGatewayConsulName());
        if (null == avaiableServiceUris || avaiableServiceUris.isEmpty()) {
            return;
        }
        final List<Exception> exceptionList = new ArrayList<>();
        for (final URI uri : avaiableServiceUris) {
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