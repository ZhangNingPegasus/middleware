package org.wyyt.apollo.tool;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.client.constant.ApolloOpenApiConstants;
import com.ctrip.framework.apollo.openapi.client.service.ItemOpenApiService;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenEnvClusterDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * the common functions of Apollo
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public class ApolloTool {
    private final String env;
    private final String appId;
    private final String operator;
    private final Escaper escaper;
    private final Gson gson;
    private final ApolloOpenApiClient apolloOpenApiClient;

    public ApolloTool(final String portalUrl,
                      final String apolloMeta,
                      final String token,
                      final String appId,
                      final String operator) {

        if (apolloMeta.contains(".dev.")) {
            env = "DEV";
        } else if (apolloMeta.contains(".test.")) {
            env = "FAT";
        } else if (apolloMeta.contains(".prod.")) {
            env = "PRO";
        } else {
            throw new RuntimeException("The value of config [apollo.meta] is incorrent");
        }
        this.appId = appId;
        this.operator = operator;
        this.escaper = UrlEscapers.urlPathSegmentEscaper();
        this.gson = new GsonBuilder().setDateFormat(ApolloOpenApiConstants.JSON_DATE_FORMAT).create();
        this.apolloOpenApiClient = ApolloOpenApiClient.newBuilder()
                .withPortalUrl(portalUrl)
                .withToken(token)
                .withConnectTimeout(5000)
                .withReadTimeout(5000)
                .build();
    }

    public Set<String> getEnvironments(final String appId) {
        final List<OpenEnvClusterDTO> envClusterInfo = this.apolloOpenApiClient.getEnvClusterInfo(appId);
        final Set<String> result = new HashSet<>(envClusterInfo.size());
        for (final OpenEnvClusterDTO openEnvClusterDTO : envClusterInfo) {
            result.add(openEnvClusterDTO.getEnv());
        }
        return result;
    }

    public Set<String> getEnvironments() {
        return this.getEnvironments(this.appId);
    }

    public Map<String, String> getConfigs(final String env,
                                          final String appId,
                                          String clusterName) throws Exception {
        if (Strings.isNullOrEmpty(clusterName)) {
            clusterName = ConfigConsts.CLUSTER_NAME_DEFAULT;
        }

        final List<OpenNamespaceDTO> openNamespaceDTOList = new ArrayList<>();
        this.execute(String.format("envs/%s/apps/%s/clusters/%s/namespaces/ex",
                escapePath(env),
                escapePath(appId),
                escapePath(clusterName)),
                response -> openNamespaceDTOList.addAll(gson.fromJson(response, new TypeToken<List<OpenNamespaceDTO>>() {
                }.getType())));
        final Map<String, String> result = new HashMap<>();
        if (openNamespaceDTOList.isEmpty()) {
            return result;
        }
        for (final OpenNamespaceDTO openNamespaceDTO : openNamespaceDTOList) {
            for (final OpenItemDTO item : openNamespaceDTO.getItems()) {
                result.put(item.getKey(), item.getValue());
            }
        }
        return result;
    }

    public Map<String, String> getConfigs(final String env,
                                          final String appId) throws Exception {
        return this.getConfigs(env, appId, null);
    }

    public Map<String, String> getConfigs(final String appId) throws Exception {
        return this.getConfigs(this.env, appId, null);
    }

    public Map<String, String> getConfigs() throws Exception {
        return this.getConfigs(this.env, this.appId, null);
    }

    public String getConfig(final String env,
                            final String appId,
                            String clusterName,
                            String namespaceName,
                            final String key) throws Exception {
        if (Strings.isNullOrEmpty(clusterName)) {
            clusterName = ConfigConsts.CLUSTER_NAME_DEFAULT;
        }
        if (Strings.isNullOrEmpty(namespaceName)) {
            namespaceName = ConfigConsts.NAMESPACE_APPLICATION;
        }

        final AtomicReference<OpenItemDTO> openItemDTOAtomicReference = new AtomicReference<>(null);
        this.execute(String.format("envs/%s/apps/%s/clusters/%s/namespaces/%s/items/%s/ex",
                escapePath(env),
                escapePath(appId),
                escapePath(clusterName),
                escapePath(namespaceName),
                escapePath(key)),
                response -> openItemDTOAtomicReference.set(gson.fromJson(response, OpenItemDTO.class)));

        final OpenItemDTO openItemDTO = openItemDTOAtomicReference.get();
        if (null == openItemDTO) {
            return null;
        }
        return openItemDTO.getValue();
    }

    public String getConfig(final String env,
                            final String appId,
                            final String key) throws Exception {
        return this.getConfig(env, appId, null, null, key);
    }

    public String getConfig(final String appId,
                            final String key) throws Exception {
        return this.getConfig(this.env, appId, null, null, key);
    }

    public String getConfig(final String key) throws Exception {
        return this.getConfig(this.env, this.appId, null, null, key);
    }

    public void updateConfig(final String env,
                             final String appId,
                             final String key,
                             String clusterName,
                             String namespaceName,
                             final String config) {
        if (Strings.isNullOrEmpty(clusterName)) {
            clusterName = ConfigConsts.CLUSTER_NAME_DEFAULT;
        }
        if (Strings.isNullOrEmpty(namespaceName)) {
            namespaceName = ConfigConsts.NAMESPACE_APPLICATION;
        }

        final Date now = new Date();

        final OpenItemDTO openItemDTO = new OpenItemDTO();
        openItemDTO.setKey(key);
        openItemDTO.setValue(config);
        openItemDTO.setComment("Operated by ".concat(this.operator));
        openItemDTO.setDataChangeCreatedBy(this.operator);
        openItemDTO.setDataChangeLastModifiedBy(this.operator);
        openItemDTO.setDataChangeCreatedTime(now);
        openItemDTO.setDataChangeLastModifiedTime(now);
        this.apolloOpenApiClient.createOrUpdateItem(appId, env, clusterName, namespaceName, openItemDTO);

        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setReleaseTitle(new SimpleDateFormat("yyyyMMddHHmmss").format(now) + "-release");
        namespaceReleaseDTO.setReleasedBy(this.operator);
        namespaceReleaseDTO.setReleaseComment("Released by ".concat(this.operator));
        namespaceReleaseDTO.setEmergencyPublish(true);
        this.apolloOpenApiClient.publishNamespace(appId, env, clusterName, namespaceName, namespaceReleaseDTO);
    }

    public void updateConfig(final String env,
                             final String appId,
                             final String key,
                             final String config) {
        this.updateConfig(env, appId, key, null, null, config);
    }

    public void updateConfig(final String appId,
                             final String key,
                             final String config) {
        this.updateConfig(this.env, appId, key, null, null, config);
    }

    public void updateConfig(final String key,
                             final String config) {
        this.updateConfig(this.env, this.appId, key, null, null, config);
    }

    public void clearConfig(final String env,
                            final String appId,
                            final String key,
                            String clusterName,
                            String namespaceName) {
        if (Strings.isNullOrEmpty(clusterName)) {
            clusterName = ConfigConsts.CLUSTER_NAME_DEFAULT;
        }
        if (Strings.isNullOrEmpty(namespaceName)) {
            namespaceName = ConfigConsts.NAMESPACE_APPLICATION;
        }

        this.apolloOpenApiClient.removeItem(appId, env, clusterName, namespaceName, key, this.operator);

        final NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setReleaseTitle(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "-release");
        namespaceReleaseDTO.setReleasedBy(operator);
        namespaceReleaseDTO.setReleaseComment("Deleted by " + operator);
        namespaceReleaseDTO.setEmergencyPublish(true);
        this.apolloOpenApiClient.publishNamespace(appId, env, clusterName, namespaceName, namespaceReleaseDTO);
    }

    public void clearConfig(final String env,
                            final String appId,
                            final String key) {
        this.clearConfig(env, appId, key, null, null);
    }

    public void clearConfig(final String appId,
                            final String key) {
        this.clearConfig(this.env, appId, key, null, null);
    }

    public void clearConfig(final String key) {
        this.clearConfig(this.env, this.appId, key, null, null);
    }

    private void execute(final String path,
                         final Handle handle) throws Exception {
        final ItemOpenApiService itemOpenApiService = obtainItemOpenApiService();
        final Method getMethod = obtainGetMethod();
        try (final CloseableHttpResponse response = (CloseableHttpResponse) getMethod.invoke(itemOpenApiService, path)) {
            handle.process(EntityUtils.toString(response.getEntity()).trim());
        }
    }

    private ItemOpenApiService obtainItemOpenApiService() throws IllegalAccessException {
        ItemOpenApiService result = null;
        for (final Field field : ApolloOpenApiClient.class.getDeclaredFields()) {
            if (field.getType().isAssignableFrom(ItemOpenApiService.class)) {
                field.setAccessible(true);
                result = (ItemOpenApiService) field.get(this.apolloOpenApiClient);
                break;
            }
        }
        if (null == result) {
            throw new RuntimeException("Not found the field of type [ItemOpenApiService] in the Class[ApolloOpenApiClient]");
        }
        return result;
    }

    private Method obtainGetMethod() {
        final Set<Method> allMethods = ReflectionUtils.getAllMethods(ItemOpenApiService.class, method -> {
            if ("get".equals(method.getName())) {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                return 1 == parameterTypes.length && parameterTypes[0] == String.class;
            }
            return false;
        });

        if (allMethods.isEmpty()) {
            throw new RuntimeException("Cannot found function: CloseableHttpResponse get(String)");
        }

        final Method result = allMethods.iterator().next();
        result.setAccessible(true);
        return result;
    }

    private Method obtainPostMethod() {
        final Set<Method> allMethods = ReflectionUtils.getAllMethods(ApolloOpenApiClient.class, method -> {
            if ("post".equals(method.getName())) {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                return 2 == parameterTypes.length &&
                        parameterTypes[0] == String.class &&
                        parameterTypes[1] == Object.class;
            }
            return false;
        });

        if (allMethods.isEmpty()) {
            throw new RuntimeException("Cannot found function: CloseableHttpResponse post(String, Object)");
        }

        final Method result = allMethods.iterator().next();
        result.setAccessible(true);
        return result;
    }

    private Method obtainPutMethod() {
        final Set<Method> allMethods = ReflectionUtils.getAllMethods(ApolloOpenApiClient.class, method -> {
            if ("put".equals(method.getName())) {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                return 2 == parameterTypes.length &&
                        parameterTypes[0] == String.class &&
                        parameterTypes[1] == Object.class;
            }
            return false;
        });

        if (allMethods.isEmpty()) {
            throw new RuntimeException("Cannot found function: CloseableHttpResponse put(String, Object)");
        }

        final Method result = allMethods.iterator().next();
        result.setAccessible(true);
        return result;
    }

    private Method obtainDeleteMethod() {
        final Set<Method> allMethods = ReflectionUtils.getAllMethods(ApolloOpenApiClient.class, method -> {
            if ("delete".equals(method.getName())) {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                return 1 == parameterTypes.length && parameterTypes[0] == String.class;
            }
            return false;
        });

        if (allMethods.isEmpty()) {
            throw new RuntimeException("Cannot found function: CloseableHttpResponse delete(String)");
        }

        final Method result = allMethods.iterator().next();
        result.setAccessible(true);
        return result;
    }

    private String escapePath(final String path) {
        return escaper.escape(path);
    }

    interface Handle {
        void process(final String response);
    }
}