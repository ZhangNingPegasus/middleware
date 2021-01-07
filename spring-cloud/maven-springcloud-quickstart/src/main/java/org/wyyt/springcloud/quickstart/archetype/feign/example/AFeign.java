package org.wyyt.springcloud.quickstart.archetype.feign.example;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.wyyt.tool.rpc.Result;

import java.util.concurrent.Future;

/**
 * SpringCloud服务消费方代码, 建议服务提供方将此代码封装到jar包中, 这样, 服务消费方直接注入即可
 */
@FeignClient(value = "a", fallbackFactory = AFeignFallback.class)
public interface AFeign {
    // 同步调用
    @GetMapping(path = "/feign/{value}")
    Result<String> invoke(@PathVariable(value = "value") String value);

    // @Async注解方式的异步调用
    @GetMapping(path = "/feign-async/{value}")
    Result<Future<String>> invokeAsync(@PathVariable(value = "value") String value);

    // 单线程方式的异步调用（不准, Header会失效）
    @GetMapping(path = "/feign-thread/{value}")
    Result<String> invokeThread(@PathVariable(value = "value") String value);

    // 线程池方式的异步调用（不准, Header会失效）
    @GetMapping(path = "/feign-threadpool/{value}")
    Result<String> invokeThreadPool(@PathVariable(value = "value") String value);
}