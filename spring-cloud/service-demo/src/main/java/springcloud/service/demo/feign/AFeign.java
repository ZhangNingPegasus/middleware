package springcloud.service.demo.feign;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.wyyt.tool.rpc.Result;

import java.io.IOException;
import java.util.concurrent.Future;

@Api("A的Feign服务")
@FeignClient(value = "a", fallbackFactory = AFeignFallback.class)
public interface AFeign {
    // 同步调用
    @ApiOperation(value = "feign同步调用示例")
    @ApiImplicitParam(name = "value", value = "参数1", required = true, dataType = "String")
    @RequestMapping(path = "/feign/{value}")
    Result<String> invoke(@PathVariable(value = "value") String value,
                          @RequestParam(value = "a", required = false) String a,
                          @RequestParam(value = "b", required = false) String b,
                          @RequestParam(value = "c", required = false) String c) throws Throwable;

    // @Async注解方式的异步调用
    @ApiOperation(value = "feign异步调用示例")
    @ApiImplicitParam(name = "value", value = "参数1", required = true, dataType = "String")
    @GetMapping(path = "/feign-async/{value}")
    Future<Result<String>> invokeAsync(@PathVariable(value = "value") String value,
                                       @RequestParam("a") String a,
                                       @RequestParam("b") String b,
                                       @RequestParam("c") String c);

    // 线程池方式的异步调用
    @ApiOperation(value = "feign线程调用示例")
    @ApiImplicitParam(name = "value", value = "参数1", required = true, dataType = "String")
    @GetMapping(path = "/feign-thread/{value}")
    Result<String> invokeThreadPool(@PathVariable(value = "value") String value,
                                    @RequestParam("a") String a,
                                    @RequestParam("b") String b,
                                    @RequestParam("c") String c);

    // 传文件
    @PostMapping(value = "feign-uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<String> uploadFile(@RequestPart("file") MultipartFile file) throws IOException;
}