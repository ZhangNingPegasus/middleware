package org.wyyt.springcloud.service.demo.feign;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = DiscoveryConstant.SPRING_APPLICATION_NAME, havingValue = "b")
public class BFeignImpl implements BFeign {
    private static final Logger LOG = LoggerFactory.getLogger(BFeignImpl.class);


    private final PluginAdapter pluginAdapter;

    public BFeignImpl(PluginAdapter pluginAdapter) {
        this.pluginAdapter = pluginAdapter;
    }

    @Override
    public String invoke(@PathVariable(value = "value") String value) {
        value = pluginAdapter.getPluginInfo(value);
        LOG.info("调用路径：{}", value);
        return value;
    }
}