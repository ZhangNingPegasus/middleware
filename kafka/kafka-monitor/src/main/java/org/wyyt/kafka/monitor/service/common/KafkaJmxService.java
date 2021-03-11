package org.wyyt.kafka.monitor.service.common;

import org.springframework.stereotype.Service;
import org.wyyt.kafka.monitor.entity.vo.KafkaBrokerVo;
import org.wyyt.kafka.monitor.util.JMXFactoryUtil;
import org.wyyt.tool.resource.ResourceTool;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The service for kafka's JMX.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class KafkaJmxService {
    private static final String JMX_URL = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";
    private static final Integer TIME_OUT = 10;

    public String getData(final KafkaBrokerVo brokerVo,
                          final String name,
                          final String attribute) throws Exception {
        String result;
        JMXConnector connector = null;
        try {
            final JMXServiceURL jmxServiceUrl = new JMXServiceURL(String.format(JMX_URL, String.format("%s:%s", brokerVo.getHost(), brokerVo.getJmxPort())));
            connector = JMXFactoryUtil.connectWithTimeout(jmxServiceUrl, TIME_OUT, TimeUnit.SECONDS);
            final MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
            result = mbeanConnection.getAttribute(ObjectName.getInstance(name), attribute).toString();
        } finally {
            ResourceTool.closeQuietly(connector);
        }
        return result;
    }

    public String[] getData(final KafkaBrokerVo brokerVo,
                            final String[] names,
                            final String[] attributes) throws Exception {
        final List<String> results = new ArrayList<>(names.length);
        JMXConnector connector = null;
        try {
            final JMXServiceURL jmxServiceUrl = new JMXServiceURL(String.format(JMX_URL, String.format("%s:%s", brokerVo.getHost(), brokerVo.getJmxPort())));
            connector = JMXFactoryUtil.connectWithTimeout(jmxServiceUrl, TIME_OUT, TimeUnit.SECONDS);
            final MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
            for (int i = 0; i < names.length; i++) {
                results.add(mbeanConnection.getAttribute(ObjectName.getInstance(names[i]), attributes[i]).toString());
            }
        } finally {
            ResourceTool.closeQuietly(connector);
        }
        return results.toArray(new String[]{});
    }
}