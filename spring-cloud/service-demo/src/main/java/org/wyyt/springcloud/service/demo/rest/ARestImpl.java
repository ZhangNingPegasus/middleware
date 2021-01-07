package org.wyyt.springcloud.service.demo.rest;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
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

import java.util.concurrent.Future;

@RestController
@ConditionalOnProperty(name = DiscoveryConstant.SPRING_APPLICATION_NAME, havingValue = "a")
public class ARestImpl {
    private static final Logger LOG = LoggerFactory.getLogger(ARestImpl.class);

    @Value("${user.name.test}")
    private String userName;

    private final PluginAdapter pluginAdapter;
    private final RestTemplate restTemplate;

    public ARestImpl(PluginAdapter pluginAdapter, RestTemplate restTemplate) {
        this.pluginAdapter = pluginAdapter;
        this.restTemplate = restTemplate;
    }

    @GetMapping(path = "/rest/{value}")
    public String rest(@PathVariable(value = "value") String value) {
        return doInvoke(value);
    }

    @GetMapping(path = "/rest-async/{value}")
    @Async
    public Future<String> invokeAsync(@PathVariable(value = "value") String value) {
        value = doInvoke(value);
        return new AsyncResult<>(value);
    }

    @GetMapping(path = "/rest-thread/{value}")
    public String invokeThread(@PathVariable(value = "value") String value) {
        Runnable runnable = createRunnable(value);
        new Thread(runnable).start();
        return "Invoke Thread";
    }

    @GetMapping(path = "/rest-threadpool/{value}")
    public String invokeThreadPool(@PathVariable String value) {
        Runnable runnable = createRunnable(value);
        return "Invoke ThreadPool";
    }

    private Runnable createRunnable(String value) {
        return new Runnable() {
            @Override
            public void run() {
                doInvoke(value);
            }
        };
    }

    private String doInvoke(String value) {
        value = pluginAdapter.getPluginInfo(value);
        value = restTemplate.getForEntity("http://b/rest/" + value, String.class).getBody();
        LOG.info("调用路径：{}", value);
        return value;
    }
}