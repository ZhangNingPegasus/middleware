package org.wyyt.kafka.monitor.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.wyyt.kafka.monitor.entity.dto.SysTopicLag;

import java.util.List;

/**
 * The mapper for table 'sys_topic_lag'.
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Mapper
public interface SysTopicLagMapper extends BaseMapper<SysTopicLag> {
    List<SysTopicLag> listTopLag(@Param(value = "top") final int top);
}