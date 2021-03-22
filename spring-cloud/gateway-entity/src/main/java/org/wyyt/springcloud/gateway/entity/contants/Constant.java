package org.wyyt.springcloud.gateway.entity.contants;

/**
 * the constants
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public class Constant {
    public final static String ERROR_PARAMETER_KEY = "_RESULT_";
    public static final String API_KEY = "npenazsugshaggni";
    public static final String API_IV = "aesusgpgianzgnhn";
    public static final String CACHED_REQUEST_BODY_OBJECT_KEY = "cached_request_body_object_key";
    public final static String JWT_SIGNING_KEY = "npegnaskugsihazgdni";

    public final static String OAUTH_TOKEN = "oauth/token";
    public final static String API_DOCS = "v2/api-docs";
    public final static String EXPIRES_IN = "expires_in";

    public static final String SERVICE_NAME = "service_name";
    public static final String ROUTE_PATH = "route_path";

    public final static String CLIENT_SECRET = "client_secret";
    public final static String GRANT_TYPE = "grant_type";

    public static final String REDIS_ACCESS_TOKEN_KEY = "scg_access_token_%s";

    public static final String REDIS_IGNORE_URLS_KEY = "scg_ignore_urls";
    public static final String REDIS_DISTRIBUTED_LOCK_IGNORE_URLS_KEY = "scg_lock_ignore_urls";

    public static final String REDIS_APP_OF_CLIENT_ID_KEY = "scg_app_%s";
    public static final String REDIS_DISTRIBUTED_LOCK_APP_OF_CLIENT_ID_KEY = "scg_lock_app_%s";

    public static final String REDIS_API_LIST_OF_CLIENT_ID_KEY = "scg_api_%s";
    public static final String REDIS_DISTRIBUTED_LOCK_API_LIST_OF_CLIENT_ID_KEY = "scg_lock_api_%s";

    public static String getAccessTokenRedisKey(final String clientId) {
        return String.format(Constant.REDIS_ACCESS_TOKEN_KEY, clientId);
    }

    public static String getAppOfClientId(final String clientId) {
        return String.format(Constant.REDIS_APP_OF_CLIENT_ID_KEY, clientId);
    }

    public static String getApiListOfClientIdKey(final String clientId) {
        return String.format(Constant.REDIS_API_LIST_OF_CLIENT_ID_KEY, clientId);
    }
}