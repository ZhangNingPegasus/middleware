package org.wyyt.kafka.monitor.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.wyyt.kafka.monitor.entity.dto.SysTableName;

import java.util.Map;

/**
 * The mapper for `sys_table_name`.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Mapper
public interface SysTableNameMapper extends BaseMapper<SysTableName> {

    @MapKey("topicName")
    Map<String, SysTableName> listMap();
}