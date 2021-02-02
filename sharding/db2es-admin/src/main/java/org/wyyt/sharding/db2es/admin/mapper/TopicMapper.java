package org.wyyt.sharding.db2es.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.wyyt.sharding.db2es.core.entity.persistent.Topic;

/**
 * The mapper of table t_topic
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Mapper
public interface TopicMapper extends BaseMapper<Topic> {
}