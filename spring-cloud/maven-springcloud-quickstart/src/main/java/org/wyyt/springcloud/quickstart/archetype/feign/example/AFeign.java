package org.wyyt.springcloud.quickstart.archetype.feign.example;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.Future;

/**
 * SpringCloud服务消费方代码, 建议服务提供方将此代码封装到jar包中, 这样, 服务消费方直接注入即可
 */
@FeignClient(value = "a", fallbackFactory = AFeignFallback.class)
public interface AFeign {
    // 同步调用
    @GetMapping(path = "/feign/{value}")
    String invoke(@PathVariable(value = "value") String value);

    // @Async注解方式的异步调用
    @GetMapping(path = "/feign-async/{value}")
    Future<String> invokeAsync(@PathVariable(value = "value") String value);

    // 单线程方式的异步调用（不准, Header会失效）
    @GetMapping(path = "/feign-thread/{value}")
    String invokeThread(@PathVariable(value = "value") String value);

    // 线程池方式的异步调用（不准, Header会失效）
    @GetMapping(path = "/feign-threadpool/{value}")
    String invokeThreadPool(@PathVariable(value = "value") String value);
}