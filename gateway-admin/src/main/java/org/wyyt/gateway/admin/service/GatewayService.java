package org.wyyt.gateway.admin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.wyyt.gateway.admin.business.contants.Names;
import org.wyyt.gateway.admin.business.exception.GatewayException;
import org.wyyt.gateway.admin.config.PropertyConfig;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.rpc.Result;
import org.wyyt.tool.rpc.RpcTool;
import org.wyyt.tool.rpc.SignTool;

import java.net.URI;
import java.util.*;

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

    public List<String> listServiceNames() {
        final List<String> services = this.discoveryClient.getServices();
        services.sort(Comparator.naturalOrder());
        return services;
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