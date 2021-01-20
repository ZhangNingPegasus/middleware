package maven.springcloud.quickstart.feign.example;

import lombok.extern.slf4j.Slf4j;
import maven.springcloud.quickstart.api.AFeign;
import maven.springcloud.quickstart.entity.User;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.wyyt.tool.rpc.Result;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * 服务实现(Feign方式), 服务提供方代码, 仅做参考, 可删除
 * 注意:
 * 使用多线程时, 必须先注入Executor线程池类, 使用该线程池的execute方法进行多线程的调用, 并且在创建过程中
 * 禁止使用匿名类、lambda表达式
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
@RestController
public class AFeignImpl implements AFeign {
    private final Executor executor;

    @Value("${user.name.test}")
    private String userName;

    public AFeignImpl(final Executor executor) {
        this.executor = executor;
    }

    @Override
    public Result<User> invoke(@PathVariable(value = "value") String value) {
        final User user = new User();
        user.setUserName("userName");
        user.setAge(RandomUtils.nextInt(18, 35));
        user.setMale(RandomUtils.nextBoolean());
        user.setDescription(doInvoke(value));
        return Result.ok(user);
    }

    @Async
    @Override
    public Future<Result<String>> invokeAsync(@PathVariable(value = "value") String value) {
        return new AsyncResult<>(Result.ok(doInvoke(value)));
    }

    @Override
    public Result<String> invokeThread(@PathVariable(value = "value") final String value) {
        // 这里不准使用lambda表达式
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                doInvoke(value);
            }
        });
        return Result.ok("Invoke Thread");
    }

    @Override
    public Result<String> uploadFile(final MultipartFile file) throws IOException {
        return Result.ok(String.valueOf(file.getBytes().length));
    }

    private String doInvoke(final String value) {
        final String result = value.concat(" -> 服务A");
        log.info("调用：{}", value);
        return result;
    }
}