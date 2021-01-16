package org.wyyt.kafka.tran;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.kafka.core.KafkaTemplate;
import org.wyyt.kafka.tran.anno.TranKafka;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AOP used for cache
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Aspect
public class KafkaTranAop {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AtomicLong tranIndex;

    public KafkaTranAop(final KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.tranIndex = new AtomicLong(0);
    }

    @Around(value = "@annotation(tranKafka)")
    public Object aroundCacheMethod(final ProceedingJoinPoint point,
                                    final TranKafka tranKafka) throws Throwable {
        Producer<String, String> producer = null;
        try {
            final Map<String, Object> properties = new HashMap<>(this.kafkaTemplate.getProducerFactory().getConfigurationProperties());
            properties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "middleware_kafka_transactional_".concat(String.valueOf(this.tranIndex.getAndIncrement())));
            producer = new KafkaProducer<>(properties);
            producer.initTransactions();
            producer.beginTransaction();
            TranProducerContext.clear();
            TranProducerContext.set(producer);
            final Object result = point.proceed();
            producer.commitTransaction();
            return result;
        } catch (final Exception exception) {
            if (null != producer) {
                producer.abortTransaction();
            }
            throw exception;
        } finally {
            TranProducerContext.clear();
            if (null != producer) {
                producer.close();
            }
        }
    }
}