package org.wyyt.sharding.db2es.admin.mapper;

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
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Mapper
public interface ShardingDbMapper {
    ArrayList<LinkedHashMap<String, Object>> select(@Param("sql") String sql);
}