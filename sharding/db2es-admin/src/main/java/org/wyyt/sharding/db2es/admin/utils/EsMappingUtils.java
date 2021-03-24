package org.wyyt.sharding.db2es.admin.utils;

import org.springframework.util.ObjectUtils;
import org.wyyt.sharding.entity.FieldInfo;

import java.util.List;

/**
 * the utils functions of compare
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
public class EsMappingUtils {
    public static String getEsSource(final int numberOfShards,
                                     final int numberOfReplicas,
                                     final String refreshInterval,
                                     final List<FieldInfo> fieldInfoList) {
        final String result = "{\"settings\":{%s},\"mappings\":{\"properties\":{%s}}}";
        return String.format(result,
                getEsSettings(
                        numberOfShards,
                        numberOfReplicas,
                        refreshInterval
                ),
                getEsProperties(fieldInfoList));
    }

    public static String getEsSource(final int numberOfShards,
                                     final int numberOfReplicas,
                                     final List<FieldInfo> fieldInfoList) {
        return getEsSource(numberOfShards, numberOfReplicas, "1s", fieldInfoList);
    }

    private static String getEsSettings(final int numberOfShards,
                                        final int numberOfReplicas,
                                        final String refreshInterval) {
        final StringBuilder settings = new StringBuilder();
        settings.append(String.format("\"number_of_shards\":%s,", numberOfShards));
        settings.append(String.format("\"number_of_replicas\":%s,", numberOfReplicas));
        settings.append(String.format("\"refresh_interval\": \"%s\",", refreshInterval));
        if (settings.length() > 0) {
            settings.delete(settings.length() - 1, settings.length());
        }
        return settings.toString().trim();
    }

    private static String getEsProperties(final List<FieldInfo> fieldInfoList) {
        final StringBuilder properties = new StringBuilder();
        for (final FieldInfo fieldInfo : fieldInfoList) {
            final String name = fieldInfo.getName();
            String type;
            String format = "";
            switch (fieldInfo.getDataType().toLowerCase()) {
                case "bigint":
                    type = "long";
                    break;
                case "int":
                case "tinyint":
                    type = "integer";
                    break;
                case "datetime":
                    type = "date";
                    format = "yyyy-M-d HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||yyyy-M-d HH:mm:ss||epoch_millis";
                    break;
                case "bit":
                    type = "boolean";
                    break;
                case "float":
                case "double":
                case "decimal":
                    type = "double";
                    break;
                default:
                    type = "keyword";
            }

            if (ObjectUtils.isEmpty(format)) {
                properties.append(String.format("\"%s\":{\"type\":\"%s\"},",
                        name,
                        type));
            } else {
                properties.append(String.format("\"%s\":{\"type\":\"%s\",\"format\":\"%s\"},",
                        name,
                        type,
                        format));
            }
        }

        if (properties.length() > 0) {
            properties.delete(properties.length() - 1, properties.length());
        }
        return properties.toString().trim();
    }
}