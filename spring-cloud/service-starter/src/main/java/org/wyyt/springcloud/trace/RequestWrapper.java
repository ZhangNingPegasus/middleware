package org.wyyt.springcloud.trace;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

public class RequestWrapper extends HttpServletRequestWrapper {
    private final byte[] body;

    public RequestWrapper(final HttpServletRequest request) {
        super(request);
        final String sessionStream = this.getBodyString(request);
        this.body = sessionStream.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.body);
        return new ServletInputStream() {
            @Override
            public int read() {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(final ReadListener readListener) {
            }
        };
    }

    public String getBodyString(final ServletRequest request) {
        final String contentType = request.getContentType();
        StringBuilder bodyString = new StringBuilder();
        if (!ObjectUtils.isEmpty(contentType) &&
                (contentType.contains("multipart/form-data") ||
                        contentType.contains("x-www-form-urlencoded"))) {

            final Enumeration<String> enumeration = request.getParameterNames();
            while (enumeration.hasMoreElements()) {
                final String n = enumeration.nextElement();
                bodyString.append(String.format("%s=%s&", n, request.getParameter(n)));
            }

            if (bodyString.toString().endsWith("&")) {
                return bodyString.substring(0, bodyString.length() - 1);
            } else {
                return bodyString.toString();
            }
        }
        try {
            byte[] byteArray = StreamUtils.copyToByteArray(request.getInputStream());
            bodyString = new StringBuilder(new String(byteArray, StandardCharsets.UTF_8));
        } catch (final IOException ignored) {
        }
        return bodyString.toString();
    }
}