package org.wyyt.admin.ui.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.wyyt.tool.exception.ExceptionTool;
import org.wyyt.tool.rpc.Result;
import org.wyyt.tool.rpc.ResultCode;

import javax.servlet.http.HttpServletRequest;

/**
 * As the base class of the interface controller. Provide common functions such as unified error handling.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
@ControllerAdvice
public class ExceptionControllerAdvice {
    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public Object handleOtherException(final Model model,
                                       final HttpServletRequest request,
                                       final Exception e) {
        return handleException(model, request, e);
    }

    private Object handleException(final Model model,
                                   final HttpServletRequest request,
                                   final Exception exception) {
        final String errorMsg = ExceptionTool.getRootCauseMessage(exception);
        log.error(errorMsg, exception);
        if (isAjax(request)) {
            final ResultCode respondCode = ResultCode.get(errorMsg);
            if (null == respondCode) {
                return Result.error(errorMsg);
            } else {
                return Result.create(respondCode);
            }
        } else {
            model.addAttribute("error", getStackTrace(exception));
            return new ModelAndView("error/500");
        }
    }

    private boolean isAjax(final HttpServletRequest request) {
        return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
    }

    private String getStackTrace(final Exception exception) {
        return ExceptionTool.getStackTraceInHtml(exception);
    }
}