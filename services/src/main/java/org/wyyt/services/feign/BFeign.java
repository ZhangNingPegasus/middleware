package org.wyyt.services.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "b", fallbackFactory = BFeignFallback.class)
public interface BFeign {
    @GetMapping(path = "/feign/{value}")
    String invoke(@PathVariable(value = "value") String value);
}