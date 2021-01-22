package maven.springcloud.quickstart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * Swagger2配置类
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Configuration
@EnableSwagger2
public class Swagger2Config {
    @Value("${spring.application.version}")
    private String version;

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                //扫描的包名称
                .apis(RequestHandlerSelectors.basePackage("maven.springcloud.quickstart"))
                .paths(PathSelectors.any())
                .build()
                .globalOperationParameters(setHeaderToken());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("SpringCloud微服务接口说明文档")
                .description("SpringCloud微服务接口说明文档")
                //项目作者  项目地址  作者邮箱
                .contact(new Contact("Ning.Zhang(Pegasus)", "", "zhangningkid@163.com"))
                .version(this.version)
                .build();
    }

    private List<Parameter> setHeaderToken() {
        final ParameterBuilder tokenPar = new ParameterBuilder();
        final List<Parameter> pars = new ArrayList<>();
        tokenPar.name("access_token")
                .description("授权access token")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false)
                .build();
        pars.add(tokenPar.build());
        return pars;
    }
}