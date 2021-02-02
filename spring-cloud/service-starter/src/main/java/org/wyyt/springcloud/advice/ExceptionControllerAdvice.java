package org.wyyt.springcloud.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.rpc.Result;

/**
 * As the base class of the interface controller. Provide common functions such as unified error handling.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
@ControllerAdvice
public class ExceptionControllerAdvice {
    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public Object handleOtherException(final Exception e) {
        final String errMsg = ExceptionTool.getRootCauseMessage(e);
        log.error(errMsg, e);
        return Result.error(errMsg);
    }
}