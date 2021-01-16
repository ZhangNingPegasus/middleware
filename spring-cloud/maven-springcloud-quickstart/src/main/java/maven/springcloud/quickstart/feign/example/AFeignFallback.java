package maven.springcloud.quickstart.feign.example;

import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.rpc.Result;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 兜底实现,用于熔断。建议服务提供方将此代码封装到jar包中
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Component
public class AFeignFallback implements FallbackFactory<AFeign> {
    @Override
    public AFeign create(final Throwable throwable) {
        return new AFeign() {
            @Override
            public Result<String> invoke(final String value) {
                return Result.error(ExceptionTool.getRootCauseMessage(throwable));
            }

            @Override
            public Future<Result<String>> invokeAsync(final String value) {
                return new Future<Result<String>>() {
                    @Override
                    public boolean cancel(final boolean mayInterruptIfRunning) {
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
                    public Result<String> get() {
                        return Result.ok(ExceptionTool.getRootCauseMessage(throwable));
                    }

                    @Override
                    public Result<String> get(final long timeout,
                                              final TimeUnit unit) {
                        return Result.ok(ExceptionTool.getRootCauseMessage(throwable));
                    }
                };
            }

            @Override
            public Result<String> invokeThread(final String value) {
                return Result.error(ExceptionTool.getRootCauseMessage(throwable));
            }
        };
    }
}