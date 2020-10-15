package org.wyyt.test.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.wyyt.kafka.service.KafkaService;
import org.wyyt.kafka.tran.anno.TranKafka;
import org.wyyt.tool.date.DateTool;

import java.util.Date;

/**
 * kafka test
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
@Service
public class KafkaTest {
    private static final String TOPIC_NAME = "A";
    private final KafkaService kafkaService;

    public KafkaTest(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    public void sendAsync() {
        this.kafkaService.sendAsync(TOPIC_NAME,
                String.valueOf(System.currentTimeMillis()),
                "异步发送" + DateTool.format(new Date()),
                (sendResult, throwable) -> {
                    if (null != throwable) {
                        log.error(throwable.getMessage(), throwable);
                    }
                    System.out.println(sendResult);

                });
    }


    public void send() throws Exception {
        this.kafkaService.send(TOPIC_NAME, String.valueOf(System.currentTimeMillis()), String.valueOf(System.currentTimeMillis()));
    }

    @TranKafka
    public void sendTran() throws Exception {
        this.kafkaService.send(TOPIC_NAME, String.valueOf(System.currentTimeMillis()), String.valueOf(System.currentTimeMillis()));
        Assert.isTrue(false, "能够正常回滚");
    }

    @TranKafka
    public void sendTranAsync() {
        this.kafkaService.sendAsync(TOPIC_NAME, String.valueOf(System.currentTimeMillis()), String.valueOf(System.currentTimeMillis()), (sendResult, throwable) -> {
            log.info(sendResult.toString());
            Assert.isTrue(false, "回调方法中的异常是不会回滚的");
        });
        Assert.isTrue(false, "能够正常回滚");
    }
}