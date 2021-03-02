package org.wyyt.springcloud.common.service;

import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.wyyt.tool.rpc.RpcService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * The service of Gateway
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public class GatewayService {
    private final ConsulService consulService;
    private final RpcService rpcService;

    public GatewayService(final ConsulService consulService,
                          final RpcService rpcService) {
        this.consulService = consulService;
        this.rpcService = rpcService;
    }

    public <T> T post(final String url,
                      final Map<String, String> headerMap,
                      final Map<String, Object> paramMap,
                      final TypeReference<T> typeReference) {
        return this.rpcService.postForEntity(url, headerMap, paramMap, typeReference);
    }

    public <T> T post(final String url,
                      final Map<String, Object> paramMap,
                      final TypeReference<T> typeReference) {
        return this.post(url, null, paramMap, typeReference);
    }

    public void post(final String url,
                     final Map<String, Object> paramMap) {
        this.post(url, paramMap, null);
    }

    public <T> T post(final String serviceName,
                      final String servicePath,
                      final Map<String, String> headerMap,
                      final Map<String, Object> paramMap,
                      final TypeReference<T> typeReference) throws Exception {
        final String remoteUrl = this.getRemoteUrl(serviceName, servicePath);
        return this.post(remoteUrl, headerMap, paramMap, typeReference);
    }

    public <T> T post(final String serviceName,
                      final String servicePath,
                      final TypeReference<T> typeReference) throws URISyntaxException {
        final String remoteUrl = this.getRemoteUrl(serviceName, servicePath);
        return this.post(remoteUrl, new HashMap<>(), new HashMap<>(), typeReference);
    }

    public <T> T post(final String serviceName,
                      final String serviceUrl,
                      final Map<String, String> headerMap,
                      final TypeReference<T> typeReference) throws Exception {
        final String remoteUrl = this.getRemoteUrl(serviceName, serviceUrl);
        return this.rpcService.postForEntity(remoteUrl, headerMap, null, typeReference);
    }

    public String getRemoteUrl(final String serviceName,
                               String serviceUrl) throws URISyntaxException {
        final URI gatewayUrl = this.consulService.getGatewayUri();
        if (null == gatewayUrl) {
            return null;
        }
        final URI serviceUri = this.consulService.getServiceUri(serviceName);
        if (null == serviceUri) {
            return null;
        }

        if (ObjectUtils.isEmpty(serviceUrl)) {
            serviceUrl = "";
        }

        String strGatewayUrl = gatewayUrl.toString().trim();

        if (strGatewayUrl.startsWith("/")) {
            strGatewayUrl = strGatewayUrl.substring(1);
        }
        if (strGatewayUrl.endsWith("/")) {
            strGatewayUrl = strGatewayUrl.substring(0, strGatewayUrl.length() - 1);
        }

        if (serviceUrl.startsWith("/")) {
            serviceUrl = serviceUrl.substring(1);
        }
        if (serviceUrl.endsWith("/")) {
            serviceUrl = serviceUrl.substring(0, strGatewayUrl.length() - 1);
        }

        return URI.create(String.format("%s/%s/%s", strGatewayUrl, serviceName, serviceUrl)).toString();
    }

}