package org.wyyt.kafka.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.wyyt.kafka.monitor.entity.dto.SysAdmin;

import java.util.Date;
import java.util.Set;

/**
 * The mapper for database's schema. Using for create database and related tables in the first running time.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Mapper
public interface SchemaMapper extends BaseMapper<SysAdmin> {
    void createTableIfNotExists();

    void deleteExpired(@Param("tableNameList") Set<String> tableNameList,
                       @Param("dateTime") Date dateTime);

    Set<String> listTables(@Param("dbName") String dbName);
}