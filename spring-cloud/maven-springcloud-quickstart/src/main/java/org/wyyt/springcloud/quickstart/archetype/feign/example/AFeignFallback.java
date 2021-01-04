package org.wyyt.springcloud.quickstart.archetype.feign.example;

import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * SpringCloud服务消费方代码, 建议服务提供方将此代码封装到jar包中
 */
@Component
public class AFeignFallback implements FallbackFactory<AFeign> {
    private static final String STR_FALL_BACK = "服务器正忙，请稍后再试";

    @Override
    public AFeign create(Throwable throwable) {
        return new AFeign() {
            @Override
            public String invoke(String value) {
                return STR_FALL_BACK;
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
                        return STR_FALL_BACK;
                    }

                    @Override
                    public String get(long timeout, TimeUnit unit) {
                        return STR_FALL_BACK;
                    }
                };
            }

            @Override
            public String invokeThread(String value) {
                return STR_FALL_BACK;
            }

            @Override
            public String invokeThreadPool(String value) {
                return STR_FALL_BACK;
            }
        };
    }
}
