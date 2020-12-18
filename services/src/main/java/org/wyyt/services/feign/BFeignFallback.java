package org.wyyt.services.feign;


import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class BFeignFallback implements FallbackFactory<BFeign> {
    @Override
    public BFeign create(Throwable throwable) {
        return value -> "服务器正忙，请稍后再试2";
    }
}
