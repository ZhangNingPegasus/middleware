package org.wyyt.springcloud.gateway.advice;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.wyyt.springcloud.gateway.entity.contants.Names;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.rpc.Result;
import org.wyyt.tool.rpc.ResultCode;

import java.util.HashMap;
import java.util.Map;

/**
 * the attributes of global error
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {
    @Override
    public Map<String, Object> getErrorAttributes(final ServerRequest request,
                                                  final ErrorAttributeOptions options) {
        final Throwable error = getError(request);
        final String errorMsg = ExceptionTool.getRootCauseMessage(error);
        log.error(errorMsg, error);
        Result<?> result;
        final ResultCode respondCode = ResultCode.get(errorMsg);
        if (null == respondCode) {
            result = Result.error(errorMsg);
        } else {
            result = Result.create(respondCode);
        }
        final Map<String, Object> errorAttributes = new HashMap<>();
        errorAttributes.put(Names.ERROR_PARAMETER_KEY, JSON.toJSONString(result));
        return errorAttributes;
    }
}
