package org.wyyt.tool.common;

import lombok.extern.slf4j.Slf4j;
import org.wyyt.tool.exception.ExceptionTool;

/**
 * the common function of resources
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Slf4j
public final class CommonTool {
    public static void sleep(final long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (Exception e) {
            log.error(ExceptionTool.getRootCauseMessage(e), e);
        }
    }
}