package org.wyyt.sharding.db2es.client.ding;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.wyyt.sharding.db2es.client.common.Context;
import org.wyyt.sharding.db2es.client.common.Utils;
import org.wyyt.tool.dingtalk.DingTalkTool;
import org.wyyt.tool.dingtalk.WarningLevel;
import org.wyyt.tool.exception.ExceptionTool;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * the wapper class of DingDing
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class DingDingWrapper implements Closeable {
    private final Context context;
    private final Cache<String, Long> cache;

    public DingDingWrapper(final Context context) {
        this.context = context;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    public final void send(final String content,
                           final List<String> mobileList,
                           final WarningLevel warningLevel) throws Exception {
        Utils.IP localIp = Utils.getLocalIp(this.context);
        DingTalkTool.prettySend(content,
                localIp.getLocalName(),
                localIp.getLocalIp(),
                mobileList,
                warningLevel,
                this.context.getConfig().getDingAccessToken(),
                this.context.getConfig().getDingSecret());
    }

    public final void send(final String content,
                           final WarningLevel warningLevel) {
        try {
            this.send(content, this.context.getConfig().getDingMobiles(), warningLevel);
        } catch (final Exception e) {
            log.error(String.format("DingDingWrapper: send message meet error, [%s]", ExceptionTool.getRootCauseMessage(e)), e);
        }
    }

    public final void sendIfNoDuplicate(final String content,
                                        final WarningLevel warningLevel) {
        final Long ifPresent = this.cache.getIfPresent(content);
        if (null != ifPresent) {
            return;
        }
        send(content, warningLevel);
        this.cache.put(content, 1L);
    }

    @Override
    public final void close() {
        if (null != this.cache) {
            this.cache.invalidateAll();
            this.cache.cleanUp();
        }
    }
}