package org.wyyt.test.kafka;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.wyyt.test.TestApplication;
import org.wyyt.test.service.KafkaTest;

/**
 * the test of Kafka
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Rollback(false)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class KafkaTester {
    @Autowired
    private KafkaTest kafkaTest;

    @Test
    public void test00_send() throws Exception {
        this.kafkaTest.send();
    }

    @Test
    public void test01_sendAsync() {
        this.kafkaTest.sendAsync();
    }

    @Test
    public void test02_sendTran() {
        try {
            this.kafkaTest.sendTran();
        } catch (Exception exception) {
            Assert.assertEquals("能够正常回滚", exception.getMessage());
        }
    }

    @Test
    public void test03_sendTranAsync() {
        try {
            this.kafkaTest.sendTranAsync();
        } catch (Exception exception) {
            Assert.assertEquals("能够正常回滚", exception.getMessage());
        }
    }
}