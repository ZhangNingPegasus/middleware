package org.wyyt.sharding.db2es.core.util.flatmsg;

import com.alibaba.fastjson.JSON;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.wyyt.sharding.db2es.core.entity.domain.FlatMsg;
import org.wyyt.sharding.db2es.core.entity.domain.OperationType;
import org.wyyt.sharding.db2es.core.exception.Db2EsException;
import org.wyyt.tool.exception.ExceptionTool;

import java.util.List;

/**
 * the common functions of FlatMsg
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public final class FlatMsgUtils {
    public static <T extends FlatMsg> T toFlatMsg(final ConsumerRecord<String, String> consumerRecord,
                                                  final Class<T> tClass) throws Exception {
        T result;
        try {
            result = JSON.parseObject(consumerRecord.value(), tClass);
            result.setConsumerRecord(consumerRecord);
            result.setOperationType(OperationType.get(result.getType()));
        } catch (Exception exception) {
            result = tClass.newInstance();
            result.setConsumerRecord(consumerRecord);
            result.setOperationType(OperationType.EXCEPTION);
            result.setEs(consumerRecord.timestamp());
            result.setException(exception);
        }
        return result;
    }

    public static <T extends FlatMsg> void operate(final List<T> flatMsgList,
                                                   final Operation<T> operation) throws Exception {
        for (final T message : flatMsgList) {
            try {
                switch (message.getOperationType()) {
                    case INSERT:
                        operation.insert(message);
                        break;
                    case DELETE:
                        operation.delete(message);
                        break;
                    case UPDATE:
                        operation.update(message);
                        break;
                    case EXCEPTION:
                        throw new Db2EsException(ExceptionTool.getRootCauseMessage(message.getException()));
                    case TRUNCATE:  //ignore
                    case ALTER:     //ignore
                    case ERASE:     //ignore
                    case DINDEX:    //ignore
                    case CREATE:    //ignore
                        break;
                    default:
                        throw new Db2EsException(String.format("[%s-%s] - 操作类型未知[%s]",
                                message.getConsumerRecord().topic(),
                                message.getConsumerRecord().partition(),
                                message.getType()));
                }
            } catch (final Exception e) {
                operation.exception(message, e);
            }
        }
    }
}