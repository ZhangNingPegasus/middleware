package springcloud.service.demo.feign;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import com.nepxion.discovery.plugin.strategy.context.StrategyContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.wyyt.tool.rpc.Result;

import java.io.File;
import java.io.IOException;

@Slf4j
@RestController
@ConditionalOnProperty(name = DiscoveryConstant.SPRING_APPLICATION_NAME, havingValue = "b")
public class BFeignImpl implements BFeign {
    private final PluginAdapter pluginAdapter;
    private final StrategyContextHolder strategyContextHolder;

    public BFeignImpl(PluginAdapter pluginAdapter, StrategyContextHolder strategyContextHolder) {
        this.pluginAdapter = pluginAdapter;
        this.strategyContextHolder = strategyContextHolder;
    }

    @Override
    public Result<String> invoke(@PathVariable(value = "value") String value) {
        log.info(strategyContextHolder.getHeader("access_token"));
        value = pluginAdapter.getPluginInfo(value);
        log.info(String.format("调用路径：{%s}", value));
        return Result.ok(value);
    }

    @Override
    public Result<String> uploadFile(MultipartFile file) throws IOException {
        file.transferTo(new File("E:\\aaa.txt"));
        return Result.ok();
    }
}