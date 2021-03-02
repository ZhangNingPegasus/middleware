package org.springcloud.sms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springcloud.sms.entity.PendingMsgAli;

import java.util.List;

/**
 * The mapper of table `t_pending_msg_ali`
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Mapper
public interface PendingMsgAliMapper extends BaseMapper<PendingMsgAli> {
    void batchSave(@Param("msgList") List<PendingMsgAli> msgList);
}