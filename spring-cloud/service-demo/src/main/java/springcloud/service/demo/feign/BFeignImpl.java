package springcloud.service.demo.feign;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import com.nepxion.discovery.plugin.strategy.context.StrategyContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
//    private final StudentServiceB studentServiceB;

    public BFeignImpl(final PluginAdapter pluginAdapter,
                      final StrategyContextHolder strategyContextHolder) {
        this.pluginAdapter = pluginAdapter;
        this.strategyContextHolder = strategyContextHolder;
//        this.studentServiceB = studentServiceB;
    }

    @Override
    public Result<String> invoke(String value) {
        value = pluginAdapter.getPluginInfo(value);
        log.info(String.format("调用路径：{%s}", value));
        return Result.ok("服务B的执行方法:" + value);
    }

    @Override
    public Result<String> uploadFile(MultipartFile file) throws IOException {
        file.transferTo(new File("E:\\aaa.txt"));
        return Result.ok();
    }
}