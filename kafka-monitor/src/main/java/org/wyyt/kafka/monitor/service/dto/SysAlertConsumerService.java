package org.wyyt.kafka.monitor.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.wyyt.kafka.monitor.anno.TranRead;
import org.wyyt.kafka.monitor.anno.TranSave;
import org.wyyt.kafka.monitor.entity.dto.SysAlertConsumer;
import org.wyyt.kafka.monitor.mapper.SysAlertConsumerMapper;

import java.util.List;

/**
 * The service for table 'sys_alert_consumer'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Service
public class SysAlertConsumerService extends ServiceImpl<SysAlertConsumerMapper, SysAlertConsumer> {

    @TranRead
    public List<SysAlertConsumer> getByGroupId(String groupId) {
        QueryWrapper<SysAlertConsumer> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysAlertConsumer::getGroupId, groupId).orderByAsc(SysAlertConsumer::getTopicName);
        return this.list(queryWrapper);
    }

    @TranSave
    public boolean save(final String groupId,
                        final String topicName,
                        final Long lagThreshold,
                        final String email,
                        final String accessToken,
                        final String secret) {
        final SysAlertConsumer sysAlertConsumer = new SysAlertConsumer();
        sysAlertConsumer.setGroupId(groupId);
        sysAlertConsumer.setTopicName(topicName);
        sysAlertConsumer.setLagThreshold(lagThreshold);
        sysAlertConsumer.setEmail(email);
        sysAlertConsumer.setAccessToken(accessToken);
        sysAlertConsumer.setSecret(secret);
        return this.save(sysAlertConsumer);
    }

    @TranSave
    public void update(final Long id,
                       final String groupId,
                       final String topicName,
                       final Long lagThreshold,
                       final String email,
                       final String accessToken,
                       final String secret) {
        final UpdateWrapper<SysAlertConsumer> updateWrapper = new UpdateWrapper();
        updateWrapper.lambda().eq(SysAlertConsumer::getId, id)
                .set(SysAlertConsumer::getGroupId, groupId)
                .set(SysAlertConsumer::getTopicName, topicName)
                .set(SysAlertConsumer::getLagThreshold, lagThreshold)
                .set(SysAlertConsumer::getEmail, email)
                .set(SysAlertConsumer::getAccessToken, accessToken)
                .set(SysAlertConsumer::getSecret, secret);
        this.update(updateWrapper);
    }


    public void deleteConsumer(final String groupId) {
        if (StringUtils.isEmpty(groupId)) {
            return;
        }
        final QueryWrapper<SysAlertConsumer> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysAlertConsumer::getGroupId, groupId);
        this.remove(queryWrapper);
    }
}