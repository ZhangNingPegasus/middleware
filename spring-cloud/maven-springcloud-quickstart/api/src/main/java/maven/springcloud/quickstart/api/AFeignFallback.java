package maven.springcloud.quickstart.api;

import feign.hystrix.FallbackFactory;
import maven.springcloud.quickstart.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.rpc.Result;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 容错实现
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Component
public class AFeignFallback implements FallbackFactory<AFeign> {
    @Override
    public AFeign create(final Throwable throwable) {
        return new AFeign() {
            @Override
            public Result<User> invoke(final String value) {
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

            @Override
            public Result<String> uploadFile(final MultipartFile file) {
                return Result.error(ExceptionTool.getRootCauseMessage(throwable));
            }
        };
    }
}