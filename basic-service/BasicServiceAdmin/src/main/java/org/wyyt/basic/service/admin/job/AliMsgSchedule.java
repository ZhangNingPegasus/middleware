package org.wyyt.basic.service.admin.job;

import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wyyt.basic.service.admin.config.PropertyConfig;
import org.wyyt.basic.service.admin.constants.Constant;
import org.wyyt.tool.rpc.Result;
import org.wyyt.tool.rpc.RpcService;

import java.net.URI;

/**
 * The schedule job
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
@Component
public class AliMsgSchedule implements InitializingBean {
    private final PropertyConfig propertyConfig;
    private final RpcService rpcService;
    private String processSendingMsg;

    public AliMsgSchedule(final PropertyConfig propertyConfig,
                          final RpcService rpcService) {
        this.propertyConfig = propertyConfig;
        this.rpcService = rpcService;
    }

    @Override
    public void afterPropertiesSet() {
        this.processSendingMsg = URI.create(String.format("%s/%s/%s", this.propertyConfig.getGatewayUrl(),
                this.propertyConfig.getSmsConsulName(),
                Constant.PROCESS_SENDING_MSG)).toString();
    }

    //每5秒执行一次
    @Scheduled(cron = "0/5 * * * * ?")
    public void processPendingMsgAli() {
        try {
            final Result<?> result = this.rpcService.postForEntity(this.processSendingMsg, new TypeReference<Result<?>>() {
            });
            if (!result.getOk()) {
                if (result.getError().startsWith("Unable to find instance for")) {
                    log.warn(result.getError());
                    Thread.sleep(30000);
                } else {
                    log.error(result.getError());
                }
            }
        } catch (final Exception ignored) {
        }
    }
}