package org.wyyt.sharding.db2es.client.http;

import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.DefaultNHttpServerConnection;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.nio.entity.ContentBufferEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.kafka.common.TopicPartition;
import org.reflections.ReflectionUtils;
import org.springframework.util.ObjectUtils;
import org.wyyt.sharding.db2es.core.entity.domain.Names;
import org.wyyt.sharding.db2es.core.entity.domain.TopicOffset;
import org.wyyt.sharding.db2es.core.exception.Db2EsException;
import org.wyyt.sharding.db2es.client.common.Context;
import org.wyyt.tool.date.DateTool;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.rpc.Result;
import org.wyyt.tool.rpc.SignTool;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * the base handler
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
public abstract class BaseHandler implements HttpAsyncRequestHandler<HttpRequest> {
    protected Context context;

    protected void checkTimestamp(final TopicPartition topicPartition,
                                  final Long timestamp) throws Exception {
        final TopicOffset topicOffset = this.context.getKafkaAdminClientWrapper().listOffsetForTimes(topicPartition, timestamp);
        if (null == topicOffset.getLeaderEpoch()) {
            throw new Db2EsException(String.format("主题[%s]无法根据时间戳[%s(%s)]定位到消费位点",
                    topicPartition.topic(),
                    timestamp,
                    DateTool.format(new Date(timestamp))));
        }
    }

    @Override
    public final HttpAsyncRequestConsumer<HttpRequest> processRequest(final HttpRequest httpRequest,
                                                                      final HttpContext httpContext) {
        return new BasicAsyncRequestConsumer();
    }

    @SneakyThrows
    @Override
    public final void handle(final HttpRequest httpRequest,
                             final HttpAsyncExchange httpAsyncExchange,
                             final HttpContext httpContext) {
        final HttpResponse response = httpAsyncExchange.getResponse();
        final Set<Field> fields = ReflectionUtils.getFields(httpAsyncExchange.getClass(), field -> field.getType().isAssignableFrom(DefaultNHttpServerConnection.class));
        if (!fields.isEmpty()) {
            this.handleInternal(httpRequest, response);
        } else {
            response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setEntity(stringEntity(Result.error("Internal Server Error")));
        }
        httpAsyncExchange.submitResponse(new BasicAsyncResponseProducer(response));
    }

    private void handleInternal(final HttpRequest request,
                                final HttpResponse response) throws Exception {
        final String requestMethod = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH).trim();
        final String uri = URLDecoder.decode(request.getRequestLine().getUri(), StandardCharsets.UTF_8.name()).trim();
        String path = uri;
        if (uri.contains("?")) {
            path = uri.substring(0, uri.indexOf("?"));
        }
        final Map<String, Object> getMap = formData2Map(getQueryString(uri));
        if ("POST".equalsIgnoreCase(requestMethod)) {
            final Map<String, Object> postMap = formData2Map(getPostString(request));
            if (!checkSign(request, postMap)) {
                response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                response.setEntity(stringEntity(Result.error("No Authenticated")));
            } else {
                final Method method = this.context.getCacheWrapper().get(path);
                if (null != method) {
                    try {
                        final Object invoke = method.invoke(this, new Param(getMap, postMap));
                        final Result<?> result = (Result<?>) invoke;
                        response.setStatusCode(HttpStatus.SC_OK);
                        response.setEntity(stringEntity(JSON.toJSONString(result)));
                    } catch (final Exception e) {
                        response.setStatusCode(HttpStatus.SC_OK);
                        assert e instanceof InvocationTargetException;
                        response.setEntity(stringEntity(JSON.toJSONString(Result.error(ExceptionTool.getRootCauseMessage(((InvocationTargetException) e).getTargetException())))));
                    }
                } else {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                    response.setEntity(stringEntity(Result.error(String.format("service for path[%s] is not implemented", requestMethod))));
                }
            }
        } else {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            response.setEntity(stringEntity(Result.error(String.format("method [%s] is not supported.", requestMethod))));
        }
    }

    private static NStringEntity stringEntity(final Object object) {
        String message;
        if (object instanceof String) {
            message = object.toString();
        } else {
            message = JSON.toJSONString(object);
        }
        return new NStringEntity(message, ContentType.create("application/json", StandardCharsets.UTF_8));
    }

    private static String getQueryString(final String uri) {
        if (uri.contains("?")) {
            return uri.substring(uri.indexOf("?") + 1).trim();
        }
        return null;
    }

    private static String getPostString(final HttpRequest request) throws IOException {
        return IOUtils.toString(((ContentBufferEntity) ((BasicHttpEntityEnclosingRequest) request).getEntity()).getContent(), StandardCharsets.UTF_8);
    }

    private static Map<String, Object> formData2Map(final String formData) throws UnsupportedEncodingException {
        final Map<String, Object> result = new HashMap<>();
        if (ObjectUtils.isEmpty(formData)) {
            return result;
        }

        final String[] items = formData.split("&");
        for (final String item : items) {
            if (ObjectUtils.isEmpty(item)) {
                continue;
            }

            final String[] keyAndVal = item.split("=");
            if (2 == keyAndVal.length) {
                final String key = URLDecoder.decode(keyAndVal[0], StandardCharsets.UTF_8.name());
                final String val = URLDecoder.decode(keyAndVal[1], StandardCharsets.UTF_8.name());
                result.put(key, val);
            }
        }
        return result;
    }

    private static boolean checkSign(final HttpRequest request,
                                     final Map<String, Object> params) throws Exception {
        final Header header = request.getFirstHeader("sign");
        if (null == header) {
            return false;
        }
        return SignTool.checkSign(header.getValue(), params, Names.API_KEY, Names.API_IV);
    }
}
