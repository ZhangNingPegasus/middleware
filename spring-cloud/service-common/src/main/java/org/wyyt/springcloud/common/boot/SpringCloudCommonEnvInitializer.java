package org.wyyt.springcloud.common.boot;

import com.nepxion.banner.BannerConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * The environment initializer
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public class SpringCloudCommonEnvInitializer implements EnvironmentPostProcessor, Ordered {
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

        System.setProperty(BannerConstant.BANNER_SHOWN, "false");
    }

    @Override
    public int getOrder() {
        return 10;
    }
}