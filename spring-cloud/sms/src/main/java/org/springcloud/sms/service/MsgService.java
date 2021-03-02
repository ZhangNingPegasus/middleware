package org.springcloud.sms.service;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springcloud.sms.entity.Msg;
import org.springcloud.sms.mapper.MsgMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The service of table `t_msg`
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class MsgService extends ServiceImpl<MsgMapper, Msg> {
    public void batchSave(final List<Msg> msgList) {
        if (null == msgList || msgList.isEmpty()) {
            return;
        }
        this.baseMapper.batchSave(msgList);
    }
}
