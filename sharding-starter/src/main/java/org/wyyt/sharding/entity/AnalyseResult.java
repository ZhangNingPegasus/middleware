package org.wyyt.sharding.entity;

import com.alibaba.druid.sql.ast.SQLStatement;
import lombok.Data;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.List;

/**
 * The result of SQL statement analyse result
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Data
public final class AnalyseResult {
    private String sql;
    private List<SQLStatement> sqlStatementList;
    private List<String> tableNameList;
    private SqlCommandType sqlCommandType;
    private List<CaseInsensitiveMap<String, Object>> updateValueMapList;
    private List<CaseInsensitiveMap<String, List<Object>>> whereValueMapList;
    private List<CaseInsensitiveMap<String, Object>> insertValueMapList;
    private List<Object> primaryKeyValueList;
}