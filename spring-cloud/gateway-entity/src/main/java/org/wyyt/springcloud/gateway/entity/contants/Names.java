package org.wyyt.springcloud.gateway.entity.contants;

/**
 * the constants
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020       Initialize   *
 * *****************************************************************
 */
public class Names {
    public final static String ERROR_PARAMETER_KEY = "_RESULT_";
    public static final String API_KEY = "npenazsugshaggni";
    public static final String API_IV = "aesusgpgianzgnhn";
    public static final String CACHED_REQUEST_BODY_OBJECT_KEY = "cached_request_body_object_key";
    public final static String ACCESS_TOKEN = "access_token";
    public final static String JWT_SIGNING_KEY = "npegnaskugsihazgdni";


    public static final String REDIS_ACCESS_TOKEN_KEY = "access_token_of_%s";

    public static final String REDIS_IGNORE_URLS_KEY = "ignore_urls";
    public static final String REDIS_DISTRIBUTED_LOCK_IGNORE_URLS_KEY = "distributed_lock_ignore_urls";

    public static final String REDIS_APP_OF_CLIENT_ID_KEY = "app_of_client_id_%s";
    public static final String REDIS_DISTRIBUTED_LOCK_APP_OF_CLIENT_ID_KEY = "distributed_lock_app_of_client_id_%s";

    public static final String REDIS_API_LIST_OF_CLIENT_ID_KEY = "api_list_of_client_id_%s";
    public static final String REDIS_DISTRIBUTED_LOCK_API_LIST_OF_CLIENT_ID_KEY = "distributed_lock_api_list_of_client_id_%s";
}
