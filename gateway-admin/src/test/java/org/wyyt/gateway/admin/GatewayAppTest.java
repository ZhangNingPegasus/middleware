package org.wyyt.gateway.admin;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.wyyt.gateway.admin.service.GatewayService;
import org.wyyt.gateway.admin.service.GrayPublishService;

/**
 * the test of Elastic-Search
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = GatewayAdminApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Rollback(false)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GatewayAppTest {
    @Autowired
    private GatewayService gatewayService;
    @Autowired
    private GrayPublishService grayPublishService;


    @Test
    public void test00() throws Exception {
        String inspect = "{\"serviceIdList\":[],\"result\":\"[ID=spring-cloud-gateway][P=Consul][H=192.168.12.155:80][V=1.0][R=default][E=default][Z=default][G=wyyt-spring-cloud-gateway] -> [ID=a][P=Consul][H=192.168.12.155:1001][V=1.1][R=default][E=default][Z=default][G=scfs] -> [ID=b][P=Consul][H=192.168.12.155:2001][V=1.1][R=default][E=default][Z=default][G=scfs] -> [ID=c][P=Consul][H=192.168.12.155:3001][V=1.1][R=default][E=default][Z=default][G=scfs] -> [ID=d][P=Consul][H=192.168.12.155:4001][V=1.1][R=default][E=default][Z=default][G=scfs]\"}";
        System.out.println(GrayPublishService.formatInspect(inspect));
    }
}