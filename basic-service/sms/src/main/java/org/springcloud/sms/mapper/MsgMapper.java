package org.springcloud.sms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springcloud.sms.entity.Msg;

import java.util.List;

/**
 * The mapper of table `t_msg`
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Mapper
public interface MsgMapper extends BaseMapper<Msg> {
    void batchSave(@Param("msgList") List<Msg> msgList);

    void batchUpdate(@Param("msgList") List<Msg> msgList);
}