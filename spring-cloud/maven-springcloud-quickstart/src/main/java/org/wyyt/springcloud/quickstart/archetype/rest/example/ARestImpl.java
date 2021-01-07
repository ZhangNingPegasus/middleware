package org.wyyt.springcloud.quickstart.archetype.rest.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.wyyt.tool.rpc.Result;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
public class ARestImpl {
    private final RestTemplate restTemplate;

    public ARestImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping(path = "/rest/{value}")
    public Result<String> rest(@PathVariable(value = "value") String value, HttpServletRequest request) {
        //service_name是注册在consul上服务的名称,即: 你想调用的服务名称
        value = this.restTemplate.getForEntity("http://{service_name}/rest/" + value, String.class).getBody();
        log.info("调用：{}", value);
        return Result.ok(value);
    }
}