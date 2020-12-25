package org.wyyt.kafka.service;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.wyyt.kafka.tran.TranProducerContext;

import java.util.concurrent.Future;

/**
 * the service of Kafka, providing the common methods of Kafka
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
public final class KafkaService implements DisposableBean {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaService(final KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public final Future sendAsync(final String topic,
                                  final Integer partition,
                                  final String key,
                                  final String data,
                                  final Callback callback) {
        final Producer<String, String> tranProducer = TranProducerContext.get();
        if (null == tranProducer) {
            final ListenableFuture<SendResult<String, String>> result = this.kafkaTemplate.send(topic, partition, key, data);
            result.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onFailure(final Throwable throwable) {
                    callback.action(null, throwable);
                }

                @Override
                public void onSuccess(final SendResult<String, String> sendResult) {
                    callback.action(sendResult, null);
                }
            });
            return result;
        } else {
            final ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, partition, key, data);
            return tranProducer.send(producerRecord, (recordMetadata, e) -> callback.action(new SendResult<>(producerRecord, recordMetadata), e));
        }
    }

    public final Future sendAsync(final String topic,
                                  final String key,
                                  final String data,
                                  final Callback callback) {
        return sendAsync(topic, null, key, data, callback);
    }

    public final void send(final String topic,
                           final Integer partition,
                           final String key,
                           final String data) throws Exception {
        final Exception[] error = new Exception[]{null};
        this.sendAsync(topic, partition, key, data, (sendResult, throwable) -> {
            if (null != throwable) {
                error[0] = new Exception(throwable);
            }
        }).get();
        if (null != error[0]) {
            throw error[0];
        }
    }

    public final void send(final String topic,
                           final String key,
                           final String data) throws Exception {
        this.send(topic, null, key, data);
    }

    @Override
    public final void destroy() {
        if (null != this.kafkaTemplate) {
            this.kafkaTemplate.destroy();
        }
    }

    public interface Callback {
        void action(final SendResult<String, String> sendResult, final Throwable throwable);
    }
}