package org.wyyt.springcloud.service.demo.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.wyyt.tool.rpc.Result;

@FeignClient(value = "b", fallbackFactory = BFeignFallback.class)
public interface BFeign {
    @GetMapping(path = "/feign/{value}")
    Result<String> invoke(@PathVariable(value = "value") String value) throws Throwable;
}