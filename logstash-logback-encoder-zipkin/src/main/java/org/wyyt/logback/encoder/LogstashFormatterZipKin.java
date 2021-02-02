package org.wyyt.logback.encoder;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.ContextAware;
import net.logstash.logback.LogstashFormatter;
import net.logstash.logback.composite.JsonProvider;
import net.logstash.logback.composite.loggingevent.MessageJsonProvider;

/**
 * The logstach formatter used for ZipKin
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public class LogstashFormatterZipKin extends LogstashFormatter {

    public LogstashFormatterZipKin(final ContextAware declaredOrigin) {
        super(declaredOrigin);
        addProvider(new MessageJsonProviderZipKin());
    }

    @Override
    public void addProvider(final JsonProvider<ILoggingEvent> provider) {
        for (final JsonProvider jsonProvider : this.getProviders().getProviders()) {
            if (jsonProvider instanceof MessageJsonProvider) {
                this.getProviders().removeProvider(jsonProvider);
                break;
            }
        }
        super.addProvider(provider);
    }
}