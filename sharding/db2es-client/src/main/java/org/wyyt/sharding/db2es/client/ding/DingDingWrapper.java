package org.wyyt.sharding.db2es.client.ding;

import lombok.extern.slf4j.Slf4j;
import org.wyyt.sharding.db2es.client.common.Context;
import org.wyyt.sharding.db2es.client.common.Utils;
import org.wyyt.tool.cache.CacheService;
import org.wyyt.tool.dingtalk.DingTalkTool;
import org.wyyt.tool.dingtalk.WarningLevel;
import org.wyyt.tool.exception.ExceptionTool;

import java.io.Closeable;
import java.util.List;

/**
 * the wrapper class of DingDing
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class DingDingWrapper implements Closeable {
    private final Context context;
    private final CacheService cacheService;

    public DingDingWrapper(final Context context) {
        this.context = context;
        this.cacheService = new CacheService(10L, 128, 1024L);
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
        final Long ifPresent = this.cacheService.get(content);
        if (null != ifPresent) {
            return;
        }
        send(content, warningLevel);
        this.cacheService.put(content, 1L);
    }

    @Override
    public final void close() {
        this.cacheService.destroy();
    }
}