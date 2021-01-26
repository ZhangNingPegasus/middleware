package springcloud.service.demo.feign;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import com.nepxion.discovery.plugin.strategy.context.StrategyContextHolder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.wyyt.tool.rpc.Result;

import java.io.IOException;
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

    public AFeignImpl(final StrategyContextHolder strategyContextHolder,
                      final PluginAdapter pluginAdapter,
                      final BFeign bFeign,
                      final Executor executor) {
        this.strategyContextHolder = strategyContextHolder;
        this.pluginAdapter = pluginAdapter;
        this.bFeign = bFeign;
        this.executor = executor;
    }

    @Override
    public Result<String> invoke(String value,
                                 String a,
                                 String b,
                                 String c) throws Throwable {
//        this.studentServiceA.save(value);
        value = doInvoke(value, a, b, c);
        return Result.ok(value);
    }

    @Override
    @Async
    public Future<Result<String>> invokeAsync(String value,
                                              String a,
                                              String b,
                                              String c) {
        try {
            value = doInvoke(value, a, b, c);
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
    public Result<String> invokeThreadPool(String value,
                                           String a,
                                           String b,
                                           String c) {
        this.executor.execute(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                doInvoke(value, a, b, c);
            }
        });
        return Result.ok("Invoke ThreadPool");
    }

    @Override
    public Result<String> uploadFile(final MultipartFile file) throws IOException {
        bFeign.uploadFile(file);
        return Result.ok(String.valueOf(file.getBytes().length));
    }

    private String doInvoke(String value,
                            String a,
                            String b,
                            String c) throws Throwable {
//        log.info(strategyContextHolder.getHeader("access_token"));
//        value = pluginAdapter.getPluginInfo(value);
        Result<String> invoke = bFeign.invoke(value, a, b, c);
        if (invoke.getOk()) {
            value = invoke.getData();
            log.info(String.format("调用路径：{%s}", value));
            return value;
        } else {
            throw new Exception(invoke.getError());
        }
    }
}