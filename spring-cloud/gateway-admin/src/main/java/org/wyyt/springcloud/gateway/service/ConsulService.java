package org.wyyt.springcloud.gateway.service;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.HealthServicesRequest;
import com.ecwid.consul.v1.health.model.Check;
import com.ecwid.consul.v1.health.model.HealthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.springcloud.gateway.config.PropertyConfig;
import org.wyyt.springcloud.gateway.entity.EndpointVo;
import org.wyyt.springcloud.gateway.entity.ServiceVo;
import org.wyyt.springcloud.gateway.entity.contants.Names;
import org.wyyt.tool.exception.BusinessException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The service of RPC for consul
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
public class ConsulService {
    private final PropertyConfig propertyConfig;
    private final ConsulClient consulClient;

    public ConsulService(final PropertyConfig propertyConfig,
                         final ConsulClient consulClient) {
        this.propertyConfig = propertyConfig;
        this.consulClient = consulClient;
    }

    public ServiceVo getService(final String serviceName,
                                final boolean onlyAlive) {
        final ServiceVo result = new ServiceVo();
        result.setName(serviceName);
        result.setEndpointVoList(new ArrayList<>());

        final HealthServicesRequest healthServicesRequest = HealthServicesRequest.newBuilder().build();
        final Response<List<HealthService>> healthServices = this.consulClient.getHealthServices(serviceName, healthServicesRequest);
        if (null == healthServices) {
            return result;
        }

        for (final HealthService healthService : healthServices.getValue()) {
            final HealthService.Service service = healthService.getService();
            final List<Check> checks = healthService.getChecks();

            final EndpointVo endpointVo = new EndpointVo();
            endpointVo.setId(service.getId());
            endpointVo.setAddress(service.getAddress());
            endpointVo.setPort(service.getPort());
            endpointVo.setVersion(service.getMeta().get(Names.VERSION));
            endpointVo.setGroup(service.getMeta().get(Names.GROUP));
            endpointVo.setAlive(false);
            for (final Check check : checks) {
                if (check.getServiceId().equals(service.getId())) {
                    if (check.getStatus() == Check.CheckStatus.PASSING) {
                        endpointVo.setAlive(true);
                    }
                    break;
                }
            }
            if (onlyAlive) {
                if (endpointVo.getAlive()) {
                    result.getEndpointVoList().add(endpointVo);
                }
            } else {
                result.getEndpointVoList().add(endpointVo);
            }
        }
        return result;
    }

    public List<ServiceVo> listService() {
        final List<String> ignoredServiceNames = Arrays.asList(this.propertyConfig.getServiceName(), this.propertyConfig.getGatewayConsulName());
        final List<ServiceVo> result = new ArrayList<>();
        final Response<Map<String, com.ecwid.consul.v1.agent.model.Service>> services = consulClient.getAgentServices();

        if (null == services) {
            return result;
        }

        final Map<String, List<com.ecwid.consul.v1.agent.model.Service>> serviceMap = new HashMap<>((int) (services.getValue().size() / 0.75));
        for (final Map.Entry<String, com.ecwid.consul.v1.agent.model.Service> pair : services.getValue().entrySet()) {
            final com.ecwid.consul.v1.agent.model.Service service = pair.getValue();
            final String key = service.getService();
            if (ignoredServiceNames.contains(key)) {
                continue;
            }
            if (serviceMap.containsKey(key)) {
                serviceMap.get(key).add(service);
            } else {
                List<com.ecwid.consul.v1.agent.model.Service> serviceList = new ArrayList<>();
                serviceList.add(service);
                serviceMap.put(key, serviceList);
            }
        }

        for (final Map.Entry<String, List<com.ecwid.consul.v1.agent.model.Service>> pair : serviceMap.entrySet()) {
            final ServiceVo serviceVo = new ServiceVo();
            serviceVo.setName(pair.getKey());
            serviceVo.setEndpointVoList(new ArrayList<>());
            final List<com.ecwid.consul.v1.agent.model.Service> value = pair.getValue();

            for (final com.ecwid.consul.v1.agent.model.Service service : value) {
                final EndpointVo endpointVo = new EndpointVo();
                endpointVo.setId(service.getId());
                endpointVo.setAddress(service.getAddress());
                endpointVo.setPort(service.getPort());
                endpointVo.setVersion(service.getMeta().get(Names.VERSION));
                endpointVo.setGroup(service.getMeta().get(Names.GROUP));
                serviceVo.getEndpointVoList().add(endpointVo);
            }
            result.add(serviceVo);
        }
        return result.stream().sorted(Comparator.comparing(ServiceVo::getName)).collect(Collectors.toList());
    }

    public List<String> listServiceNames() {
        List<String> result = new ArrayList<>();
        final List<ServiceVo> serviceVos = this.listService();

        if (null == serviceVos || serviceVos.isEmpty()) {
            return result;
        }

        return serviceVos.stream().map(ServiceVo::getName).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
    }

    public List<URI> getServiceUris(final String serviceName) {
        final ServiceVo serviceVo = this.getService(serviceName, true);
        if (null == serviceVo) {
            return null;
        }
        final List<EndpointVo> endpointVoList = serviceVo.getEndpointVoList();
        if (endpointVoList.isEmpty()) {
            return null;
        }

        return endpointVoList.stream().map(p -> {
            try {
                return new URI(String.format("http://%s:%s", p.getAddress(), p.getPort()));
            } catch (URISyntaxException e) {
                throw new BusinessException(e);
            }
        }).collect(Collectors.toList());
    }

    public URI getServiceUri(final String serviceName) {
        final List<URI> serviceUris = this.getServiceUris(serviceName);
        if (null == serviceUris || serviceUris.isEmpty()) {
            return null;
        }
        return serviceUris.get(RandomUtils.nextInt(0, serviceUris.size()));
    }

    public URI getGatewayUri() throws URISyntaxException {
        final String gatewayUrl = this.propertyConfig.getGatewayUrl();
        if (ObjectUtils.isEmpty(gatewayUrl)) {
            return this.getServiceUri(this.propertyConfig.getGatewayConsulName());
        }
        return new URI(gatewayUrl);
    }

}