package org.wyyt.springcloud.springbootadmin.notifier;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.notify.AbstractEventNotifier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.wyyt.springcloud.gateway.entity.entity.Api;
import org.wyyt.springcloud.springbootadmin.config.PropertyConfig;
import org.wyyt.springcloud.springbootadmin.service.ApiLoadService;
import org.wyyt.tool.dingtalk.Message;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * the common functions of Bean
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
@Component
public class DingTalkNotifier extends AbstractEventNotifier {

    private final Long startDate;
    private final DateTimeFormatter dateFormat;
    @Getter
    private final LinkedBlockingQueue<Message> toProcessRecords;
    public static final Integer MAX_SIZE = 1024;
    private final ApiLoadService apiLoadService;
    private final PropertyConfig propertyConfig;

    public DingTalkNotifier(final InstanceRepository repository,
                            final ApiLoadService apiLoadService,
                            final PropertyConfig propertyConfig) {
        super(repository);
        this.apiLoadService = apiLoadService;
        this.propertyConfig = propertyConfig;
        this.dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
        this.toProcessRecords = new LinkedBlockingQueue<>(MAX_SIZE);
        this.startDate = System.currentTimeMillis();
    }

    @Override
    protected Mono<Void> doNotify(final InstanceEvent event, final Instance instance) {
        if (Math.abs(System.currentTimeMillis() - this.startDate) < 30 * 1000) {
            return Mono.fromRunnable(() -> {
            });
        }
        return Mono.fromRunnable(() -> {
            String content = null;
            String urlAndPort = getUrlAndPort(instance.getRegistration().getServiceUrl());
            String checkTime = null;
            Boolean isOk = null;
            final String serviceName = instance.getRegistration().getName();
            String instanceName = null;
            Api.Result apiResult = null;
            if (event instanceof InstanceStatusChangedEvent) {
                final InstanceStatusChangedEvent instanceStatusChangedEvent = (InstanceStatusChangedEvent) event;
                final String status = instanceStatusChangedEvent.getStatusInfo().getStatus();
                switch (status) {
                    // 健康检查没通过
                    case "DOWN":
                        instanceName = instanceStatusChangedEvent.getInstance().getValue();
                        checkTime = this.dateFormat.format(instanceStatusChangedEvent.getTimestamp());
                        content = "健康检查没有通过, 请立刻检查.";
                        isOk = false;
                        break;
                    // 服务离线
                    case "OFFLINE":
                        instanceName = instanceStatusChangedEvent.getInstance().getValue();
                        checkTime = this.dateFormat.format(instanceStatusChangedEvent.getTimestamp());
                        content = "已离线, 请立刻检查.";
                        isOk = false;
                        break;
                    //服务上线
                    case "UP":
                        instanceName = instanceStatusChangedEvent.getInstance().getValue();
                        checkTime = this.dateFormat.format(instanceStatusChangedEvent.getTimestamp());
                        content = "已成功上线.";
                        isOk = true;
                        if (!serviceName.equals(this.propertyConfig.getGatewayConsulName()) &&
                                !serviceName.equals(this.propertyConfig.getGatewayAdminConsulName()) &&
                                !serviceName.equals(this.propertyConfig.getAuthConsulName())) {
                            apiResult = this.apiLoadService.updateApi(instance.getRegistration().getName(),
                                    instance.getRegistration().getServiceUrl());
                        }
                        break;
                    // 服务未知异常
                    case "UNKNOWN":
                        instanceName = instanceStatusChangedEvent.getInstance().getValue();
                        checkTime = this.dateFormat.format(instanceStatusChangedEvent.getTimestamp());
                        content = "发现未知异常, 请立刻检查.";
                        isOk = false;
                        break;
                    default:
                        break;
                }
            } else if (event instanceof InstanceDeregisteredEvent) {
                final InstanceDeregisteredEvent instanceDeregisteredEvent = (InstanceDeregisteredEvent) event;
                if ("DEREGISTERED".equals(instanceDeregisteredEvent.getType())) {
                    instanceName = instanceDeregisteredEvent.getInstance().getValue();
                    checkTime = this.dateFormat.format(instanceDeregisteredEvent.getTimestamp());
                    content = "已成功下线.";
                    isOk = true;
                }
            }
            if (!ObjectUtils.isEmpty(content)) {
                final Message message = new Message();
                message.setMsgtype("text");

                final StringBuilder stringBuilder = new StringBuilder();
                if (!ObjectUtils.isEmpty(urlAndPort)) {
                    stringBuilder.append(String.format("主机地址：%s\n", urlAndPort));
                }

                if (!ObjectUtils.isEmpty(serviceName)) {
                    stringBuilder.append(String.format("服务名称：%s\n", serviceName));
                }

                if (!ObjectUtils.isEmpty(instanceName)) {
                    stringBuilder.append(String.format("实例名称：%s\n", instanceName));
                }

                if (isOk) {
                    stringBuilder.append("当前状态：OK\n");
                    stringBuilder.append(String.format("消息详情：%s\n", content));
                } else {
                    stringBuilder.append("告警等级：严重\n");
                    stringBuilder.append(String.format("问题详情：%s\n", content));
                }

                if (null != apiResult) {
                    stringBuilder.append(String.format("新增接口：%s个\n", apiResult.getInsertNum()));
                    stringBuilder.append(String.format("更新接口：%s个\n", apiResult.getUpdateNum()));
                }

                if (!ObjectUtils.isEmpty(checkTime)) {
                    stringBuilder.append(String.format("检查时间：%s", checkTime));
                }

                message.setText(new Message.Text(stringBuilder.toString()));

                int offerTryCount = 0;
                try {
                    while (!this.toProcessRecords.offer(message, 1000, TimeUnit.MILLISECONDS)) {
                        if (++offerTryCount % 30 == 0) {
                            log.warn(String.format("DingTalkNotifier: offer message has failed for a period 30 seconds, [%s]", message));
                            break;
                        }
                    }
                } catch (final Exception exception) {
                    log.error(exception.getMessage(), exception);
                }
            }
        });
    }

    private String getUrlAndPort(final String strUrl) {
        try {
            final URL url = new URL(strUrl);
            return String.format("%s : %s", url.getHost(), url.getPort());
        } catch (MalformedURLException e) {
            return "";
        }
    }
}