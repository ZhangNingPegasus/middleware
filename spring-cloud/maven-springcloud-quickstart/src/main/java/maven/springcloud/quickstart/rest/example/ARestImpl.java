package maven.springcloud.quickstart.rest.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.wyyt.tool.rpc.Result;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * 服务实现(Rest方式), 服务提供方代码
 * 注意:
 * 使用多线程时, 必须先注入Executor线程池类, 使用该线程池的execute方法进行多线程的调用, 并且在创建过程中
 * 禁止使用匿名类、lambda表达式
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        01/01/2021        Initialize  *
 * *****************************************************************
 */
@Slf4j
@RestController
public class ARestImpl {
    private final RestTemplate restTemplate;
    private final Executor executor;

    public ARestImpl(final RestTemplate restTemplate,
                     final Executor executor) {
        this.restTemplate = restTemplate;
        this.executor = executor;
    }

    @GetMapping(path = "/rest/{value}")
    public Result<String> rest(@PathVariable(value = "value") final String value) {
        return Result.ok(this.doInvoke(value));
    }

    @Async
    @GetMapping(path = "/rest-async/{value}")
    public Future<Result<String>> invokeAsync(@PathVariable(value = "value") final String value) {
        return new AsyncResult<>(Result.ok(doInvoke(value)));
    }

    @GetMapping(path = "/rest-threadpool/{value}")
    public Result<String> invokeThreadPool(@PathVariable final String value) {
        // 这里不准使用lambda表达式
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                doInvoke(value);
            }
        });
        return Result.ok("Invoke ThreadPool");
    }

    private String doInvoke(String value) {
        value = this.restTemplate.getForEntity("http://{service_name}/rest/" + value, String.class).getBody();
        log.info("调用：{}", value);
        return value;
    }
}