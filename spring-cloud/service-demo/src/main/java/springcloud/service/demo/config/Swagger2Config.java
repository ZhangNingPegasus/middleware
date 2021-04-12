package springcloud.service.demo.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wyyt.springcloud.entity.constants.Names;
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
                .apis(RequestHandlerSelectors.basePackage("springcloud.service.demo"))
                .paths(PathSelectors.any())
                .build()
                .globalOperationParameters(setHeaderToken());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("SpringCloud示例")
                .description("SpringCloud示例")
                .termsOfServiceUrl("https://www.sijibao.com/")
                .contact(new Contact("Ning.Zhang(Pegasus)", "", "zhangning@sijibao.com"))
                .version(this.version)
                .build();
    }

    private List<Parameter> setHeaderToken() {
        final ParameterBuilder tokenPar = new ParameterBuilder();
        final List<Parameter> pars = new ArrayList<>();
        tokenPar.name(Names.HEADER_ACCESS_TOKEN)
                .description("授权码")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false)
                .build();
        pars.add(tokenPar.build());
        return pars;
    }
}