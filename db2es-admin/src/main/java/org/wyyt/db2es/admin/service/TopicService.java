package org.wyyt.db2es.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.wyyt.db2es.admin.mapper.TopicMapper;
import org.wyyt.db2es.core.entity.persistent.Topic;
import org.wyyt.sharding.anno.TranRead;
import org.wyyt.sharding.anno.TranSave;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The service for table 't_topic'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         10/1/2020      Initialize   *
 * *****************************************************************
 */
@Service
public class TopicService extends ServiceImpl<TopicMapper, Topic> {

    @TranRead
    public Map<String, Topic> listTopicMap(final String searchName) {
        final Map<String, Topic> result = new HashMap<>();
        final List<Topic> topicList = listTopic(searchName);
        for (final Topic topic : topicList) {
            result.put(topic.getName(), topic);
        }
        return result;
    }

    @TranRead
    public List<Topic> listTopic(final String searchName) {
        final QueryWrapper<Topic> queryWrapper = new QueryWrapper<>();
        final LambdaQueryWrapper<Topic> lambda = queryWrapper.lambda();
        if (!StringUtils.isEmpty(searchName)) {
            lambda.like(Topic::getName, searchName);
        }
        lambda.select(Topic::getId,
                Topic::getName,
                Topic::getNumberOfShards,
                Topic::getNumberOfReplicas,
                Topic::getAliasOfYears,
                Topic::getRefreshInterval,
                Topic::getDescription,
                Topic::getRowCreateTime,
                Topic::getRowUpdateTime);
        return this.list(queryWrapper);
    }


    @TranRead
    public Topic getByName(final String topicName) {
        final QueryWrapper<Topic> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<Topic> eq = queryWrapper.lambda()
                .eq(Topic::getName, topicName);
        return this.getOne(queryWrapper);
    }

    @TranRead
    public String getMapping(final Long topicId) {
        final QueryWrapper<Topic> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Topic::getId, topicId).select(Topic::getMapping);
        final Topic topic = this.getOne(queryWrapper);
        if (null == topic) {
            return null;
        }
        return topic.getMapping();
    }

    @TranSave
    public boolean insertOrUpdate(final Topic newTopic) {
        final Topic dbTopic = getByName(newTopic.getName());
        if (null == dbTopic) {
            return this.save(newTopic);
        } else {
            final UpdateWrapper<Topic> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda()
                    .eq(Topic::getId, dbTopic.getId())
                    .set(Topic::getNumberOfShards, newTopic.getNumberOfShards())
                    .set(Topic::getNumberOfReplicas, newTopic.getNumberOfReplicas())
                    .set(Topic::getRefreshInterval, newTopic.getRefreshInterval())
                    .set(Topic::getAliasOfYears, newTopic.getAliasOfYears())
                    .set(Topic::getMapping, newTopic.getMapping())
                    .set(Topic::getDescription, newTopic.getDescription());
            return this.update(updateWrapper);
        }
    }

    @TranSave
    public boolean insertIfNotExists(final Topic newTopic) {
        final Topic dbTopic = getByName(newTopic.getName());
        if (null == dbTopic) {
            return this.save(newTopic);
        }
        return true;
    }
}