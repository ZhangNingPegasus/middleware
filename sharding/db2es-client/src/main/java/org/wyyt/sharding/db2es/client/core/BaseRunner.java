package org.wyyt.sharding.db2es.client.core;

import org.wyyt.sharding.db2es.client.common.Context;
import org.wyyt.tool.common.CommonTool;

import java.io.Closeable;

/**
 * the base class for thread
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public abstract class BaseRunner implements Runnable, Closeable {
    private static final long INTERVAL = 100L;
    protected final Context context;
    protected volatile boolean terminated = false;

    protected BaseRunner(final Context context) {
        this.context = context;
    }

    protected boolean continued() {
        return !this.terminated && this.context.getZooKeeperWrapper().isLeader();
    }

    public final void sleepInterval(final long milliseconds) {
        final long sleepMillseconds = Math.min(milliseconds, INTERVAL);
        final long end = System.currentTimeMillis() + milliseconds;
        while (System.currentTimeMillis() < end) {
            CommonTool.sleep(sleepMillseconds);
            if (this.terminated) {
                break;
            }
        }
    }
}