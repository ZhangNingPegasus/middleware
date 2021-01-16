package org.wyyt.tool.exception;

import cn.hutool.core.exceptions.ExceptionUtil;

/**
 * the common functions of Exception
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
public final class ExceptionTool {
    public static String getRootCauseMessage(final Throwable throwable) {
        Throwable rootCause = ExceptionUtil.getRootCause(throwable);
        if (null == rootCause) {
            return "";
        }
        return null == rootCause.getMessage() ? rootCause.toString() : rootCause.getMessage();
    }

    public static String getMessage(final Throwable throwable) {
        return ExceptionUtil.getMessage(throwable);
    }

    public static String getStackTrace(final Throwable throwable) {
        return ExceptionUtil.stacktraceToString(throwable);
    }

    public static String getStackTraceInHtml(final Throwable throwable) {
        return getStackTrace(throwable).replaceAll("\\r\\n\\t", "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
    }
}