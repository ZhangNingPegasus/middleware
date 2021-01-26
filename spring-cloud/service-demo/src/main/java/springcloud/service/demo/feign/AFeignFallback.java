package springcloud.service.demo.feign;

import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.rpc.Result;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Component
public class AFeignFallback implements FallbackFactory<AFeign> {
    @Override
    public AFeign create(Throwable throwable) {
        return new AFeign() {
            @Override
            public Result<String> invoke(String value,
                                         String a,
                                         String b,
                                         String c) {
                return Result.error(ExceptionTool.getRootCauseMessage(throwable));
            }

            @Override
            public Future<Result<String>> invokeAsync(String value,
                                                      String a,
                                                      String b,
                                                      String c) {
                return new Future<Result<String>>() {
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
                    public Result<String> get() {
                        return Result.ok("服务器正忙，请稍后再试");
                    }

                    @Override
                    public Result<String> get(long timeout, TimeUnit unit) {
                        return Result.ok(ExceptionTool.getRootCauseMessage(throwable));
                    }
                };
            }

            @Override
            public Result<String> invokeThreadPool(String value,
                                                   String a,
                                                   String b,
                                                   String c) {
                return Result.error(ExceptionTool.getRootCauseMessage(throwable));
            }

            @Override
            public Result<String> uploadFile(MultipartFile file) {
                return Result.error(ExceptionTool.getRootCauseMessage(throwable));
            }
        };
    }
}
