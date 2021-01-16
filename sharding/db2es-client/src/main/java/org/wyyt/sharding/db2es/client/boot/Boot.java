package org.wyyt.sharding.db2es.client.boot;

import lombok.extern.slf4j.Slf4j;
import org.wyyt.sharding.db2es.client.common.Context;
import org.wyyt.tool.common.CommonTool;
import org.wyyt.tool.date.DateTool;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * the main class used for bootstrap
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class Boot {
    private static final AtomicBoolean TERMINATED = new AtomicBoolean(false);
    private static final List<String> TERMINATED_SIGNAL = Arrays.asList("TERM", "INT");

    public static void boot(final Context context) throws Exception {
        log.info(String.format("db2es start with successfully at %s", DateTool.format(new Date())));

        context.getProcessorWrapper().startAll(null);

        registerSignalHandler();

        while (!TERMINATED.get()) {
            CommonTool.sleep(500);
        }

        log.info(String.format("db2es is shutting down at %s... ...", DateTool.format(new Date())));
    }

    private static void registerSignalHandler() {
        SignalHandler signalHandler = signal -> {
            //SIG_INT
            if (TERMINATED_SIGNAL.contains(signal.getName().toUpperCase())) {
                TERMINATED.compareAndSet(false, true);
            }
        };

        Signal.handle(new Signal("TERM"), signalHandler);
        Signal.handle(new Signal("INT"), signalHandler);
    }
}