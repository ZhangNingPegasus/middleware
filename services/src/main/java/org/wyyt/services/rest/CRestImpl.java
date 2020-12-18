package org.wyyt.services.rest;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

@RestController
@ConditionalOnProperty(name = DiscoveryConstant.SPRING_APPLICATION_NAME, havingValue = "c")
public class CRestImpl {
    private static final Logger LOG = LoggerFactory.getLogger(CRestImpl.class);

    @Autowired
    private PluginAdapter pluginAdapter;
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping(path = "/rest/{value}")
    public String rest(@PathVariable(value = "value") String value, HttpServletRequest request) {
        value = pluginAdapter.getPluginInfo(value);
        value = restTemplate.getForEntity("http://d/rest/" + value, String.class).getBody();
        LOG.info("调用路径：{}", value);
        return value;
    }
}