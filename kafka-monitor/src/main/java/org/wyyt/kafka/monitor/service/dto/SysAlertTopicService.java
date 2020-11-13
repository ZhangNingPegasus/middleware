package org.wyyt.kafka.monitor.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.kafka.monitor.anno.TranRead;
import org.wyyt.kafka.monitor.anno.TranSave;
import org.wyyt.kafka.monitor.entity.dto.SysAlertTopic;
import org.wyyt.kafka.monitor.exception.BusinessException;
import org.wyyt.kafka.monitor.mapper.SysAlertTopicMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The service for table 'sys_alert_topic'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Service
public class SysAlertTopicService extends ServiceImpl<SysAlertTopicMapper, SysAlertTopic> {

    @TranSave
    public boolean save(final String topicName,
                        final String fromTime,
                        final String toTime,
                        final Integer fromTps,
                        final Integer toTps,
                        final Integer fromMomTps,
                        final Integer toMomTps,
                        final String email,
                        final String accessToken,
                        final String secret
    ) {
        if (ObjectUtils.isEmpty(topicName)) {
            throw new BusinessException("主题名称不允许为空");
        }

        final SysAlertTopic orgiSysAlertTopic = getByTopicName(topicName);
        if (null != orgiSysAlertTopic) {
            throw new BusinessException(String.format("主题[%s]的TPS设置已存在", topicName));
        }

        final SysAlertTopic sysAlertTopic = new SysAlertTopic();
        sysAlertTopic.setTopicName(topicName);
        sysAlertTopic.setFromTime(fromTime);
        sysAlertTopic.setToTime(toTime);
        sysAlertTopic.setFromTps(fromTps);
        sysAlertTopic.setToTps(toTps);
        sysAlertTopic.setFromMomTps(fromMomTps);
        sysAlertTopic.setToMomTps(toMomTps);
        sysAlertTopic.setEmail(email);
        sysAlertTopic.setAccessToken(accessToken);
        sysAlertTopic.setSecret(secret);
        return this.save(sysAlertTopic);
    }

    @TranSave
    public boolean update(final Long id,
                          final String topicName,
                          final String fromTime,
                          final String toTime,
                          final Integer fromTps,
                          final Integer toTps,
                          final Integer fromMomTps,
                          final Integer toMomTps,
                          final String email,
                          final String accessToken,
                          final String secret) {
        if (ObjectUtils.isEmpty(topicName)) {
            throw new BusinessException("主题名称不允许为空");
        }

        final SysAlertTopic orgiSysAlertTopic = this.getByTopicName(topicName);
        if (null != orgiSysAlertTopic) {
            if (!topicName.equals(orgiSysAlertTopic.getTopicName())) {
                throw new BusinessException(String.format("主题[%s]的TPS设置已存在", topicName));
            }
        }

        final UpdateWrapper<SysAlertTopic> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(SysAlertTopic::getId, id)
                .set(SysAlertTopic::getTopicName, topicName)
                .set(SysAlertTopic::getFromTime, fromTime)
                .set(SysAlertTopic::getToTime, toTime)
                .set(SysAlertTopic::getFromTps, fromTps)
                .set(SysAlertTopic::getToTps, toTps)
                .set(SysAlertTopic::getFromMomTps, fromMomTps)
                .set(SysAlertTopic::getToMomTps, toMomTps)
                .set(SysAlertTopic::getEmail, email)
                .set(SysAlertTopic::getAccessToken, accessToken)
                .set(SysAlertTopic::getSecret, secret);

        return this.update(updateWrapper);
    }

    @TranRead
    public List<String> listTopicNames() {
        final QueryWrapper<SysAlertTopic> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(SysAlertTopic::getTopicName);
        return this.list(queryWrapper).stream().map(SysAlertTopic::getTopicName).collect(Collectors.toList());
    }

    @TranRead
    public SysAlertTopic getByTopicName(final String topicName) {
        if (ObjectUtils.isEmpty(topicName)) {
            return null;
        }
        final QueryWrapper<SysAlertTopic> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysAlertTopic::getTopicName, topicName);
        return this.getOne(queryWrapper);
    }

    @TranSave
    public void deleteTopic(String topicName) {
        if (ObjectUtils.isEmpty(topicName)) {
            return;
        }
        final QueryWrapper<SysAlertTopic> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysAlertTopic::getTopicName, topicName);
        this.remove(queryWrapper);
    }
}