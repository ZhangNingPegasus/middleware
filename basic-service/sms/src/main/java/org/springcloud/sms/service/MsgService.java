package org.springcloud.sms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springcloud.sms.entity.Msg;
import org.springcloud.sms.mapper.MsgMapper;
import org.springframework.stereotype.Service;
import org.wyyt.sms.enums.ProviderType;
import org.wyyt.tool.anno.TranSave;

import java.util.Date;
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
    @TranSave
    public void batchSave(final List<Msg> msgList) {
        if (null == msgList || msgList.isEmpty()) {
            return;
        }
        this.baseMapper.batchSave(msgList);
    }

    @TranSave
    public Msg getByMsgId(final String msgId,
                          final String phoneNumber,
                          final ProviderType providerType,
                          final Date rowCreateTime) {
        final QueryWrapper<Msg> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Msg::getMsgId, msgId)
                .eq(Msg::getPhoneNumber, phoneNumber)
                .eq(Msg::getProvider, providerType.getCode())
                .eq(Msg::getRowCreateTime, rowCreateTime);
        return this.getOne(queryWrapper);
    }

    @TranSave
    public void batchUpdate(final List<Msg> msgList) {
        if (null == msgList || msgList.isEmpty()) {
            return;
        }
        for (final Msg msg : msgList) {
            if (null == msg.getErrMsg()) {
                msg.setErrMsg("");
            }
        }
        this.baseMapper.batchUpdate(msgList);
    }
}
