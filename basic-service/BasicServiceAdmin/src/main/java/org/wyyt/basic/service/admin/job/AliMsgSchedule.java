package org.wyyt.basic.service.admin.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wyyt.basic.service.admin.config.PropertyConfig;
import org.wyyt.tool.rpc.RpcService;

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
public class AliMsgSchedule {
    private final PropertyConfig propertyConfig;

    public AliMsgSchedule(final PropertyConfig propertyConfig) {
        this.propertyConfig = propertyConfig;
    }

    //每5秒执行一次
    @Scheduled(cron = "0/5 * * * * ?")
    public void processPendingMsgAli() throws Exception {

//        this.rpcService.post();

//        final Result<?> result = this.rpcService.post(
//                this.propertyConfig.getSmsConsulName(),
//                Constant.PROCESS_ENDING_MSG,
//                new TypeReference<Result<?>>() {
//                });
    }
}