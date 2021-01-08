package springcloud.service.demo.rest;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.wyyt.tool.rpc.Result;
import springcloud.service.demo.feign.BFeignImpl;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

@RestController
@ConditionalOnProperty(name = DiscoveryConstant.SPRING_APPLICATION_NAME, havingValue = "a")
public class ARestImpl {
    private static final Logger LOG = LoggerFactory.getLogger(ARestImpl.class);

    @Value("${user.name.test}")
    private String userName;

    private final PluginAdapter pluginAdapter;
    private final RestTemplate restTemplate;
    private final Executor executor;

    public ARestImpl(final PluginAdapter pluginAdapter,
                     final RestTemplate restTemplate,
                     final Executor executor) {
        this.pluginAdapter = pluginAdapter;
        this.restTemplate = restTemplate;
        this.executor = executor;
    }

    @GetMapping(path = "/rest/{value}")
    public Result<String> rest(@PathVariable(value = "value") final String value) {
        return Result.ok(doInvoke(value));
    }

    @GetMapping(path = "/rest-async/{value}")
    @Async
    public Future<Result<String>> invokeAsync(@PathVariable(value = "value") final String value) {
        return new AsyncResult<>(Result.ok(doInvoke(value)));
    }

    @GetMapping(path = "/rest-thread/{value}")
    public Result<String> invokeThreadPool(@PathVariable final String value) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                doInvoke(value);
            }
        });
        return Result.ok("Invoke ThreadPool");
    }

    private String doInvoke(String value) {
        value = pluginAdapter.getPluginInfo(value);
        final Result<String> result = restTemplate.getForEntity("http://b/rest/" + value, Result.class).getBody();
        LOG.info(String.format("调用路径：{%s}", result.getData()));
        return result.getData();
    }
}