package org.wyyt.sharding.sqltool.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.sharding.sqltool.entity.vo.FieldVo;
import org.wyyt.sharding.sqltool.entity.vo.IndexVo;
import org.wyyt.tool.sql.SqlTool;

import java.util.*;

/**
 * the service which providing the ability of genrate table creating script
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Service
public class TableService {
    private static final String LINE_SEPERATOR = "&#13;&#10;";

    public final Map<String, String> createTable(final String tableName,
                                                 final Integer dbCount,
                                                 final Integer tableCount,
                                                 final List<FieldVo> fieldVoList,
                                                 final List<IndexVo> indexVoList) {
        final Map<String, String> result = new LinkedHashMap<>();
        for (int i = 1; i <= dbCount; i++) {
            final String dbName = "数据库".concat(String.valueOf(i));
            final StringBuilder sql = new StringBuilder();
            for (int j = 0; j < tableCount; j++) {
                final String tbName = String.format("%s_%s", tableName, j + (i - 1) * (tableCount));
                final String tableSql = generateCreateTable(tbName, fieldVoList, indexVoList);
                sql.append(tableSql);
                if (j < tableCount - 1) {
                    sql.append(LINE_SEPERATOR);
                    sql.append(LINE_SEPERATOR);
                }
            }
            result.put(dbName, sql.toString().trim());
        }
        return result;
    }

    private String generateCreateTable(final String tableName,
                                       final List<FieldVo> fieldVoList,
                                       final List<IndexVo> indexVoList) {
        final StringBuilder strTable = new StringBuilder();
        final List<String> pkNames = new ArrayList<>();
        strTable.append(String.format("CREATE TABLE IF NOT EXISTS `%s` (%s", SqlTool.removeMySqlQualifier(tableName), LINE_SEPERATOR));

        if (null != fieldVoList && !fieldVoList.isEmpty()) {
            for (final FieldVo fieldVo : fieldVoList) {
                if (null == fieldVo) {
                    continue;
                }

                String defaultValue = fieldVo.getDefaultValue();
                if ("now()".equalsIgnoreCase(defaultValue)) {
                    defaultValue = "CURRENT_TIMESTAMP(0)";
                }

                strTable.append(String.format("%s%s%s%s%s COMMENT '%s',%s",
                        generateFieldType(fieldVo),
                        fieldVo.getNotNull() ? " NOT NULL" : " NULL",
                        !ObjectUtils.isEmpty(defaultValue) ? " DEFAULT".concat(" ").concat(defaultValue) : fieldVo.getNotNull() ? "" : " DEFAULT NULL",
                        fieldVo.getAutoIncrement() ? " AUTO_INCREMENT" : "",
                        fieldVo.getAutoUpdateByTimestampt() == null || !fieldVo.getAutoUpdateByTimestampt() ? "" : " ON UPDATE CURRENT_TIMESTAMP(0)",
                        fieldVo.getComment(),
                        LINE_SEPERATOR));

                if (fieldVo.getIsPrimary()) {
                    pkNames.add(String.format("`%s`", SqlTool.removeMySqlQualifier(fieldVo.getName())));
                }
            }
            strTable.delete(strTable.length() - String.format(",%s", LINE_SEPERATOR).length(), strTable.length());
        }

        if (!pkNames.isEmpty()) {
            strTable.append(String.format(",%s", LINE_SEPERATOR));
            strTable.append(String.format("PRIMARY KEY (%s) USING BTREE",
                    StringUtils.join(pkNames, ',')));
        }

        if (null != indexVoList && !indexVoList.isEmpty()) {
            strTable.append(String.format(",%s", LINE_SEPERATOR));
            for (final IndexVo indexVo : indexVoList) {
                strTable.append(String.format("%sINDEX `%s`(%s) USING BTREE COMMENT '%s',%s",
                        "NORMAL".equalsIgnoreCase(indexVo.getIndexType()) ? "" : "UNIQUE ",
                        SqlTool.removeMySqlQualifier(indexVo.getIndexName()),
                        indexVo.getFieldName(),
                        indexVo.getComment(),
                        LINE_SEPERATOR));
            }
            strTable.delete(strTable.length() - String.format(",%s", LINE_SEPERATOR).length(), strTable.length());
        }
        strTable.append(LINE_SEPERATOR);
        strTable.append(") ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;");
        return strTable.toString();
    }

    private String generateFieldType(final FieldVo fieldVo) {
        final List<String> mathTypeList = Arrays.asList("BIGINT", "INT", "TINYINT", "DECIMAL", "DOUBLE", "FLOAT");
        final List<String> strTypeList = Arrays.asList("VARCHAR", "CHAR");

        final String name = SqlTool.removeMySqlQualifier(fieldVo.getName());
        final String type = fieldVo.getType().toUpperCase();
        String length = fieldVo.getLength() == null ? "" : fieldVo.getLength().toString();
        final Boolean unsigned = fieldVo.getUnsigned();

        if (fieldVo.getDecimal() != null) {
            length = String.format("%s, %s", length, fieldVo.getDecimal());
        }

        if (ObjectUtils.isEmpty(length)) {
            if ("BIGINT".equalsIgnoreCase(type)) {
                length = "20";
            } else if ("INT".equalsIgnoreCase(type)) {
                length = "11";
            } else if ("VARCHAR".equalsIgnoreCase(type)) {
                length = "255";
            } else if ("SMALLINT".equalsIgnoreCase(type)) {
                length = "6";
            } else if ("BIT".equalsIgnoreCase(type)) {
                length = "1";
            } else if ("DATETIME".equalsIgnoreCase(type)) {
                length = "0";
            } else if ("TINYINT".equalsIgnoreCase(type)) {
                length = "4";
            }
        }

        if (mathTypeList.contains(type)) {
            return String.format("`%s` %s(%s)%s",
                    name,
                    type,
                    length,
                    unsigned ? " UNSIGNED" : "");
        } else if (strTypeList.contains(type)) {
            return String.format("`%s` %s(%s) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
                    name,
                    type,
                    length);
        } else {
            return String.format("`%s` %s(%s)",
                    name,
                    type,
                    length);
        }
    }
}