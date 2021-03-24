package org.wyyt.sharding.db2es.core.util;

import cn.hutool.core.date.DateUtil;
import com.ctrip.framework.apollo.ConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.wyyt.sharding.db2es.core.entity.domain.Config;
import org.wyyt.sharding.db2es.core.entity.domain.TableMap;
import org.wyyt.sharding.db2es.core.entity.persistent.Property;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.wyyt.sharding.db2es.core.entity.domain.Names.*;

/**
 * the common functions
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public final class CommonUtils {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static boolean isLocalPortUsing(final int port) {
        return isPortUsing("127.0.0.1", port);
    }

    public static boolean isPortUsing(final String host, final int port) {
        try {
            final InetAddress Address = InetAddress.getByName(host);
            new Socket(Address, port);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static String getVersion() {
        return System.getProperty("version", "BETA");
    }

    public static String formatMs(final Date value) {
        return DateUtil.format(value, "yyyy-MM-dd HH:mm:ss.SSS");
    }

    public static void fillConfig(final List<Property> properties,
                                  final TableMap tableMap,
                                  final Config config) {
        config.setTableMap(tableMap);
        if (null == properties || properties.isEmpty()) {
            return;
        }
        for (final Property property : properties) {
            final String value = property.getValue();
            if (DB2ES_ADMIN_HOST.equals(property.getName())) {
                config.setDb2esAdminHost(value);
            } else if (DB2ES_ADMIN_PORT.equals(property.getName())) {
                config.setDb2esAdminPort(Integer.parseInt(value));
            } else if (DING_ACCESS_TOKEN.equals(property.getName())) {
                config.setDingAccessToken(value);
            } else if (DING_SECRET.equals(property.getName())) {
                config.setDingSecret(value);
            } else if (DING_MOBILES.equals(property.getName())) {
                final String[] mobileArray = value.split(",");
                final List<String> mobileList = new ArrayList<>(mobileArray.length);
                for (final String mobile : mobileArray) {
                    if (ObjectUtils.isEmpty(mobile.trim())) {
                        continue;
                    }
                    mobileList.add(mobile);
                }
                config.setDingMobiles(mobileList);
            }
        }
    }

    public static long toEsVersion(final Date date) {
        return date.getTime() * 100000;
    }
}