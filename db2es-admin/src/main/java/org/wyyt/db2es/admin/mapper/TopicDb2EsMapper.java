package org.wyyt.db2es.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.wyyt.db2es.admin.entity.dto.TopicDb2Es;
import org.wyyt.db2es.admin.entity.vo.TopicDb2EsVo;
import org.wyyt.db2es.core.entity.persistent.Topic;

import java.util.List;
import java.util.Map;

/**
 * The mapper of table t_topic_db2es
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize        10/1/2020        Initialize  *
 * *****************************************************************
 */
@Mapper
public interface TopicDb2EsMapper extends BaseMapper<TopicDb2Es> {
    List<Topic> listUnused();

    @MapKey("name")
    Map<String, TopicDb2EsVo> listAll();
}