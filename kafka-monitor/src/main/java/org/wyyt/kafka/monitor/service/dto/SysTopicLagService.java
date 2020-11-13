package org.wyyt.kafka.monitor.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.kafka.monitor.anno.TranRead;
import org.wyyt.kafka.monitor.anno.TranSave;
import org.wyyt.kafka.monitor.entity.dto.SysTopicLag;
import org.wyyt.kafka.monitor.mapper.SysTopicLagMapper;

import java.util.Date;
import java.util.List;

/**
 * The service for table 'sys_topic_lag'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Service
public class SysTopicLagService extends ServiceImpl<SysTopicLagMapper, SysTopicLag> {

    @TranSave
    public void deleteTopic(final String topicName) {
        if (ObjectUtils.isEmpty(topicName)) {
            return;
        }
        final QueryWrapper<SysTopicLag> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysTopicLag::getTopicName, topicName);
        this.remove(queryWrapper);
    }

    @TranSave
    public void deleteConsumer(final String groupId) {
        if (ObjectUtils.isEmpty(groupId)) {
            return;
        }
        final QueryWrapper<SysTopicLag> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysTopicLag::getGroupId, groupId);
        this.remove(queryWrapper);
    }

    @TranRead
    public List<SysTopicLag> listByGroupId(final String topicName,
                                           final String groupId,
                                           final Date from,
                                           final Date to) {
        final QueryWrapper<SysTopicLag> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(SysTopicLag::getTopicName, topicName)
                .eq(SysTopicLag::getGroupId, groupId)
                .ge(SysTopicLag::getRowCreateTime, from)
                .le(SysTopicLag::getRowCreateTime, to)
                .orderByAsc(SysTopicLag::getRowCreateTime);
        return this.list(queryWrapper);
    }

    @TranRead
    public List<SysTopicLag> listTopLag(final int top) {
        return this.baseMapper.listTopLag(top);
    }
}