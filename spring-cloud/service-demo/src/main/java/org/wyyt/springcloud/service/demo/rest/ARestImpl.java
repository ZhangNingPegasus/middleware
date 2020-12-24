package org.wyyt.springcloud.service.demo.rest;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

@RestController
@ConditionalOnProperty(name = DiscoveryConstant.SPRING_APPLICATION_NAME, havingValue = "a")
public class ARestImpl {
    private static final Logger LOG = LoggerFactory.getLogger(ARestImpl.class);

    private final PluginAdapter pluginAdapter;
    private final RestTemplate restTemplate;

    public ARestImpl(PluginAdapter pluginAdapter, RestTemplate restTemplate) {
        this.pluginAdapter = pluginAdapter;
        this.restTemplate = restTemplate;
    }

    @GetMapping(path = "/rest/{value}")
    public String rest(@PathVariable(value = "value") String value, HttpServletRequest request) {
        value = pluginAdapter.getPluginInfo(value);
        value = restTemplate.getForEntity("http://b/rest/" + value, String.class).getBody();
        LOG.info("调用路径：{}", value);
        return value;
    }
}