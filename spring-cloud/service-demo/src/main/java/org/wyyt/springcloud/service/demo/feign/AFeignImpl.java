package org.wyyt.springcloud.service.demo.feign;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RestController
@ConditionalOnProperty(name = DiscoveryConstant.SPRING_APPLICATION_NAME, havingValue = "a")
public class AFeignImpl implements AFeign {
    private static final Logger LOG = LoggerFactory.getLogger(AFeignImpl.class);

    private final PluginAdapter pluginAdapter;
    private final BFeign bFeign;

    private final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    public AFeignImpl(PluginAdapter pluginAdapter, BFeign bFeign) {
        this.pluginAdapter = pluginAdapter;
        this.bFeign = bFeign;
    }

    @Override
    public String invoke(@PathVariable(value = "value") String value) {
        value = doInvoke(value);
        LOG.info(value);
        return value;
    }

    @Override
    @Async
    public Future<String> invokeAsync(@PathVariable(value = "value") String value) {
        try {
            value = doInvoke(value);
        } catch (Exception e) {
            e.printStackTrace();
        }

        AsyncResult<String> result = new AsyncResult<>(value);
        try {
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String invokeThread(@PathVariable(value = "value") String value) {
        Runnable runnable = createRunnable(value);
        new Thread(runnable).start();
        return "Invoke Thread";
    }

    @Override
    public String invokeThreadPool(String value) {
        Runnable runnable = createRunnable(value);
        cachedThreadPool.execute(runnable);
        return "Invoke ThreadPool";
    }

    private Runnable createRunnable(String value) {
        return () -> doInvoke(value);
    }

    private String doInvoke(String value) {
        value = pluginAdapter.getPluginInfo(value);
        value = bFeign.invoke(value);
        LOG.info("调用路径：{}", value);
        return value;
    }
}