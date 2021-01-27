package org.wyyt.springcloud.trace;


import brave.Span;
import brave.Tracer;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class HttpResponseInjectingTraceFilter extends GenericFilterBean {

    private final Tracer tracer;

    public HttpResponseInjectingTraceFilter(final Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void doFilter(final ServletRequest servletRequest,
                         final ServletResponse servletResponse,
                         final FilterChain filterChain) throws IOException, ServletException {
        final Span span = this.tracer.currentSpan();
        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        final Map<String, String> headerMap = new HashMap<>();
        final Enumeration<String> headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String key = headerNames.nextElement();
            final String value = httpRequest.getHeader(key);
            headerMap.put(key, value);
        }
        this.fillTag(span, "http.head", headerMap);


        final RequestWrapper requestWrapper = new RequestWrapper(httpRequest);
        final ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) servletResponse);

        filterChain.doFilter(requestWrapper, responseWrapper);
        this.fillTag(span, "http.params", this.getRequestParameter(requestWrapper));

        final byte[] content = responseWrapper.getContent();
        if (null != content && content.length > 0) {
            final String result = new String(content, StandardCharsets.UTF_8);
            this.fillTag(span, "http.result", result);
        }

        final ServletOutputStream out = servletResponse.getOutputStream();
        if (null != out) {
            if (null != content) {
                out.write(content);
            }
            out.flush();
            out.close();
        }
    }

    private String getRequestParameter(final RequestWrapper requestWrapper) {
        if (null == requestWrapper) {
            return null;
        }
        String params;
        String method = requestWrapper.getMethod();
        if (StringUtils.isNotBlank(method) && "GET".equalsIgnoreCase(method)) {
            // 获取请求体中的字符串(get)
            params = requestWrapper.getQueryString();
            try {
                if (StringUtils.isNotBlank(params)) {
                    return URLDecoder.decode(params, "UTF-8");
                } else {
                    return "";
                }
            } catch (UnsupportedEncodingException e) {
                return "";
            }
        } else {
            return requestWrapper.getBodyString(requestWrapper);
        }
    }

    private void fillTag(final Span span,
                         final String key,
                         final String value) {
        if (ObjectUtils.isEmpty(value)) {
            span.tag(key, "");
        } else {
            span.tag(key, value);
        }
    }

    private void fillTag(final Span span,
                         final String key,
                         final Map<String, String> value
    ) {
        if (null == value || value.isEmpty()) {
            this.fillTag(span, key, "");
        }
        this.fillTag(span, key, JSONObject.toJSONString(value));
    }
}
