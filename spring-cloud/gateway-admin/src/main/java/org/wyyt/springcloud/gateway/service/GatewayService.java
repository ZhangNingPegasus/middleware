package org.wyyt.springcloud.gateway.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.wyyt.springcloud.gateway.config.PropertyConfig;
import org.wyyt.springcloud.gateway.entity.EndpointVo;
import org.wyyt.springcloud.gateway.entity.ServiceVo;
import org.wyyt.springcloud.gateway.entity.contants.Names;
import org.wyyt.springcloud.gateway.entity.exception.GatewayException;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.rpc.Result;
import org.wyyt.tool.rpc.RpcTool;
import org.wyyt.tool.rpc.SignTool;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The service of RPC for gateway
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
@Service
public class GatewayService {
    private final DiscoveryClient discoveryClient;
    private final PropertyConfig propertyConfig;
    private final LoadBalancerClient loadBalancerClient;
    private final RpcTool rpcTool;
    private final static String REFRESH_URL = "/route/refresh";
    private final static String LIST_ROUTES_URL = "/route/listRoutes";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public GatewayService(final DiscoveryClient discoveryClient,
                          final PropertyConfig propertyConfig,
                          final LoadBalancerClient loadBalancerClient,
                          final RpcTool rpcTool) {
        this.discoveryClient = discoveryClient;
        this.propertyConfig = propertyConfig;
        this.loadBalancerClient = loadBalancerClient;
        this.rpcTool = rpcTool;
    }

    public EndpointVo getGatewayUri() {
        final ServiceInstance serviceInstance = this.loadBalancerClient.choose(this.propertyConfig.getGatewayConsulName());
        if (null == serviceInstance) {
            return null;
        }

        final EndpointVo result = new EndpointVo();
        result.setHost(serviceInstance.getHost());
        result.setPort(serviceInstance.getPort());
        result.setVersion(serviceInstance.getMetadata().get("version"));
        result.setGroup(serviceInstance.getMetadata().get("group"));
        return result;
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
        final String json = this.rpcTool.get(url);
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
                            if (StringUtils.isEmpty(serviceID)) {
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
                    boolean alive = (null != jsonCheck && jsonCheck.getString("Status").equalsIgnoreCase("passing"));

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

    public ServiceVo getService(final String serviceId) throws Exception {
        return this.getService(serviceId, true);
    }

    public List<String> listServiceIds() {
        final List<String> ignoredServiceNames = Arrays.asList("consul", this.propertyConfig.getServiceName(), this.propertyConfig.getGatewayConsulName());
        final List<String> services = this.discoveryClient.getServices();
        return services.stream().filter(p -> !ignoredServiceNames.contains(p))
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    public void refresh() throws Exception {
        final List<Exception> exceptionList = new ArrayList<>();
        final List<ServiceInstance> instances = this.discoveryClient.getInstances(this.propertyConfig.getGatewayConsulName());
        for (final ServiceInstance instance : instances) {
            try {
                final URI uri = instance.getUri();
                final Result<?> respondResult = this.rpc(instance.getUri(), REFRESH_URL, null);
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

    public List<String> listWorkingRoutes() throws Exception {
        final ServiceInstance serviceInstance = this.loadBalancerClient.choose(this.propertyConfig.getGatewayConsulName());
        if (null == serviceInstance) {
            return null;
        }

        final URI uri = serviceInstance.getUri();
        final Result<?> respondResult = this.rpc(uri, LIST_ROUTES_URL, null);
        return (List<String>) ConvertUtils.convert(respondResult.getData(), List.class);
    }

    private Result<?> rpc(final URI uri,
                          final String path,
                          Map<String, Object> params) throws Exception {
        if (null == params) {
            params = new HashMap<>();
        }
        final Map<String, String> headers = new HashMap<>();
        headers.put("sign", SignTool.sign(params, Names.API_KEY, Names.API_IV));

        final URI remoteAddress = UriComponentsBuilder.fromHttpUrl(uri.toString().concat(path)).build().toUri();
        final String responseText = this.rpcTool.post(remoteAddress.toString(), params, headers);
        return OBJECT_MAPPER.readValue(responseText, new TypeReference<Result<?>>() {
        });
    }
}