package org.wyyt.springcloud.gateway;

import com.nepxion.banner.BannerConstant;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.wyyt.springcloud.common.service.ConsulService;

/**
 * The unit test
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        02/14/2021       Initialize  *
 * *****************************************************************
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = GatewayAdminApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Rollback(false)
public class UnitTest {

    @Autowired
    private ConsulService consulService;

    @Test
    public void test00() {
        System.out.println(this.consulService.listServiceNames());
    }
}