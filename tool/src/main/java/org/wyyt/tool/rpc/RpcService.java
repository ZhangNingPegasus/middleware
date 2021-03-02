package org.wyyt.tool.rpc;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * The service for RPC
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public class RpcService {
    private static final Integer CONNECTION_TIMEOUT = 30000;
    private static final Integer SOCKET_TIMEOUT = 30000;
    private static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";

    public byte[] getFileBytes(final String url) {
        return Unirest.get(url).asBytes().getBody();
    }

    public String get(final String url,
                      @Nullable final Map<String, String> headers,
                      @Nullable final Map<String, Object> params) {
        return Unirest.get(url)
                .connectTimeout(CONNECTION_TIMEOUT)
                .socketTimeout(SOCKET_TIMEOUT)
                .headers(headers)
                .queryString(params)
                .asString()
                .getBody();
    }

    public String get(final String url,
                      final Map<String, Object> params) {
        return get(url, null, params);
    }

    public String get(final String url) {
        return get(url, null, null);
    }

    public String post(final String url,
                       final String contentType,
                       @Nullable final Map<String, String> headers,
                       @Nullable final Object params) {
        final HttpRequestWithBody httpRequestWithBody = Unirest.post(url)
                .connectTimeout(CONNECTION_TIMEOUT)
                .socketTimeout(SOCKET_TIMEOUT)
                .contentType(contentType)
                .charset(StandardCharsets.UTF_8)
                .headers(headers);

        if (params instanceof Map) {
            return httpRequestWithBody.fields((Map<String, Object>) params).asString().getBody();
        }
        return httpRequestWithBody.body(params).asString().getBody();
    }

    public String post(final String url,
                       final Map<String, String> headers,
                       final Object params) {
        return this.post(url, DEFAULT_CONTENT_TYPE, headers, params);
    }

    public String post(final String url,
                       final String contentType,
                       @Nullable final Map<String, String> headers) {
        return post(url, contentType, headers, null);
    }

    public String post(final String url,
                       final String contentType,
                       @Nullable final Object params) {
        return post(url, contentType, null, params);
    }

    public String post(final String url,
                       final String contentType) {
        return post(url, contentType, null, null);
    }

    public String post(final String url) {
        return this.post(url, DEFAULT_CONTENT_TYPE);
    }

    public <T> T postForEntity(final String url,
                               final String contentType,
                               final Map<String, String> headers,
                               final Object params,
                               final TypeReference<T> typeReference) {
        final String responseText = this.post(url, contentType, headers, params);
        if (ObjectUtils.isEmpty(responseText)) {
            return null;
        }
        return JSONObject.parseObject(responseText, typeReference);
    }

    public <T> T postForEntity(final String url,
                               final Map<String, String> headers,
                               final Object params,
                               final TypeReference<T> typeReference) {
        return this.postForEntity(url, DEFAULT_CONTENT_TYPE, headers, params, typeReference);
    }


    public <T> T postForEntity(final String url,
                               final String contentType,
                               final Object params,
                               final TypeReference<T> typeReference) {
        return this.postForEntity(url, contentType, null, params, typeReference);
    }


    public <T> T postForEntity(final String url,
                               final Object params,
                               final TypeReference<T> typeReference) {
        return this.postForEntity(url, DEFAULT_CONTENT_TYPE, params, typeReference);
    }
}