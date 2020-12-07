package org.wyyt.logback.encoder;

import ch.qos.logback.classic.spi.ILoggingEvent;
import net.logstash.logback.composite.CompositeJsonFormatter;
import net.logstash.logback.encoder.LogstashEncoder;

/**
 * The logstach encoder used for ZipKin
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * ******************************************************************
 * Name               Action            Time          Description   *
 * Ning.Zhang       Initialize         10/1/2020        Initialize  *
 * ******************************************************************
 */
public class LogstashEncoderZipkin extends LogstashEncoder {
    @Override
    protected CompositeJsonFormatter<ILoggingEvent> createFormatter() {
        return new LogstashFormatterZipKin(this);
    }
}