package org.wyyt.springcloud.auth.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * the configuration of bean
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020       Initialize   *
 * *****************************************************************
 */
@Data
@Configuration
public class PropertyConfig {
    @Value("${server.port}")
    public int port;
}
