package org.wyyt.logback.tool;

import brave.propagation.CurrentTraceContext;
import org.wyyt.logback.core.ApplicationContextZipKin;
import org.wyyt.logback.entity.Trace;

/**
 * The entity of zipkin's trace
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * ******************************************************************
 * Name               Action            Time          Description   *
 * Ning.Zhang       Initialize         10/1/2020        Initialize  *
 * ******************************************************************
 */
public class TraceTool {
    public static Trace getCurrentThreadTrace() {
        final CurrentTraceContext currentTraceContext = ApplicationContextZipKin.getBean(CurrentTraceContext.class);
        if (null == currentTraceContext) {
            return null;
        }
        final Trace result = new Trace();
        result.setTraceId(currentTraceContext.get().traceIdString());
        result.setSpanId(currentTraceContext.get().spanIdString());
        result.setParentId(currentTraceContext.get().parentIdString());
        result.setRootId(currentTraceContext.get().localRootIdString());
        return result;
    }
}