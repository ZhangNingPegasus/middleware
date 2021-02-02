package org.wyyt.kafka.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.wyyt.kafka.monitor.entity.dto.SysTableName;
import org.wyyt.kafka.monitor.entity.dto.TopicRecord;
import org.wyyt.kafka.monitor.entity.po.MaxOffset;
import org.wyyt.kafka.monitor.entity.po.PartitionInfo;
import org.wyyt.kafka.monitor.entity.vo.RecordVo;

import java.util.Date;
import java.util.List;

/**
 * The mapper for dynamic table. Using for save the topics'content.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Mapper
public interface TopicRecordMapper extends BaseMapper<TopicRecord> {
    void createTableIfNotExists(@Param(value = "sysTableNameList") List<SysTableName> sysTableNameList,
                                @Param(value = "partitionInfoList") List<PartitionInfo> partitionInfoList);

    void dropTable(@Param(value = "recordTableName") String recordTableName,
                   @Param(value = "recordDetailTableName") String recordDetailTableName);

    void truncateTable(@Param(value = "recordTableName") String recordTableName,
                       @Param(value = "recordDetailTableName") String recordDetailTableName);

    List<MaxOffset> listMaxOffset(@Param(value = "recordTableName") String recordTableName,
                                  @Param(value = "partitionId") Integer partitionId);

    String getRecordDetailValue(@Param("recordDetailTableName") String recordDetailTableName,
                                @Param("partitionId") Integer partitionId,
                                @Param("offset") Long offset);

    List<TopicRecord> listRecords(IPage<RecordVo> page,
                                  @Param("recordTableName") String recordTableName,
                                  @Param("partitionId") Integer partitionId,
                                  @Param("offset") Long offset,
                                  @Param("key") String key,
                                  @Param("from") Date from,
                                  @Param("to") Date to);
}