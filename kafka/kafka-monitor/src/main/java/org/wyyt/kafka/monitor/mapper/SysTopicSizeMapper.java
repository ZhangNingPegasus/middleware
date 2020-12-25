package org.wyyt.kafka.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.Nullable;
import org.wyyt.kafka.monitor.entity.dto.SysTopicSize;
import org.wyyt.kafka.monitor.entity.vo.TopicRecordCountVo;

import java.util.Date;
import java.util.List;

/**
 * The mapper for table 'sys_topic_size'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Mapper
public interface SysTopicSizeMapper extends BaseMapper<SysTopicSize> {
    Long getHistoryLogSize(@Param(value = "recordTableName") final String recordTableName,
                           @Param(value = "from") final Date from,
                           @Param(value = "to") final Date to);

    List<SysTopicSize> getTopicRank(@Param(value = "rank") final Integer rank,
                                    @Nullable @Param(value = "from") final Date from,
                                    @Nullable @Param(value = "to") final Date to);

    Long getTotalRecordCount(@Param(value = "fromPartition") String fromPartition,
                             @Param(value = "toPartition") String toPartition);

    List<TopicRecordCountVo> listTotalRecordCount(@Param(value = "top") final int top,
                                                  @Param(value = "from0") final Date from0,
                                                  @Param(value = "to0") final Date to0,
                                                  @Param(value = "from1") final Date from1,
                                                  @Param(value = "to1") final Date to1);
}