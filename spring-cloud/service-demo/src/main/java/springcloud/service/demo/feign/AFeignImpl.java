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
import org.wyyt.springcloud.service.EnvironmentService;
import org.wyyt.tool.rpc.Result;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

@Slf4j
@RestController
@ConditionalOnProperty(name = DiscoveryConstant.SPRING_APPLICATION_NAME, havingValue = "a")
public class AFeignImpl implements AFeign {
    private final PluginAdapter pluginAdapter;
    private final BFeign bFeign;
    private final Executor executor;
    private final EnvironmentService environmentService;

    public AFeignImpl(final PluginAdapter pluginAdapter,
                      final BFeign bFeign,
                      final Executor executor,
                      final EnvironmentService environmentService) {
        this.pluginAdapter = pluginAdapter;
        this.bFeign = bFeign;
        this.executor = executor;
        this.environmentService = environmentService;
    }

    @Override
    public Result<String> invoke(String value) throws Throwable {
        System.out.println(this.environmentService.getClientId());
        System.out.println(this.environmentService.getClient());
        value = doInvoke(value);
        return Result.ok(value);
    }

    @Override
    @Async
    public Future<Result<String>> invokeAsync(String value) {
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
    public Result<String> invokeThreadPool(String value) {
        this.executor.execute(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                doInvoke(value);
            }
        });
        return Result.ok("Invoke ThreadPool");
    }

    @Override
    public Result<String> uploadFile(final MultipartFile file) throws IOException {
        bFeign.uploadFile(file);
        return Result.ok(String.valueOf(file.getBytes().length));
    }

    private String doInvoke(String value) throws Throwable {
        value = pluginAdapter.getPluginInfo(value);
        Result<String> invoke = bFeign.invoke(value);
        if (invoke.getOk()) {
            value = invoke.getData();
            log.info(String.format("调用路径：{%s}", value));
            return "服务A的执行方法: " + value;
        } else {
            throw new Exception(invoke.getError());
        }
    }
}