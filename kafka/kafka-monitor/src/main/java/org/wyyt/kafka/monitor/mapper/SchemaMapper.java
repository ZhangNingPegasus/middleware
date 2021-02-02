package org.wyyt.kafka.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.wyyt.kafka.monitor.entity.dto.SysAdmin;
import org.wyyt.kafka.monitor.entity.po.Partition;
import org.wyyt.kafka.monitor.entity.po.PartitionInfo;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * The mapper for database's schema. Using for create database and related tables in the first running time.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Mapper
public interface SchemaMapper extends BaseMapper<SysAdmin> {
    void createTableIfNotExists(@Param(value = "partitionInfoList") List<PartitionInfo> partitionInfoList);

    void removePartitions(@Param("tableName") String tableName,
                       @Param("partitionNameList") Set<String> partitionNameList);

    Set<String> listTables(@Param("dbName") String dbName);

    List<Partition> getPartition(@Param("tableNameList") Collection<String> tableNameList);

    void addPartitions(@Param("tableName") String tableName,
                       @Param("partitionInfoList") List<PartitionInfo> partitionInfoList);
}