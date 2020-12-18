package org.wyyt.services.feign;

import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Component
public class AFeignFallback implements FallbackFactory<AFeign> {
    @Override
    public AFeign create(Throwable throwable) {
        return new AFeign() {
            @Override
            public String invoke(String value) {
                return "服务器正忙，请稍后再试1";
            }

            @Override
            public Future<String> invokeAsync(String value) {
                return new Future<String>() {
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
                        return "服务器正忙，请稍后再试1";
                    }

                    @Override
                    public String get(long timeout, TimeUnit unit) {
                        return "服务器正忙，请稍后再试1";
                    }
                };
            }

            @Override
            public String invokeThread(String value) {
                return "服务器正忙，请稍后再试1";
            }

            @Override
            public String invokeThreadPool(String value) {
                return "服务器正忙，请稍后再试1";
            }
        };
    }
}
