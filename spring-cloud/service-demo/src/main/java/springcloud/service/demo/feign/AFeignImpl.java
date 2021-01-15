package springcloud.service.demo.feign;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import com.nepxion.discovery.plugin.strategy.context.StrategyContextHolder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.wyyt.tool.rpc.Result;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

@Slf4j
@RestController
@ConditionalOnProperty(name = DiscoveryConstant.SPRING_APPLICATION_NAME, havingValue = "a")
public class AFeignImpl implements AFeign {
    private final StrategyContextHolder strategyContextHolder;
    private final PluginAdapter pluginAdapter;
    private final BFeign bFeign;
    private final Executor executor;

    public AFeignImpl(StrategyContextHolder strategyContextHolder, PluginAdapter pluginAdapter, BFeign bFeign, Executor executor) {
        this.strategyContextHolder = strategyContextHolder;
        this.pluginAdapter = pluginAdapter;
        this.bFeign = bFeign;
        this.executor = executor;
    }

    @Override
    public Result<String> invoke(@PathVariable(value = "value") String value) throws Throwable {
        value = doInvoke(value);
        return Result.ok(value);
    }

    @Override
    @Async
    public Future<Result<String>> invokeAsync(@PathVariable(value = "value") String value) {
        try {
            value = doInvoke(value);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        AsyncResult<Result<String>> result = new AsyncResult<>(Result.ok(value));
        try {
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Result<String> invokeThreadPool(@PathVariable(value = "value") String value) {
        this.executor.execute(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                doInvoke(value);
            }
        });
        return Result.ok("Invoke ThreadPool");
    }


    private String doInvoke(String value) throws Throwable {
        log.info(strategyContextHolder.getHeader("access_token"));

        value = pluginAdapter.getPluginInfo(value);
        Result<String> invoke = bFeign.invoke(value);
        if (invoke.getOk()) {
            value = invoke.getData();
            log.info(String.format("调用路径：{%s}", value));
            return value;
        } else {
            throw new Exception(invoke.getError());
        }
    }
}