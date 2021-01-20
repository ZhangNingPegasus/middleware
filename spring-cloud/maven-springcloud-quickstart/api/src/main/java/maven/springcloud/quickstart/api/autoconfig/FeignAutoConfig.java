package maven.springcloud.quickstart.api.autoconfig;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static maven.springcloud.quickstart.api.autoconfig.FeignAutoConfig.BASE_PACKAGE;


/**
 * Feign客户端的自动装配, 让调用方无需任何配置, 直接注入即可
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Configuration
@EnableFeignClients(basePackages = {BASE_PACKAGE})
@ComponentScan(value = {BASE_PACKAGE})
public class FeignAutoConfig {
    public static final String BASE_PACKAGE = "maven.springcloud.quickstart.api";
}