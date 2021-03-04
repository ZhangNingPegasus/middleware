package org.springcloud.sms.service;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springcloud.sms.entity.PendingMsgAli;
import org.springcloud.sms.mapper.PendingMsgAliMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The service of table `t_pending_msg_ali`
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Service
public class PendingMsgAliService extends ServiceImpl<PendingMsgAliMapper, PendingMsgAli> {
    public void batchSave(final List<PendingMsgAli> pendingMsgAliList) {
        if (null == pendingMsgAliList || pendingMsgAliList.isEmpty()) {
            return;
        }
        this.baseMapper.batchSave(pendingMsgAliList);
    }
}
