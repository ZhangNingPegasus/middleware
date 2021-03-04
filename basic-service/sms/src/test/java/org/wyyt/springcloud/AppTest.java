package org.wyyt.springcloud;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springcloud.sms.SmsApplication;
import org.springcloud.sms.core.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.wyyt.sms.request.SmsRequest;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SmsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Rollback(false)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppTest {
    @Autowired
    private SmsService smsService;

    @Test
    public void shouldAnswerWithTrue() {
        SmsRequest smsRequest = new SmsRequest();
        smsRequest.setPhoneNumbers("18207131101");
        smsRequest.setSignCode("煤链社");
        smsRequest.setTemplateCode("SMS_206740237");
        smsRequest.setTemplateParam("{\"temple\":\"78901\"}");
        smsRequest.setExtra("I am the extra information");
        System.out.println(this.smsService.send(smsRequest));
    }
}