package springcloud.service.demo.feign;


import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.rpc.Result;

@Component
public class BFeignFallback implements FallbackFactory<BFeign> {
    @Override
    public BFeign create(Throwable throwable) {
        return new BFeign() {
            @Override
            public Result<String> invoke(String value) {
                return Result.error(ExceptionTool.getRootCauseMessage(throwable));
            }
        };
    }
}
