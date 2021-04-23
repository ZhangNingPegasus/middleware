package org.wyyt.springcloud.boot;

import com.ctrip.framework.apollo.spring.boot.ApolloApplicationContextInitializer;
import com.nepxion.discovery.common.constant.DiscoveryConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.ObjectUtils;
import org.wyyt.springcloud.entity.constants.Names;
import org.wyyt.tool.exception.ExceptionTool;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

/**
 * The Context initializer
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public class SpringCloudApplicationContextInitializer implements EnvironmentPostProcessor, Ordered {
    @Override
    public void postProcessEnvironment(final ConfigurableEnvironment configurableEnvironment,
                                       final SpringApplication springApplication) {
        if (null == configurableEnvironment || null == springApplication) {
            return;
        }
        final WebApplicationType webApplicationType = springApplication.getWebApplicationType();
        if (null == webApplicationType || WebApplicationType.NONE == webApplicationType) {
            return;
        }

        if (ObjectUtils.isEmpty(configurableEnvironment.getProperty("spring.application.name", ""))) {
            throw new RuntimeException("未设置服务名称, 请设置[spring.application.name]");
        }
        if (ObjectUtils.isEmpty(configurableEnvironment.getProperty("spring.application.group", ""))) {
            throw new RuntimeException("未设置服务组名, 请设置[spring.application.group]");
        }
        if (ObjectUtils.isEmpty(configurableEnvironment.getProperty("spring.application.version", ""))) {
            throw new RuntimeException("未设置服务版本, 请设置[spring.application.version]");
        }

        final Properties properties = new Properties();
        // common config
        final String agentJarVersion = System.getProperty("agent.jar.version");
        if (!ObjectUtils.isEmpty(agentJarVersion)) {
            System.setProperty(DiscoveryConstant.SPRING_APPLICATION_DISCOVERY_AGENT_VERSION, agentJarVersion);
        }
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.cloud.consul.discovery.service-name",
                "${spring.application.name}");
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.cloud.discovery.metadata.group",
                "${spring.application.group}");
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.cloud.discovery.metadata.version",
                "${spring.application.version}");
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.cloud.consul.discovery.metadata.group",
                "${spring.application.group}");
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.cloud.consul.discovery.metadata.version",
                "${spring.application.version}");
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.cloud.consul.discovery.instance-id",
                "${spring.application.name}-${server.port}-${spring.cloud.client.hostname}-${spring.cloud.client.ip-address}");
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.cloud.consul.discovery.prefer-ip-address",
                true);
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.cloud.gateway.discovery.locator.lower-case-service-id",
                true);
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.cloud.gateway.discovery.locator.enabled",
                false);
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.cloud.loadbalancer.ribbon.enabled",
                true);
        this.addDefaultConfig(configurableEnvironment, properties,
                "server.tomcat.uri-encoding",
                StandardCharsets.UTF_8.name());
        this.addDefaultConfig(configurableEnvironment, properties,
                "server.servlet.encoding.charset",
                StandardCharsets.UTF_8.name());
        this.addDefaultConfig(configurableEnvironment, properties,
                "server.servlet.encoding.enabled",
                true);
        this.addDefaultConfig(configurableEnvironment, properties,
                "server.servlet.encoding.force",
                true);

        // sleuth config
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.sleuth.sampler.probability",
                1);

        // Ribbon config
        this.addDefaultConfig(configurableEnvironment, properties,
                "ribbon.eager-load.enabled",
                true);
        this.addDefaultConfig(configurableEnvironment, properties,
                "ribbon.ServerListRefreshInterval",
                5000);
        this.addDefaultConfig(configurableEnvironment, properties,
                "ribbon.ConnectTimeout",
                15000);
        this.addDefaultConfig(configurableEnvironment, properties,
                "ribbon.ReadTimeout",
                15000);
        this.addDefaultConfig(configurableEnvironment, properties,
                "ribbon.maxAutoRetries",
                3);
        this.addDefaultConfig(configurableEnvironment, properties,
                "ribbon.maxAutoRetriesNextServer",
                3);
        this.addDefaultConfig(configurableEnvironment, properties,
                "ribbon.okToRetryOnAllOperations",
                true);

        // feign config
        this.addDefaultConfig(configurableEnvironment, properties,
                "feign.hystrix.enabled",
                true);
        this.addDefaultConfig(configurableEnvironment, properties,
                "feign.httpclient.enabled",
                true);
        this.addDefaultConfig(configurableEnvironment, properties,
                "feign.client.config.default.ConnectTimeOut",
                15000);
        this.addDefaultConfig(configurableEnvironment, properties,
                "feign.client.config.default.ReadTimeOut",
                15000);

        //hystrix config
        this.addDefaultConfig(configurableEnvironment, properties,
                "hystrix.command.default.execution.isolation.strategy",
                "SEMAPHORE");
        this.addDefaultConfig(configurableEnvironment, properties,
                "hystrix.command.default.execution.timeout.enabled",
                true);
        this.addDefaultConfig(configurableEnvironment, properties,
                "hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds",
                10000);
        this.addDefaultConfig(configurableEnvironment, properties,
                "hystrix.threadpool.default.coreSize",
                "200");
        this.addDefaultConfig(configurableEnvironment, properties,
                "hystrix.threadpool.default.maximumSize",
                "500");
        this.addDefaultConfig(configurableEnvironment, properties,
                "hystrix.threadpool.default.allowMaximumSizeToDivergeFromCoreSize",
                true);
        this.addDefaultConfig(configurableEnvironment, properties,
                "hystrix.threadpool.default.keepAliveTimeInMinutes",
                1);

        // compression config
        this.addDefaultConfig(configurableEnvironment, properties,
                "server.compression.enabled",
                true);
        this.addDefaultConfig(configurableEnvironment, properties,
                "server.compression.mime-types",
                "text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json,application/xml");
        this.addDefaultConfig(configurableEnvironment, properties,
                "feign.compression.response.enabled",
                true);
        this.addDefaultConfig(configurableEnvironment, properties,
                "feign.compression.request.enabled",
                true);
        this.addDefaultConfig(configurableEnvironment, properties,
                "feign.compression.request.mime-types",
                "${server.compression.mime-types}");
        this.addDefaultConfig(configurableEnvironment, properties,
                "feign.compression.request.min-request-size",
                2048);

        // Management config
        this.addDefaultConfig(configurableEnvironment, properties,
                "management.endpoints.web.exposure.include",
                "*");
        this.addDefaultConfig(configurableEnvironment, properties,
                "management.endpoints.jmx.exposure.include",
                "*");
        this.addDefaultConfig(configurableEnvironment, properties,
                "management.endpoint.health.show-details",
                "ALWAYS");
        this.addDefaultConfig(configurableEnvironment, properties,
                "management.metrics.tags.application",
                "${spring.application.name}");
        this.addDefaultConfig(configurableEnvironment, properties,
                "management.endpoint.logfile.external-file",
                "logs/app.log");

        // core config
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.main.allow-bean-definition-overriding",
                true);
        // 开启和关闭服务注册层面的控制。一旦关闭，服务注册的黑/白名单过滤功能将失效，最大注册数的限制过滤功能将失效。缺失则默认为true
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.register.control.enabled",
                true);
        // 开启和关闭服务发现层面的控制。一旦关闭，服务多版本调用的控制功能将失效，动态屏蔽指定IP地址的服务实例被发现的功能将失效。缺失则默认为true
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.discovery.control.enabled",
                true);
        // 开启和关闭通过Rest方式对规则配置的控制和推送。一旦关闭，只能通过远程配置中心来控制和推送。缺失则默认为true
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.config.rest.control.enabled",
                true);
        // 规则文件的格式，支持xml和json。缺失则默认为xml
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.config.format",
                "xml");
        // 本地规则文件的路径，支持两种方式：classpath:rule.xml（rule.json） - 规则文件放在resources目录下，便于打包进jar;file:rule.xml（rule.json） - 规则文件放在工程根目录下，放置在外部便于修改。缺失则默认为不装载本地规则
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.config.path",
                "classpath:rule.xml");
        // 为微服务归类的Key，一般通过group字段来归类，例如eureka.instance.metadataMap.group=xxx-group或者eureka.instance.metadataMap.application=xxx-application。缺失则默认为group
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.group.key",
                Names.GROUP);
        // 业务系统希望大多数时候Spring、SpringBoot或者SpringCloud的基本配置、调优参数（非业务系统配置参数），不配置在业务端，集成到基础框架里。但特殊情况下，业务系统有时候也希望能把基础框架里配置的参数给覆盖掉，用他们自己的配置
        // 对于此类型的配置需求，可以配置在下面的配置文件里。该文件一般放在resource目录下。缺失则默认为spring-application-default.properties
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.default.properties.path",
                "spring-application-default.properties");
        // 负载均衡下，消费端尝试获取对应提供端初始服务实例列表为空的时候，进行重试。缺失则默认为false
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.no.servers.retry.enabled",
                false);
        // 负载均衡下，消费端尝试获取对应提供端初始服务实例列表为空的时候，进行重试的次数。缺失则默认为5
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.no.servers.retry.times",
                5);
        // 负载均衡下，消费端尝试获取对应提供端初始服务实例列表为空的时候，进行重试的时间间隔。缺失则默认为2000
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.no.servers.retry.await.time",
                2000);
        // 负载均衡下，消费端尝试获取对应提供端服务实例列表为空的时候，通过日志方式通知。缺失则默认为false
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.no.servers.notify.enabled",
                false);

        // strategy config
        // 开启和关闭Ribbon默认的ZoneAvoidanceRule负载均衡策略。一旦关闭，则使用RoundRobin简单轮询负载均衡策略。缺失则默认为true
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.zone.avoidance.rule.enabled",
                true);
        // 启动和关闭路由策略的时候，对REST方式的调用拦截。缺失则默认为true
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.rest.intercept.enabled",
                true);
        // 启动和关闭路由策略的时候，对REST方式在异步调用场景下在服务端的Request请求的装饰，当主线程先于子线程执行完的时候，Request会被Destory，导致Header仍旧拿不到，开启装饰，就可以确保拿到。缺失则默认为true
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.rest.request.decorator.enabled",
                true);
        // 启动和关闭Header传递的Debug日志打印，注意：每调用一次都会打印一次，会对性能有所影响，建议压测环境和生产环境关闭。缺失则默认为false
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.rest.intercept.debug.enabled",
                false);
        // 当外界传值Header的时候，服务也设置并传递同名的Header，需要决定哪个Header传递到后边的服务去，该开关依赖前置过滤器的开关。如果下面开关为true，以服务设置为优先，否则以外界传值为优先。缺失则默认为true
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.service.header.priority",
                true);
        // 启动和关闭Feign上核心策略Header传递，缺失则默认为true。当全局订阅启动时，可以关闭核心策略Header传递，这样可以节省传递数据的大小，一定程度上可以提升性能
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.feign.core.header.transmission.enabled",
                true);
        // 启动和关闭RestTemplate上核心策略Header传递，缺失则默认为true。当全局订阅启动时，可以关闭核心策略Header传递，这样可以节省传递数据的大小，一定程度上可以提升性能
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.rest.template.core.header.transmission.enabled",
                true);
        // 路由策略的时候，对REST方式调用拦截的时候（支持Feign或者RestTemplate调用），希望把来自外部自定义的Header参数（用于框架内置上下文Header，例如：trace-id, span-id等）传递到服务里，那么配置如下值。如果多个用“;”分隔，不允许出现空格
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.context.request.headers",
                "trace-id;span-id");
        // 路由策略的时候，对REST方式调用拦截的时候（支持Feign或者RestTemplate调用），希望把来自外部自定义的Header参数（用于业务系统自定义Header，例如：mobile）传递到服务里，那么配置如下值。如果多个用“;”分隔，不允许出现空格
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.business.request.headers",
                StringUtils.join(Arrays.asList(Names.HEADER_ACCESS_TOKEN, Names.HEADER_CLIENT_ID), ";"));
        // 启动和关闭路由策略的时候，对RPC方式的调用拦截。缺失则默认为false
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.rpc.intercept.enabled",
                true);
        // 启动和关闭消费端的服务隔离（基于Group是否相同的策略）。缺失则默认为false
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.consumer.isolation.enabled",
                false);
        // 启动和关闭提供端的服务隔离（基于Group是否相同的策略）。缺失则默认为false
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.provider.isolation.enabled",
                false);
        // 启动和关闭监控，一旦关闭，调用链和日志输出都将关闭。缺失则默认为false
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.monitor.enabled",
                false);
        // 启动和关闭日志输出。缺失则默认为false
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.logger.enabled",
                false);
        // 日志输出中，是否显示MDC前面的Key。缺失则默认为true
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.logger.mdc.key.shown",
                true);
        // 启动和关闭Debug日志打印，注意：每调用一次都会打印一次，会对性能有所影响，建议压测环境和生产环境关闭。缺失则默认为false
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.logger.debug.enabled",
                false);
        // 启动和关闭调用链输出。缺失则默认为false
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.tracer.enabled",
                false);
        // 启动和关闭调用链的蓝绿灰度信息以独立的Span节点输出，如果关闭，则蓝绿灰度信息输出到原生的Span节点中（SkyWalking不支持原生模式）。缺失则默认为true
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.tracer.separate.span.enabled",
                true);
        // 启动和关闭调用链的蓝绿灰度规则策略信息输出。缺失则默认为true
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.tracer.rule.output.enabled",
                true);
        // 启动和关闭调用链的异常信息是否以详细格式输出。缺失则默认为false
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.tracer.exception.detail.output.enabled",
                false);
        // 启动和关闭类方法上入参和出参输出到调用链。缺失则默认为false
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.tracer.method.context.output.enabled",
                false);
        // 显示在调用链界面上蓝绿灰度Span的名称，建议改成具有公司特色的框架产品名称
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.tracer.span.value",
                "ZhangNing(Pegasus)");
        // 显示在调用链界面上蓝绿灰度Span Tag的插件名称，建议改成具有公司特色的框架产品的描述
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.tracer.span.tag.plugin.value",
                "ZhangNing(Pegasus)");
        // 启动和关闭Sentinel调用链上规则在Span上的输出，注意：原生的Sentinel不是Spring技术栈，下面参数必须通过-D方式或者System.setProperty方式等设置进去。缺失则默认为true
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.tracer.sentinel.rule.output.enabled",
                false);
        // 启动和关闭Sentinel调用链上方法入参在Span上的输出，注意：原生的Sentinel不是Spring技术栈，下面参数必须通过-D方式或者System.setProperty方式等设置进去。缺失则默认为false
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.tracer.sentinel.args.output.enabled",
                false);
        // 开启Spring Cloud Gateway网关上实现Hystrix线程隔离模式做服务隔离时，必须把spring.application.strategy.hystrix.threadlocal.supported设置为true，同时要引入discovery-plugin-strategy-starter-hystrix包，否则线程切换时会发生ThreadLocal上下文对象丢失。缺失则默认为false
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.hystrix.threadlocal.supported",
                false);
        // 启动和关闭Sentinel限流降级熔断权限等原生功能的数据来源扩展。缺失则默认为false
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.sentinel.enabled",
                false);
        // 流控规则文件路径。缺失则默认为classpath:sentinel-flow.json
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.sentinel.flow.path",
                "classpath:sentinel-flow.json");
        // 降级规则文件路径。缺失则默认为classpath:sentinel-degrade.json
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.sentinel.degrade.path",
                "classpath:sentinel-degrade.json");
        // 授权规则文件路径。缺失则默认为classpath:sentinel-authority.json
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.sentinel.authority.path",
                "classpath:sentinel-authority.json");
        // 系统规则文件路径。缺失则默认为classpath:sentinel-system.json
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.sentinel.system.path",
                "classpath:sentinel-system.json");
        // 热点参数流控规则文件路径。缺失则默认为classpath:sentinel-param-flow.json
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.sentinel.param.flow.path",
                "classpath:sentinel-param-flow.json");
        // 服务端执行规则时候，以Http请求中的Header值作为关键Key。缺失则默认为n-d-service-id，即以服务名作为关键Key
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.service.sentinel.request.origin.key",
                "n-d-service-id");
        // 启动和关闭Sentinel LimitApp限流等功能。缺失则默认为false
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.service.sentinel.limit.app.enabled",
                false);
        // 流量路由到指定的环境下。不允许为保留值default，缺失则默认为common
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.environment.route",
                "common");
        // 启动和关闭可用区亲和性，即同一个可用区的服务才能调用，同一个可用区的条件是调用端实例和提供端实例的元数据Metadata的zone配置值必须相等。缺失则默认为false
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.zone.affinity.enabled",
                false);
        // 启动和关闭可用区亲和性失败后的路由，即调用端实例没有找到同一个可用区的提供端实例的时候，当开关打开，可路由到其它可用区或者不归属任何可用区，当开关关闭，则直接调用失败。缺失则默认为true
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.zone.route.enabled",
                true);
        // 版本故障转移，即无法找到相应版本的服务实例，路由到老的稳定版本的实例。其作用是防止蓝绿灰度版本发布人为设置错误，或者对应的版本实例发生灾难性的全部下线，导致流量有损
        // 启动和关闭版本故障转移。缺失则默认为false
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.version.failover.enabled",
                false);
        // 版本偏好，即非蓝绿灰度发布场景下，路由到老的稳定版本的实例。其作用是防止多个网关上并行实施蓝绿灰度版本发布产生混乱，对处于非蓝绿灰度状态的服务，调用它的时候，只取它的老的稳定版本的实例；蓝绿灰度状态的服务，还是根据传递的Header版本号进行匹配
        // 启动和关闭版本偏好。缺失则默认为false
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.strategy.version.prefer.enabled",
                false);
        // 启动和关闭在服务启动的时候参数订阅事件发送。缺失则默认为true
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.parameter.event.onstart.enabled",
                true);
        // 开启和关闭使用服务名前缀来作为服务组名。缺失则默认为false
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.group.generator.enabled",
                false);
        // 服务名前缀的截断长度，必须大于0
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.group.generator.length",
                15);
        // 服务名前缀的截断标志。当截断长度配置了，则取截断长度方式，否则取截断标志方式
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.group.generator.character",
                "-");
        // 开启和关闭使用Git信息中的字段单个或者多个组合来作为服务版本号。缺失则默认为false
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.git.generator.enabled",
                false);
        // 插件git-commit-id-plugin产生git信息文件的输出路径，支持properties和json两种格式，支持classpath:xxx和file:xxx两种路径，这些需要和插件里的配置保持一致。缺失则默认为classpath:git.properties
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.git.generator.path",
                "classpath:git.properties");
        // 使用Git信息中的字段单个或者多个组合来作为服务版本号。缺失则默认为{git.commit.time}-{git.total.commit.count}
        this.addDefaultConfig(configurableEnvironment, properties,
                "spring.application.git.version.key",
                "{git.commit.id.abbrev}-{git.commit.time}");

        if (!properties.isEmpty()) {
            configurableEnvironment.getPropertySources().addFirst(new PropertiesPropertySource("springCloudApplicationProperties", properties));
        }
    }

    @Override
    public int getOrder() {
        return ApolloApplicationContextInitializer.DEFAULT_ORDER + 1;
    }

    private void addDefaultConfig(final ConfigurableEnvironment configurableEnvironment,
                                  final Properties properties,
                                  final String name,
                                  final Object value) {
        try {
            final String oldProperty = configurableEnvironment.getProperty(name);
            if (ObjectUtils.isEmpty(oldProperty)) {
                properties.put(name, value);
            }
        } catch (final IllegalArgumentException exception) {
            log.error(ExceptionTool.getRootCauseMessage(exception), exception);
        }
    }
}
