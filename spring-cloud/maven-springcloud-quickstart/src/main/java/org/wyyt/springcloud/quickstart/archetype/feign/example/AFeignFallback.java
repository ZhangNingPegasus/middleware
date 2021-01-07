package org.wyyt.springcloud.quickstart.archetype.feign.example;

import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.rpc.Result;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * SpringCloud服务消费方代码, 建议服务提供方将此代码封装到jar包中
 */
@Component
public class AFeignFallback implements FallbackFactory<AFeign> {

    @Override
    public AFeign create(Throwable throwable) {
        return new AFeign() {
            @Override
            public Result<String> invoke(String value) {
                return Result.error(ExceptionTool.getRootCauseMessage(throwable));
            }

            @Override
            public Result<Future<String>> invokeAsync(String value) {
                return Result.ok(new Future<String>() {
                    @Override
                    public boolean cancel(boolean mayInterruptIfRunning) {
                        return false;
                    }

                    @Override
                    public boolean isCancelled() {
                        return false;
                    }

                    @Override
                    public boolean isDone() {
                        return true;
                    }

                    @Override
                    public String get() {
                        return ExceptionTool.getRootCauseMessage(throwable);
                    }

                    @Override
                    public String get(final long timeout,
                                      final TimeUnit unit) {
                        return ExceptionTool.getRootCauseMessage(throwable);
                    }
                });
            }

            @Override
            public Result<String> invokeThread(String value) {
                return Result.error(ExceptionTool.getRootCauseMessage(throwable));
            }

            @Override
            public Result<String> invokeThreadPool(String value) {
                return Result.error(ExceptionTool.getRootCauseMessage(throwable));
            }
        };
    }
}
