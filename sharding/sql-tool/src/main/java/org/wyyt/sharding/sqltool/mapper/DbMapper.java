package org.wyyt.sharding.sqltool.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * The mapper of mybatis-plus
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       01/01/2021       Initialize   *
 * *****************************************************************
 */
@Mapper
public interface DbMapper {
    Integer insert(@Param("sql") String sql);

    Integer delete(@Param("sql") String sql);

    Integer update(@Param("sql") String sql);

    ArrayList<LinkedHashMap<String, Object>> select(@Param("sql") String sql);
}