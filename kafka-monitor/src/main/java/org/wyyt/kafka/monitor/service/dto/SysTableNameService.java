package org.wyyt.kafka.monitor.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wyyt.kafka.monitor.anno.TranRead;
import org.wyyt.kafka.monitor.anno.TranSave;
import org.wyyt.kafka.monitor.entity.dto.SysTableName;
import org.wyyt.kafka.monitor.mapper.SysTableNameMapper;

import java.util.Map;

/**
 * The service for table 'sys_table_name'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Service
public class SysTableNameService extends ServiceImpl<SysTableNameMapper, SysTableName> {
    @TranSave
    public SysTableName insert(final String topicName,
                               final String recordTableName,
                               final String recordDetailTableName) {
        final SysTableName sysTableName = new SysTableName();
        sysTableName.setTopicName(topicName);
        sysTableName.setRecordTableName(recordTableName);
        sysTableName.setRecordDetailTableName(recordDetailTableName);
        this.save(sysTableName);
        return sysTableName;
    }

    @TranRead
    public SysTableName getByTopicName(final String topicName) {
        if (ObjectUtils.isEmpty(topicName)) {
            return null;
        }
        final QueryWrapper<SysTableName> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysTableName::getTopicName, topicName);
        return this.getOne(queryWrapper);
    }

    @TranRead
    public SysTableName getByTableName(final String recordTableName,
                                       final String recordDetailTableName) {
        if (ObjectUtils.isEmpty(recordTableName) || ObjectUtils.isEmpty(recordDetailTableName)) {
            return null;
        }
        final QueryWrapper<SysTableName> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(SysTableName::getRecordTableName, recordTableName)
                .eq(SysTableName::getRecordDetailTableName, recordDetailTableName);
        return this.getOne(queryWrapper);
    }

    @TranSave
    public void deleteTopic(final String topicName) {
        if (ObjectUtils.isEmpty(topicName)) {
            return;
        }
        final QueryWrapper<SysTableName> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysTableName::getTopicName, topicName);
        this.remove(queryWrapper);
    }

    @TranRead
    public Map<String, SysTableName> listMap() {
        return this.baseMapper.listMap();
    }
}