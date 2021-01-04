package org.wyyt.springcloud.quickstart.archetype.feign.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 服务提供方代码
 */
@Slf4j
@RestController
public class AFeignImpl implements AFeign {
    private final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @Override
    public String invoke(@PathVariable(value = "value") String value) {
        value = doInvoke(value);
        log.info(value);
        return value;
    }

    @Override
    @Async
    public Future<String> invokeAsync(@PathVariable(value = "value") String value) {
        try {
            value = doInvoke(value);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        AsyncResult<String> result = new AsyncResult<>(value);
        try {
            return result;
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String invokeThread(@PathVariable(value = "value") final String value) {
        final Runnable runnable = createRunnable(value);
        new Thread(runnable).start();
        return "Invoke Thread";
    }

    @Override
    public String invokeThreadPool(final String value) {
        final Runnable runnable = createRunnable(value);
        cachedThreadPool.execute(runnable);
        return "Invoke ThreadPool";
    }

    private Runnable createRunnable(final String value) {
        return () -> doInvoke(value);
    }

    private String doInvoke(final String value) {
        String result = value.concat(" -> 服务A");
        log.info("调用: {}", result);
        return result;
    }
}