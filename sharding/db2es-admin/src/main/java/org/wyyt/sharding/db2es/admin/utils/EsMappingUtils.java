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
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
public class EsMappingUtils {
    public static String getEsMapping(final List<FieldInfo> fieldInfoList) {
        String result = "{\"properties\": {%s}}";

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

        return String.format(result, properties.toString().trim()).trim();
    }
}