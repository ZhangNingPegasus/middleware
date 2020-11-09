package org.wyyt.kafka.monitor.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.web.Result;
import org.wyyt.tool.web.ResultCode;

import javax.servlet.http.HttpServletRequest;

/**
 * As the base class of the interface controller. Provide common functions such as unified error handling.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
@ControllerAdvice
public final class ExceptionControllerAdvice {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public final Object handleOtherException(final Model model,
                                             final HttpServletRequest request,
                                             final Exception e) {
        return handleException(model, request, e);
    }

    private Object handleException(final Model model,
                                   final HttpServletRequest request,
                                   final Exception exception) {
        final String errorMsg = ExceptionTool.getRootCauseMessage(exception);
        log.error(errorMsg, exception);
        if (this.isAjax(request)) {
            final ResultCode respondCode = ResultCode.get(errorMsg);
            if (null == respondCode) {
                return Result.error(errorMsg);
            } else {
                return Result.create(respondCode);
            }
        } else {
            model.addAttribute("error", ExceptionTool.getStackTraceInHtml(exception));
            return new ModelAndView("error/500");
        }
    }

    private boolean isAjax(final HttpServletRequest request) {
        return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
    }
}