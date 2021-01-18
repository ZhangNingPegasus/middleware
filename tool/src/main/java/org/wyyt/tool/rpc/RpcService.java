package org.wyyt.tool.rpc;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.lang.Nullable;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.resource.ResourceTool;

import javax.net.ssl.SSLContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The service for RPC
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public class RpcService {
    private static final Integer CONNECTION_TIMEOUT = 30000;
    private static final Integer REQUEST_TIMEOUT = 30000;
    private static final Integer SOCKET_TIMEOUT = 30000;

    public String get(final String url) throws Exception {
        if (url.toLowerCase().startsWith("https:")) {
            return get(url, null, null, true);
        } else {
            return get(url, null, null, false);
        }
    }

    public String get(final String url,
                      final Map<String, Object> params) throws Exception {
        if (url.toLowerCase().startsWith("https:")) {
            return get(url, params, null, true);
        } else {
            return get(url, params, null, false);
        }
    }

    public String get(final String url,
                      final Map<String, Object> params,
                      final Map<String, String> headers) throws Exception {
        if (url.toLowerCase().startsWith("https:")) {
            return get(url, params, headers, true);
        } else {
            return get(url, params, headers, false);
        }
    }

    public String post(final String url) throws Exception {
        if (url.toLowerCase().startsWith("https:")) {
            return post(url, null, null, true);
        } else {
            return post(url, null, null, false);
        }
    }

    public String post(final String url,
                       final Map<String, Object> params) throws Exception {
        if (url.toLowerCase().startsWith("https:")) {
            return post(url, params, null, true);
        } else {
            return post(url, params, null, false);
        }
    }

    public String post(final String url,
                       final Map<String, Object> params,
                       final Map<String, String> headers) throws Exception {
        if (url.toLowerCase().startsWith("https:")) {
            return post(url, params, headers, true);
        } else {
            return post(url, params, headers, false);
        }
    }

    private String get(String url,
                       @Nullable final Map<String, Object> params,
                       @Nullable final Map<String, String> headers,
                       final boolean isHttps) throws Exception {
        String strResult;
        CloseableHttpClient httpClient;
        if (isHttps) {
            httpClient = createSSLClientDefault();
        } else {
            httpClient = HttpClients.createDefault();
        }
        HttpGet request = null;
        try {
            final StringBuilder strParams = new StringBuilder();
            if (params != null && params.size() > 0) {
                for (final Map.Entry<String, Object> pair : params.entrySet()) {
                    strParams.append(String.format("%s=%s&", pair.getKey(), pair.getValue().toString()));
                }
                url = String.format("%s?%s", url, strParams.substring(0, strParams.length() - 1));
            }
            request = new HttpGet(url);
            if (null != headers && !headers.isEmpty()) {
                final List<Header> _headers = new ArrayList<>(headers.size());
                for (final Map.Entry<String, String> header : headers.entrySet()) {
                    _headers.add(new BasicHeader(header.getKey(), header.getValue()));
                }
                request.setHeaders(_headers.toArray(new Header[]{}));
            }
            request.setConfig(RequestConfig // 配置
                    .custom() // 开启自定义模式
                    .setConnectTimeout(CONNECTION_TIMEOUT) // 设置超时连接超时时间
                    .setConnectionRequestTimeout(REQUEST_TIMEOUT) // 设置连接后的请求处理超时时间
                    .setSocketTimeout(SOCKET_TIMEOUT) // 设置整体socket的超时时间
                    .build());
            final HttpResponse response = httpClient.execute(request);
            strResult = EntityUtils.toString(response.getEntity());
        } finally {
            if (null != request) {
                request.releaseConnection();
            }
            ResourceTool.closeQuietly(httpClient);
        }
        return strResult;
    }

    private String post(final String url,
                        @Nullable final Map<String, Object> params,
                        @Nullable final Map<String, String> headers,
                        final boolean isHttps) throws Exception {
        String strResult;
        CloseableHttpClient httpClient;
        if (isHttps) {
            httpClient = createSSLClientDefault();
        } else {
            httpClient = HttpClients.createDefault();
        }
        final HttpPost request = new HttpPost(url);
        try {
            request.setConfig(RequestConfig // 配置
                    .custom() // 开启自定义模式
                    .setConnectTimeout(CONNECTION_TIMEOUT) // 设置超时连接超时时间
                    .setConnectionRequestTimeout(REQUEST_TIMEOUT) // 设置连接后的请求处理超时时间
                    .setSocketTimeout(SOCKET_TIMEOUT) // 设置整体socket的超时时间
                    .build());
            if (null != params && params.size() > 0) {
                final List<NameValuePair> _params = new ArrayList<>(params.size());
                for (final Map.Entry<String, Object> pair : params.entrySet()) {
                    String value;
                    if (null == pair.getValue()) {
                        value = "";
                    } else {
                        value = pair.getValue().toString();
                    }
                    _params.add(new BasicNameValuePair(pair.getKey(), value));
                }
                final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(_params, "utf-8");
                entity.setContentType("application/x-www-form-urlencoded");
                entity.setContentEncoding("UTF-8");
                request.setEntity(entity);
            }
            if (null != headers && headers.size() > 0) {
                final List<Header> _headers = new ArrayList<>(headers.size());
                for (final Map.Entry<String, String> header : headers.entrySet()) {
                    _headers.add(new BasicHeader(header.getKey(), header.getValue()));
                }
                request.setHeaders(_headers.toArray(new Header[]{}));
            }
            final HttpResponse result = httpClient.execute(request);
            strResult = EntityUtils.toString(result.getEntity());
        } finally {
            request.releaseConnection();
            ResourceTool.closeQuietly(httpClient);
        }
        return strResult;
    }

    private CloseableHttpClient createSSLClientDefault() {
        try {
            final SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (chain, authType) -> true).build();
            final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
            return HttpClients.custom().setSSLSocketFactory(sslsf).build();
        } catch (final Exception e) {
            log.error(String.format("Create SSL HttpClient meet error, [%s]", ExceptionTool.getRootCauseMessage(e)), e);
        }
        return HttpClients.createDefault();
    }
}