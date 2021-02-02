package org.wyyt.sharding.db2es.client.common;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.springframework.util.ObjectUtils;
import org.wyyt.sharding.db2es.client.http.BaseHandler;
import org.wyyt.sharding.db2es.client.http.HttpServerWrapper;
import org.wyyt.sharding.db2es.client.http.anno.PostMapping;
import org.wyyt.sharding.db2es.client.http.anno.RestController;
import org.wyyt.sharding.db2es.core.exception.Db2EsException;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * the common function
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class Utils {
    public static IP getLocalIp(final Context context) {
        final IP result = new IP();
        try {
            final InetAddress ia = InetAddress.getLocalHost();
            result.setLocalName(ia.getHostName());
            final String db2EsHost = context.getConfig().getDb2EsHost();
            if (ObjectUtils.isEmpty(db2EsHost)) {
                result.setLocalIp(ia.getHostAddress());
            } else {
                result.setLocalIp(db2EsHost);
            }
        } catch (final Exception ignored) {
        }
        return result;
    }

    public static String getLocalIpInfo(final Context context) {
        final IP ip = getLocalIp(context);
        return String.format("%s(%s)", ip.localName, ip.localIp);
    }

    public static Map<String, Method> getPostMappingMethods(final Class<?> tClass) {
        final Map<String, Method> result = new HashMap<>();

        final Set<Method> allMethods = ReflectionUtils.getAllMethods(tClass);
        for (final Method method : allMethods) {
            final PostMapping annotation = method.getAnnotation(PostMapping.class);
            if (null == annotation) {
                continue;
            }
            final String path = path(annotation.value().trim());
            if (result.containsKey(path)) {
                throw new Db2EsException(String.format("HttpServerWrapper: @PostMapping[%s] already existed", annotation.value()));
            }
            result.put(path, method);
        }
        return result;
    }

    public static Map<String, Class<? extends BaseHandler>> getRestControllers() {
        final Map<String, Class<? extends BaseHandler>> result = new HashMap<>();
        final Set<Class<? extends BaseHandler>> allHandlers = getAllHandlers(HttpServerWrapper.class.getPackage().getName());
        for (final Class<? extends BaseHandler> handler : allHandlers) {
            final RestController annotation = handler.getAnnotation(RestController.class);
            if (null == annotation) {
                continue;
            }
            final String path = path(annotation.value().trim());
            if (result.containsKey(path)) {
                throw new Db2EsException(String.format("HttpServerWrapper: @RestController[%s] already existed", annotation.value()));
            }
            result.put(path, handler);
        }
        return result;
    }

    private static Set<Class<? extends BaseHandler>> getAllHandlers(final String... packages) {
        final Reflections reflections = new Reflections((Object[]) packages);
        return reflections.getSubTypesOf(BaseHandler.class);
    }

    private static String path(String path) {
        if (!path.startsWith("/")) {
            path = "/".concat(path);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    @Data
    public static class IP {
        private String localName;
        private String localIp;
    }
}