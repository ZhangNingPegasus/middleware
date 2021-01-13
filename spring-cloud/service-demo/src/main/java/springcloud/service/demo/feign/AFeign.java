package springcloud.service.demo.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wyyt.tool.rpc.Result;

import java.util.concurrent.Future;

@FeignClient(value = "a", fallbackFactory = AFeignFallback.class)
public interface AFeign {
    // 同步调用
    @GetMapping(path = "/feign/{value}")
    Result<String> invoke(@PathVariable(value = "value") String value) throws Throwable;

    // @Async注解方式的异步调用
    @GetMapping(path = "/feign-async/{value}")
    Future<Result<String>> invokeAsync(@PathVariable(value = "value") String value);

    // 线程池方式的异步调用
    @GetMapping(path = "/feign-thread/{value}")
    Result<String> invokeThreadPool(@PathVariable(value = "value") String value);
}