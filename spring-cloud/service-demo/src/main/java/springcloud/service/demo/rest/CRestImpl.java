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
import org.springframework.web.client.RestTemplate;
import org.wyyt.tool.rpc.Result;

import javax.servlet.http.HttpServletRequest;

@Api("C的REST服务")
@RestController
@ConditionalOnProperty(name = DiscoveryConstant.SPRING_APPLICATION_NAME, havingValue = "c")
public class CRestImpl {
    private static final Logger LOG = LoggerFactory.getLogger(CRestImpl.class);
    private final PluginAdapter pluginAdapter;
    private final RestTemplate restTemplate;

    public CRestImpl(PluginAdapter pluginAdapter, RestTemplate restTemplate) {
        this.pluginAdapter = pluginAdapter;
        this.restTemplate = restTemplate;
    }

    @ApiOperation(value = "rest同步调用示例")
    @ApiImplicitParam(name = "value", value = "参数1", required = true, dataType = "String")
    @GetMapping(path = "/rest/{value}")
    public Result<String> rest(@PathVariable(value = "value") String value, HttpServletRequest request) {
        value = pluginAdapter.getPluginInfo(value);
        final Result<String> result = restTemplate.getForEntity("http://d/rest/" + value, Result.class).getBody();
        LOG.info(String.format("调用路径：{%s}", result.getData()));
        return Result.ok(value);
    }
}