package org.wyyt.springcloud.gateway.advice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.wyyt.springcloud.gateway.entity.contants.Names;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.rpc.Result;
import org.wyyt.tool.rpc.ResultCode;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

/**
 * the attributes of global error
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {
    @Override
    public Map<String, Object> getErrorAttributes(final ServerRequest request,
                                                  final ErrorAttributeOptions options) {
        final Map<String, Object> errorAttributes = new HashMap<>();
        final Throwable error = getError(request);
        final URI url = request.exchange().getAttribute(GATEWAY_REQUEST_URL_ATTR);
        Result<?> result;
        if (null != url && error.getClass().isAssignableFrom(NotFoundException.class)) {
            final String version = getVersion(request);
            if (ObjectUtils.isEmpty(version)) {
                result = Result.error(ResultCode.UNABLE_TO_ACCESS.getCode(), String.format("Unable to find instance for \"%s\"", url.getHost()));
            } else {
                result = Result.error(ResultCode.UNABLE_TO_ACCESS.getCode(), String.format("Unable to find instance for \"%s\" with version \"%s\"", url.getHost(), version));
            }
        } else {
            final String errorMsg = ExceptionTool.getRootCauseMessage(error);
            final ResultCode respondCode = ResultCode.get(errorMsg);
            if (null == respondCode) {
                result = Result.error(errorMsg);
            } else {
                result = Result.create(respondCode);
            }
        }
        errorAttributes.put(Names.ERROR_PARAMETER_KEY, JSON.toJSONString(result));
        return errorAttributes;
    }

    private static String getVersion(final ServerRequest serverRequest) {
        final String version = serverRequest.exchange().getRequest().getHeaders().getFirst(Names.N_D_VERSION);
        if (ObjectUtils.isEmpty(version)) {
            return null;
        }
        final URI url = serverRequest.exchange().getAttribute(GATEWAY_REQUEST_URL_ATTR);
        if (null == url) {
            return null;
        }
        final String serviceName = url.getHost();
        try {
            final JSONObject versionJson = JSONObject.parseObject(version);
            for (final Map.Entry<String, Object> pair : versionJson.entrySet()) {
                if (serviceName.equals(pair.getKey())) {
                    return pair.getValue().toString();
                }
            }
            return null;
        } catch (final Exception exception) {
            log.error(ExceptionTool.getRootCauseMessage(exception), exception);
            return null;
        }
    }
}
