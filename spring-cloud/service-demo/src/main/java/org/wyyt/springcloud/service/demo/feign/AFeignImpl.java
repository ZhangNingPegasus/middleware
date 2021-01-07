package org.wyyt.springcloud.service.demo.feign;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.wyyt.tool.rpc.Result;

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
    public Result<String> invoke(@PathVariable(value = "value") String value) throws Throwable {
        value = doInvoke(value);
        LOG.info(value);
        return Result.ok(value);
    }

    @Override
    @Async
    public Result<Future<String>> invokeAsync(@PathVariable(value = "value") String value) {
        try {
            value = doInvoke(value);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        AsyncResult<String> result = new AsyncResult<>(value);
        try {
            return Result.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Result<String> invokeThread(@PathVariable(value = "value") String value) {
        Runnable runnable = createRunnable(value);
        new Thread(runnable).start();
        return Result.ok("Invoke Thread");
    }

    @Override
    public Result<String> invokeThreadPool(@PathVariable(value = "value") String value) {
        Runnable runnable = createRunnable(value);
        cachedThreadPool.execute(runnable);
        return Result.ok("Invoke ThreadPool");
    }

    private Runnable createRunnable(String value) {
        return new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                doInvoke(value);
            }
        };
    }

    private String doInvoke(String value) throws Throwable {
        value = pluginAdapter.getPluginInfo(value);
        value = bFeign.invoke(value).getData();
        LOG.info("调用路径：{}", value);
        return value;
    }
}