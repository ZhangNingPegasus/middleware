package springcloud.service.demo.rest;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import com.nepxion.discovery.plugin.strategy.context.StrategyContextHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.wyyt.tool.rpc.Result;

@Api("B的REST服务")
@Slf4j
@RestController
@ConditionalOnProperty(name = DiscoveryConstant.SPRING_APPLICATION_NAME, havingValue = "b")
public class BRestImpl {
    private static final Logger LOG = LoggerFactory.getLogger(BRestImpl.class);
    private final PluginAdapter pluginAdapter;
    private final StrategyContextHolder strategyContextHolder;

    public BRestImpl(PluginAdapter pluginAdapter, StrategyContextHolder strategyContextHolder) {
        this.pluginAdapter = pluginAdapter;
        this.strategyContextHolder = strategyContextHolder;
    }

    @ApiOperation(value = "rest同步调用示例")
    @ApiImplicitParam(name = "value", value = "参数1", required = true, dataType = "String")
    @GetMapping(path = "/rest/{value}")
    public Result<String> rest(@PathVariable(value = "value") String value) {
        log.info(strategyContextHolder.getHeader("access_token"));
        value = pluginAdapter.getPluginInfo(value);
        LOG.info("调用路径：{}", value);
        return Result.ok(value);
    }
}