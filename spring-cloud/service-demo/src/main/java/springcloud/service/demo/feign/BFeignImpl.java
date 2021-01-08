package springcloud.service.demo.feign;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.wyyt.tool.rpc.Result;

@Slf4j
@RestController
@ConditionalOnProperty(name = DiscoveryConstant.SPRING_APPLICATION_NAME, havingValue = "b")
public class BFeignImpl implements BFeign {
    private final PluginAdapter pluginAdapter;

    public BFeignImpl(PluginAdapter pluginAdapter) {
        this.pluginAdapter = pluginAdapter;
    }

    @Override
    public Result<String> invoke(@PathVariable(value = "value") String value) throws InterruptedException {
        value = pluginAdapter.getPluginInfo(value);
        log.info(String.format("调用路径：{%s}", value));
        return Result.ok(value);
    }
}