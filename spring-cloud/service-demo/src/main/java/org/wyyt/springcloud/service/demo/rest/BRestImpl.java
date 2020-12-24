package org.wyyt.springcloud.service.demo.rest;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@ConditionalOnProperty(name = DiscoveryConstant.SPRING_APPLICATION_NAME, havingValue = "b")
public class BRestImpl {
    private static final Logger LOG = LoggerFactory.getLogger(BRestImpl.class);

    private final PluginAdapter pluginAdapter;

    public BRestImpl(PluginAdapter pluginAdapter) {
        this.pluginAdapter = pluginAdapter;
    }

    @GetMapping(path = "/rest/{value}")
    public String rest(@PathVariable(value = "value") String value, HttpServletRequest httpServletRequest) {
        value = pluginAdapter.getPluginInfo(value);
        LOG.info("调用路径：{}", value);
        return value;
    }
}