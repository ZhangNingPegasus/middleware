package maven.springcloud.quickstart.feign.example;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.wyyt.tool.rpc.Result;

import java.util.concurrent.Future;

/**
 * SpringCloud服务消费方代码, 建议服务提供方将此代码封装到jar包中, 这样, 服务消费方直接注入即可
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        01/01/2021        Initialize  *
 * *****************************************************************
 */
@FeignClient(value = "maven-springcloud-quickstart", fallbackFactory = AFeignFallback.class)
public interface AFeign {
    /**
     * 同步调用
     *
     * @param value 参数
     * @return
     */
    @GetMapping(path = "/feign/{value}")
    Result<String> invoke(@PathVariable(value = "value") final String value);

    /**
     * 注解方式(@Async)的异步调用
     *
     * @param value 参数
     * @return
     */
    @GetMapping(path = "/feign-async/{value}")
    Future<Result<String>> invokeAsync(@PathVariable(value = "value") final String value);

    /**
     * 线程方式的异步调用
     *
     * @param value 参数
     * @return
     */
    @GetMapping(path = "/feign-thread/{value}")
    Result<String> invokeThread(@PathVariable(value = "value") final String value);
}