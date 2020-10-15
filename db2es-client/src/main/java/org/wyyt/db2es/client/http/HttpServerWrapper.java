package org.wyyt.db2es.client.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.nio.bootstrap.HttpServer;
import org.apache.http.impl.nio.bootstrap.ServerBootstrap;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.reflections.ReflectionUtils;
import org.wyyt.db2es.client.common.Context;
import org.wyyt.db2es.client.common.Utils;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * the http server
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
public final class HttpServerWrapper implements Closeable {
    private final HttpServer httpServer;
    private final Context context;

    public HttpServerWrapper(final Context context) throws Exception {
        this.context = context;
        final int port = context.getConfig().getDb2EsPort();
        final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setConnectTimeout(1000 * 30)
                .setSoTimeout(1000 * 60 * 2)
                .setRcvBufSize(8192)
                .setSndBufSize(8192)
                .setTcpNoDelay(true)
                .setSoKeepAlive(true)
                .build();
        final ServerBootstrap serverBootstrap = ServerBootstrap.bootstrap()
                .setListenerPort(port)
                .setServerInfo("db2es/1.0")
                .setIOReactorConfig(ioReactorConfig);

        final Map<String, Class<? extends BaseHandler>> restControllers = Utils.getRestControllers();
        for (final Map.Entry<String, Class<? extends BaseHandler>> pair1 : restControllers.entrySet()) {
            final String rootPath = pair1.getKey();
            final Class<? extends BaseHandler> handler = pair1.getValue();
            final BaseHandler instance = newInstance(handler);
            final Map<String, Method> postMappingMethods = Utils.getPostMappingMethods(handler);
            for (final Map.Entry<String, Method> pair2 : postMappingMethods.entrySet()) {
                final String childPath = pair2.getKey();
                final Method method = pair2.getValue();
                final String path = rootPath.concat(childPath);
                serverBootstrap.registerHandler(path, instance);
                context.getCacheWrapper().put(path, method);
            }
        }
        this.httpServer = serverBootstrap.create();
        this.httpServer.start();
        log.info(String.format("HttpServerWrapper: initialize the HttpServer with port[%s] with successfully", port));
    }

    private BaseHandler newInstance(final Class<? extends BaseHandler> tClass) throws IllegalAccessException, InstantiationException {
        final BaseHandler result = tClass.newInstance();
        final Set<Field> allFields = ReflectionUtils.getAllFields(tClass, field -> field.getType().isAssignableFrom(Context.class));
        for (final Field field : allFields) {
            field.setAccessible(true);
            field.set(result, this.context);
        }
        return result;
    }

    @Override
    public final void close() {
        if (null != this.httpServer) {
            this.httpServer.shutdown(10, TimeUnit.SECONDS);
        }
    }
}