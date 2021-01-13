package org.wyyt.springcloud.permission;

import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import com.nepxion.matrix.proxy.aop.DefaultAutoScanProxy;
import com.nepxion.matrix.proxy.mode.ProxyMode;
import com.nepxion.matrix.proxy.mode.ScanMode;
import com.nepxion.matrix.proxy.util.ProxyUtil;
import org.apache.commons.lang3.StringUtils;
import org.reflections.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import org.wyyt.springcloud.entity.dto.ApiDto;
import org.wyyt.springcloud.exception.PermissionException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class PermissionAutoScanProxy extends DefaultAutoScanProxy {
    private String[] commonInterceptorNames;
    private Class<? extends Annotation>[] annotations;
    private final List<ApiDto> permissions = new ArrayList<>();
    private final PluginAdapter pluginAdapter;

    public PermissionAutoScanProxy(final String scanPackages,
                                   final PluginAdapter pluginAdapter) {
        super(scanPackages, ProxyMode.BY_CLASS_OR_METHOD_ANNOTATION, ScanMode.FOR_CLASS_OR_METHOD_ANNOTATION);
        this.pluginAdapter = pluginAdapter;
    }

    public List<ApiDto> getPermissions() {
        return this.permissions;
    }

    @Override
    protected String[] getCommonInterceptorNames() {
        if (this.commonInterceptorNames == null) {
            this.commonInterceptorNames = new String[]{"permissionInterceptor"};
        }
        return this.commonInterceptorNames;
    }

    @Override
    protected Class<? extends Annotation>[] getClassAnnotations() {
        if (this.annotations == null) {
            this.annotations = new Class[]{Permission.class};
        }
        return this.annotations;
    }

    @Override
    protected Class<? extends Annotation>[] getMethodAnnotations() {
        if (this.annotations == null) {
            this.annotations = new Class[]{Permission.class};
        }
        return annotations;
    }

    @Override
    protected void classAnnotationScanned(final Class<?> targetClass,
                                          final Class<? extends Annotation> classAnnotation) {
        if (classAnnotation == Permission.class) {
            final Permission permissionAnnotation = targetClass.getAnnotation(Permission.class);
            scanned(targetClass, null, permissionAnnotation);
        }
    }

    @Override
    protected void methodAnnotationScanned(final Class<?> targetClass,
                                           final Method method,
                                           final Class<? extends Annotation> methodAnnotation) {
        if (methodAnnotation == Permission.class) {
            final Permission permissionAnnotation = method.getAnnotation(Permission.class);
            scanned(targetClass, method, permissionAnnotation);
        }
    }

    private void scanned(final Class<?> targetClass,
                         final Method method,
                         final Permission permissionAnnotation) {
        final String name = permissionAnnotation.name();
        if (StringUtils.isEmpty(name)) {
            throw new PermissionException("Annotation @Permission's name is required");
        }

        final ApiDto apiDto = new ApiDto();
        apiDto.setName(permissionAnnotation.name());
        apiDto.setDescription(permissionAnnotation.description());
        apiDto.setServiceId(this.pluginAdapter.getServiceId());
        apiDto.setClassName(targetClass.getName());

        if (null != method) {
            apiDto.setMethodName(method.getName());
            apiDto.setParameterTypes(ProxyUtil.toString(method.getParameterTypes()));
            this.populatePath(apiDto, targetClass, method);
        }

        final String group = this.pluginAdapter.getGroup();
        final String serviceType = this.pluginAdapter.getServiceType();
        permissions.add(apiDto);
    }

    private void populatePath(final ApiDto apiDto,
                              final Class<?> targetClass,
                              final Method method) {
        GetMapping getMapping = null;
        PostMapping postMapping = null;
        DeleteMapping deleteMapping = null;
        PutMapping putMapping = null;
        RequestMapping requestMapping = null;

        final Set<Class<?>> superTypes = ReflectionUtils.getSuperTypes(targetClass);
        final Set<Annotation> annotations = new HashSet<>();
        for (final Class<?> superType : superTypes) {
            final Set<Method> methods = ReflectionUtils.getMethods(superType, m ->
                    method.getName().equals(m.getName()) &&
                            ProxyUtil.toString(method.getParameterTypes()).equals(ProxyUtil.toString(m.getParameterTypes())) &&
                            method.getReturnType().equals(m.getReturnType()));
            for (final Method m : methods) {
                annotations.addAll(ReflectionUtils.getAnnotations(m, annotation ->
                        annotation.annotationType().isAssignableFrom(GetMapping.class) ||
                                annotation.annotationType().isAssignableFrom(PostMapping.class) ||
                                annotation.annotationType().isAssignableFrom(DeleteMapping.class) ||
                                annotation.annotationType().isAssignableFrom(PutMapping.class)));
            }
            if (null == requestMapping) {
                requestMapping = superType.getAnnotation(RequestMapping.class);
            }
        }

        if (null == requestMapping) {
            requestMapping = targetClass.getAnnotation(RequestMapping.class);
        }

        if (annotations.isEmpty()) {
            getMapping = method.getAnnotation(GetMapping.class);
            postMapping = method.getAnnotation(PostMapping.class);
            deleteMapping = method.getAnnotation(DeleteMapping.class);
            putMapping = method.getAnnotation(PutMapping.class);
        } else {
            final Annotation annotation = annotations.iterator().next();
            if (annotation.annotationType().isAssignableFrom(GetMapping.class)) {
                getMapping = (GetMapping) annotation;
            } else if (annotation.annotationType().isAssignableFrom(PostMapping.class)) {
                postMapping = (PostMapping) annotation;
            } else if (annotation.annotationType().isAssignableFrom(DeleteMapping.class)) {
                deleteMapping = (DeleteMapping) annotation;
            } else if (annotation.annotationType().isAssignableFrom(PutMapping.class)) {
                putMapping = (PutMapping) annotation;
            }
        }

        String[] paths = null;
        if (null != getMapping) {
            apiDto.setMethod("get");
            paths = getMapping.path();
        } else if (null != postMapping) {
            apiDto.setMethod("post");
            paths = postMapping.path();
        } else if (null != deleteMapping) {
            apiDto.setMethod("delete");
            paths = deleteMapping.path();
        } else if (null != putMapping) {
            apiDto.setMethod("put");
            paths = putMapping.path();
        }

        if (null != requestMapping) {
            String rootPath = requestMapping.path()[0];
            if (rootPath.endsWith("/")) {
                rootPath = rootPath.substring(0, rootPath.length() - 1);
            }
            for (int i = 0; i < Objects.requireNonNull(paths).length; i++) {
                if (paths[i].startsWith("/")) {
                    paths[i] = paths[i].substring(1, rootPath.length());
                }
                paths[i] = String.format("%s/%s", rootPath, paths[i]);
            }
        }
        apiDto.setPath(ProxyUtil.toString(paths));
    }
}