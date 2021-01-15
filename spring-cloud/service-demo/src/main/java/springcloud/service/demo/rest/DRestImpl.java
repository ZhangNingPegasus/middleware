package springcloud.service.demo.rest;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
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

import javax.servlet.http.HttpServletRequest;

@Api("D的REST服务")
@RestController
@ConditionalOnProperty(name = DiscoveryConstant.SPRING_APPLICATION_NAME, havingValue = "d")
public class DRestImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DRestImpl.class);
    private final PluginAdapter pluginAdapter;

    public DRestImpl(PluginAdapter pluginAdapter) {
        this.pluginAdapter = pluginAdapter;
    }

    @ApiOperation(value = "rest同步调用示例")
    @ApiImplicitParam(name = "value", value = "参数1", required = true, dataType = "String")
    @GetMapping(path = "/rest/{value}")
    public Result<String> rest(@PathVariable(value = "value") String value, HttpServletRequest httpServletRequest) {
        value = pluginAdapter.getPluginInfo(value);
        LOG.info("调用路径：{}", value);
        return Result.ok(value);
    }
}