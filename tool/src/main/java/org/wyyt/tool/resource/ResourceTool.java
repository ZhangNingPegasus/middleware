package org.wyyt.tool.resource;

import lombok.extern.slf4j.Slf4j;
import org.wyyt.tool.exception.ExceptionTool;

/**
 * the common function of resources
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class ResourceTool {
    public static void closeQuietly(final AutoCloseable target) {
        if (null != target) {
            try {
                target.close();
            } catch (Exception e) {
                log.error(ExceptionTool.getRootCauseMessage(e), e);
            }
        }
    }
}