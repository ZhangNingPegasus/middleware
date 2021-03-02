package org.wyyt.springcloud.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.nepxion.discovery.plugin.strategy.context.StrategyContextHolder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ObjectUtils;
import org.wyyt.springcloud.common.config.SpringCloudConfig;
import org.wyyt.springcloud.common.service.GatewayService;
import org.wyyt.springcloud.entity.AppVo;
import org.wyyt.springcloud.entity.constants.Names;
import org.wyyt.tool.rpc.Result;

import java.util.HashMap;
import java.util.Map;

/**
 * The service of information
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public class EnvironmentService {
    @Autowired
    private StrategyContextHolder strategyContextHolder;
    @Autowired
    private GatewayService gatewayService;
    @Autowired
    private SpringCloudConfig springCloudConfig;

    @Getter
    @Value("${server.port}")
    private Integer port;
    @Getter
    @Value("${spring.cloud.client.hostname}")
    private String hostname;
    @Getter
    @Value("${spring.cloud.client.ip-address}")
    private String ipAddress;
    @Getter
    @Value("${gateway_url}")
    private String gatewayUrl;

    public String getClientId() {
        return this.strategyContextHolder.getHeader(Names.CLIENT_ID);
    }

    public AppVo getClient() throws Exception {
        final String accessToken = this.strategyContextHolder.getHeader(Names.ACCESS_TOKEN);
        if (ObjectUtils.isEmpty(accessToken)) {
            return null;
        }

        final Map<String, String> headerMap = new HashMap<>();
        headerMap.put(Names.ACCESS_TOKEN, accessToken);

        final Result<String> result = this.gatewayService.post(
                this.springCloudConfig.getAuthConsulName(),
                "v1/oauth/info",
                headerMap,
                null,
                new TypeReference<Result<String>>() {
                });

        if (null != result && null != result.getOk() && result.getOk()) {
            return JSON.parseObject(result.getData(), AppVo.class);
        }
        return null;
    }
}
