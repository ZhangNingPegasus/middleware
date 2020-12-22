package org.wyyt.gateway.admin.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.wyyt.gateway.admin.business.contants.Names;
import org.wyyt.gateway.admin.business.exception.GatewayException;
import org.wyyt.gateway.admin.config.PropertyConfig;
import org.wyyt.gateway.admin.entity.EndpointVo;
import org.wyyt.gateway.admin.entity.ServiceVo;
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

    public EndpointVo getGatewayUri() throws Exception {
        final ServiceVo service = this.getService(this.propertyConfig.getGatewayConsulName());
        return service.getEndpointVoList().get(RandomUtils.nextInt(0, service.getEndpointVoList().size()));
    }

    public List<ServiceVo> listService() throws Exception {
        final List<ServiceVo> result = new ArrayList<>();
        for (final String serviceId : this.listServiceIds()) {
            result.add(this.getService(serviceId));
        }
        return result;
    }

    public ServiceVo getService(final String serviceId) throws Exception {
        final ServiceVo result = new ServiceVo();
        result.setName(serviceId);
        final List<EndpointVo> endpointVoList = new ArrayList<>();
        result.setEndpointVoList(endpointVoList);
        final String url = String.format("http://%s:%s/v1/health/service/%s",
                this.propertyConfig.getConulHost(),
                this.propertyConfig.getConsulPort(),
                serviceId);
        final String json = this.rpcTool.get(url);
        final JSONArray jsonArray = JSON.parseArray(json);

        if (null == jsonArray || jsonArray.isEmpty()) {
            return null;
        }

        for (final Object object : jsonArray) {
            if (object instanceof JSONObject) {
                final JSONObject jsonObject = (JSONObject) object;
                final Object service = jsonObject.get("Service");
                if (service instanceof JSONObject) {
                    final EndpointVo endpointVo = new EndpointVo();
                    final JSONObject jsonService = (JSONObject) service;
                    endpointVo.setAddress(jsonService.getString("Address"));
                    endpointVo.setPort(jsonService.getInteger("Port"));
                    final Object tags = jsonService.get("Tags");

                    if (tags instanceof JSONArray) {
                        final JSONArray jsonTags = (JSONArray) tags;
                        for (Object jsonTag : jsonTags) {
                            if (jsonTag instanceof String) {
                                final String tag = jsonTag.toString().trim();
                                final String[] all = tag.split("=");
                                if (null == all || all.length < 2) {
                                    continue;
                                } else if ("version".equals(all[0])) {
                                    endpointVo.setVersion(all[1]);
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

    public List<String> listServiceIds() {
        final List ignoredServiceNames = Arrays.asList("consul", this.propertyConfig.getServiceName(), this.propertyConfig.getGatewayConsulName());
        final List<String> services = this.discoveryClient.getServices();
        return services.stream().filter(p -> !ignoredServiceNames.contains(p))
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    public void refresh() throws Exception {
        final List<Exception> exceptionList = new ArrayList<>();
        final List<ServiceInstance> instances = this.discoveryClient.getInstances(propertyConfig.getGatewayConsulName());
        for (final ServiceInstance instance : instances) {
            try {
                final URI uri = instance.getUri();
                final Result respondResult = this.rpc(instance.getUri(), REFRESH_URL, null);
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
        final URI uri = this.loadBalancerClient.choose(propertyConfig.getGatewayConsulName()).getUri();
        final Result respondResult = this.rpc(uri, LIST_ROUTES_URL, null);
        return (List<String>) ConvertUtils.convert(respondResult.getData(), List.class);
    }

    private Result rpc(final URI uri,
                       final String path,
                       Map<String, Object> params) throws Exception {
        if (null == params) {
            params = new HashMap<>();
        }
        final Map<String, String> headers = new HashMap<>();
        headers.put("sign", SignTool.sign(params, Names.API_KEY, Names.API_IV));

        final URI remoteAddress = UriComponentsBuilder.fromHttpUrl(uri.toString().concat(path)).build().toUri();
        final String responseText = this.rpcTool.post(remoteAddress.toString(), params, headers);
        return OBJECT_MAPPER.readValue(responseText, new TypeReference<Result>() {
        });
    }
}