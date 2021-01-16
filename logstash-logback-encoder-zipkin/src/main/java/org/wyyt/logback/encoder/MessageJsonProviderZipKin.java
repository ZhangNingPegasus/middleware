package org.wyyt.logback.encoder;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.loggingevent.MessageJsonProvider;

import java.io.IOException;

/**
 * The logstach formatter used for ZipKin
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
public class MessageJsonProviderZipKin extends MessageJsonProvider {
    @Override
    public void writeTo(final JsonGenerator generator, final ILoggingEvent event) throws IOException {
        super.writeTo(generator, event);
    }
}